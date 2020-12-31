# Control Flow

Right now, our interpreter is basically a calculator. In this chapter, our interpreter takes a big step toward the programming language major leagues: Turing-completeness.

## Turing Machines (Briefly)

In the early 1900s, mathematicians stumbled into a series of paradoxes that led them to doubt the foundation of their work (Russell's paradox being one of these). To address this, they went back to square one. They wanted to rigorously answer questions like "Can we comput all functions that we can define?" or "What do we mean when we claim a function is 'computable'?".

Alan Turing and Alonzo Church devised a precise answer to the last question - a definition of what kinds of functions are computable. Turing produced a Turing machine and Church lambda calculus.

![A-machine](https://craftinginterpreters.com/image/control-flow/turing-machine.png)

Any programming language with some minimal level of expressiveness is powerful enough to compute any computable function.

You can prove that by writing a simulator for a Turing machine in your language. You just need to translate the computable function into a Turing machine, and run it on your simulator.

To be Turing-complete, you need arithmetic, a little control flow, and the ability to allocate and use (theoretically) arbitrary amounts of memory. We have the first, and by the end of this chapter, we'll have the second.

## Conditional Execution

There's roughly two kinds of control flow - 

* Conditional/branching control flow is used to not execute some piece of code. You are able to jump ahead of a region of code

* Looping control flow executes a chunk of code more than once. It jumps back to run the same code again. This usually have some logic to prevent infinite loops

In C-derived languages, branching has if statements and ternary operators. Lox only has if statement.

```
statement      → exprStmt
               | ifStmt
               | printStmt
               | block ;

ifStmt         → "if" "(" expression ")" statement
               ( "else" statement )? ;
```

An if statement has an expression for the condition then a statement to execute if the condition is truthy. It might also have an else keyword and a statement to execute if the condition is falsey.

```
"If         : Expr condition, Stmt thenBranch," +
              " Stmt elseBranch",
```

The parse recognizes an if statement by the leading if keyword - 

`    if (match(IF)) return ifStatement();`

```
  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after if condition."); 

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }
```

![Danling Else Problem](https://craftinginterpreters.com/image/control-flow/dangling-else.png)

`if (first) if (second) whenTrue(); else whenFalse();`

Which if statement should the else belong to?

Most languages and parsers avoid this problem in an ad hoc way - else is bound to the nearest if that precedes it.

Our parser conveniently does that already.

Now we're ready to interpret - 

```
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }
```

Most other syntax trees always evaluate their subtrees. But this has an if statement in its implementation. We might not evaluate the then or else statement.

## Logical Operators

The logical operators `and` and `or` are the other part of branching control flow.

These are not like other binary operators because they short-circuit. In order for an `and` expression to evaluate to something truthy, both operands must be truthy.

These new operators are low in the precedence table. They each have their own precedence with `or` lower than `and`.

```
expression     → assignment ;
assignment     → IDENTIFIER "=" assignment
               | logic_or ;
logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
```

It's cleaner to define a new class for these operators rather than reuse the existing Expr.Binary class.

`      "Logical  : Expr left, Token operator, Expr right",`

`    Expr expr = or();`

```
  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
```

In Interpreter - 

```
  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }
```

We evaluate the left operand first, check its value to see if we can short circuit. If not, we then check the right operand.

We don't literally return `true` or `false`, but a value with proper truthiness.

```
print "hi" or 2; // "hi".
print nil or "yes"; // "yes".
```

## While Loops

The grammar for while loops is the same as in C - 

```
statement      → exprStmt
               | ifStmt
               | printStmt
               | whileStmt
               | block ;

whileStmt      → "while" "(" expression ")" statement ;
```

```
      "Var        : Token name, Expr initializer",
      "While      : Expr condition, Stmt body"
```

The condition is an expression and the body is a statement.

In the parser, we follow the same process we used for if statements.

`    if (match(WHILE)) return whileStatement();`

```
  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }
```

```
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
  }
```

The method isn't complex, but this makes Lox much more powerful. Our program's running time is no longer bound by the length of the source code.

## For Loops

We will just be building an old school for loop.

```
statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | whileStmt
               | block ;

forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                 expression? ";"
                 expression? ")" statement ;
```

The for loop has three clauses separated by semicolons:

1. Initializer - executed once, before anything else. It's usually an expression, but it can also be variable declaration.

2. Condition - this controls when to exit the loop. It is evaluated once at the beginning of each iteration, including the first.

3. Increment - This does some work at the end of each loop iteration. No result is saved, so it must be a side effect. It usually increments a variable

### Desugaring

For loops are technically necessary. The same functionality is possible with just while loops and putting the initializer and increment where they need to be.

```
{
  var i = 0;
  while (i < 10) {
    print i;
    i = i + 1;
  }
}
```

But for loops make common code patterns more pleasant to write. This is referred to as syntactic sugar.

![Sugar](https://craftinginterpreters.com/image/control-flow/sugar.png)

Desugaring involves a process where the front end takes code using syntax sugar and translates it to a more primitive form that the back end already knows how to execute.

We while desugar for loops to the while loops that our interpreter already knows how to handle.

`    if (match(FOR)) return forStatement();`

```
  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(
          Arrays.asList(
              body,
              new Stmt.Expression(increment)));
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }
```

If the token following the ( is a semicolon, the initializer has been omitted. Otherwise, we check for a var keyword to see if it's a variable declaration. If not, it must be an expression.

Then in the condition, we look for a semicolon, if not, it's an expression.

Finally, we look for the increment. If there is one, it executes after the body in each iteration of the loop. 

We do that by replacing the body with a little block that contains the original body followed by an expression statement that evaluates the increment. Then we build the loop using a primitive while loop. If there's an initializer, it runs once before the entire loop. Then it happens again, replacing the whole statement with a block that runs the initializer and then executes the loop.

Now Lox is powerful enough to entertain us.