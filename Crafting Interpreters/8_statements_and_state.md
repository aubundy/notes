# Statements and State

We still don't have a way to bind a name to some data or function. You can't compose software without a way to refer to the smaller pieces to build with.

Our interpreter needs internal state in order to support bindings. State and statements go hand in hand. Statements are useful because they produce side effects. It might be producing user-visible output or modifying some state in the interpreter to be detected later. We'll do both of these here. We'll define statements that produce output (`print`) and create state (`var`). We'll add expressions to access and assign to variables, add blocks, and add local scope.

## Statements

Let's extend Lox's grammar with statements.

1. Expression statements - these let you place an expression where a statement is expected. These evaluate expressions that have side effects. In C, Java, etc. when you see a function/method call followed by ';', you're seeing an expression statement.

2. `print` statement - evaluates an expression and displays the result to the user. This could potentially be an outside library our language brings in, but it helps when building out an interpreter step-by-step.

With this new syntax, we need new grammar rules.

```
program        → statement* EOF ;

statement      → exprStmt
               | printStmt ;

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
```

A program is a list of statements followed by the special "end of file" token. The end of file token ensures the parser consumes the entire input and doesn't silently ignore erroneous unconsumed tokens at the end of a script.

Right now, statement only has two cases for the statements we've described. 

### Statement syntax trees

The grammar doesn't allow for both an expression and statement to occur. + are always expressions, while loop blocks are always statements, etc.

So we need separate classes to handle both of these. This will help the compiler find dumb mistakes like passing a statement to a method that expects an expression.

```
    defineAst(outputDir, "Stmt", Arrays.asList(
      "Expression : Expr expression",
      "Print      : Expr expression"
    ));
```

This will generate a new `Stmt.java` file for us with the syntax tree classes we need for expression and print statements.

### Parsing statements

Now our grammar has the correct starting rule, 'program', we can update our `parse()` method.

```
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(statement());
    }

    return statements; 
  }
```

This parses a series of statements, until the end of input.

```
  private Stmt statement() {
    if (match(PRINT)) return printStatement();

    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }
```

We look at the current token and determine the specific statement rule needed, print or expression.

Print statements parse the subsequent expression, consume the terminating semicolon, and emit the syntax tree.

Expression statements follow the same pattern, but we wrap the Expr in a Stmt of the correct type and return it.

### Executing statements

Since we can now produce statement syntax tree, we need to interpret them. We'll use the Visitor pattern we saw in expressions.

```
class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
```

Statement don't produce values like expressions, though, so we use Void as the return type.

We need a visit method for each statement type.

```
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }
```

We evaluate the inner expression using our existing `evaluate()` method and discard the value. Then we return null to satisfy the Void return type.

For print, we convert the expression's value to a string and dump it to stdout.

```
  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }
```

We modify the old interpret method to accept statements. And we let Java know we're working with lists

`import java.util.List;`

Update the Lox class 

```
    List<Stmt> statements = parser.parse();
    ...
    interpreter.interpret(statements);
```

## Global Variables

Now that we have statements, we can start working on state. We'll start with globals before getting to lexical scoping.

1. Variable declaration - these bring new variables into the world

`var beverage = "espresso";`

2. Variable expression - these access the bindings created with declarations.

`print beverage; // "espresso"`

## Variable syntax

Variable declarations are statements, but they are different than normal statements. 

We are not going to allow declarations inside the clauses of control flow statements (after an `if` or inside a `while`).

We'll add a special rule to accommodate for this distinction.

```
program        → declaration* EOF ;

declaration    → varDecl
               | statement ;

statement      → exprStmt
               | printStmt ;
```

Declaration statements go under the new declaration rule and right now, it's only variables. Functions and classes will also go here. The declaration rule falls through to statements since non-declaring statements are allowed everywhere declaration statements are allowed.

Rule for declaring a variable - 

`varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;`

`var` is the leading keyword, followed by an identifier token and optional initializer expression.

Here's a new primary expression in order to access the variable - 

```
primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;
```

Let's add a new statement tree for a variable declaration - 

```
      "Print      : Expr expression",
      "Var        : Token name, Expr initializer"
```

And an expression node for accessing a variable - 

```
      "Unary    : Token operator, Expr right",
      "Variable : Token name"
```

### Parsing variables

We need to shift around some code to make room for the new declaration rule in the grammar. The top level of a program is now a list of declarations.

In `parser.java`, `parse()` - 

`statements.add(declaration());`

```
  private Stmt declaration() {
    try {
      if (match(TokenType.VAR)) return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }
```

The `declaration()` method is the method we call repeatedly when parsing a series of statements in a block or a script. So we'll synchronize here when the parser goes into panic mode.

```
  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }
```

The recursive descent follows the grammar rule, so it next requires and consumes an identifier token for the variable name. If it sees a = token next, it knows it's an expression and parses it. 

In `primary()` - 

```
    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
```

This gives us a working front end for declaring and using variables. Now we just need to feed it into the interpreter, but we need to talk about where variables live in memory first.

## Environments