# Evaluating Expressions

[Decrepit Victorian Mansion](http://craftinginterpreters.com/image/evaluating-expressions/lightning.png)

There are lots of ways to tell the computer what to do with the user's commands. You can compile it to machine code, translate it into another high-level language, or reduce it to some bytecode format for a virtual machine to run. The shortest path is to just execute the source code itself, which is what we will do.

Since we only support expressions right now, we will evaluate an expression and produce a value in order to execute the code.

1. What kind of values do we want to produce?
2. How do we organize the chunks of code that know how to evaluate different expressions?

## Representing Values

In Lox, values are created by literals, computed by expressions, and stored in variables. These look like Lox values, but they are implemented in Java, the language of our interpreter. We will have to build a bridge between our dynamic types and Java's static types.

```
Lox                 Java
any Lox value   --- Object
nil             --- null
Boolean         --- Boolean
number          --- Double
string          --- String
```

## Evaluating Expressions

We could create an `interpret()` inside the syntax tree classes, but we don't want to put too much logic in there. So instead, we will continue to use our `Visitor` pattern.

```
package com.craftinginterpreters.lox;

class Interpreter implements Expr.Visitor<Object> {
}
```

Create a new class that declares it's a visitor. We will need to define visit methods for each of the four expression tree classes our parser produces.

### Evaluating literals

A literal is a bit of syntax that produces a value. Lots of values are produced by computation, yet don't exist anywhere in the code. Just like we convert a literal token into a literal syntax tree node, we know convert the literal syntax tree node into a runtime value.

All we need to do is pull out the exact literal from the syntax tree node.

```
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }
```

### Evaluating parentheses

```
  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
```

A grouping node has a reference to an inner node for the expression contained inside the parentheses. To evaluate the actual grouping expression, we recursively evaluate the subexpression and return it.

### Evaluating unary expressions

Unary expressions also have a single subexpression that we evaluate first, but then we also do a little work afterwards.

```
  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        return -(double)right;
    }

    // Unreachable.
    return null;
  }
```

We evaluate the operand and then apply the unary operator itself to the result of that. Since we don't statically know that the subexpression must be a number given a minus, we cast it before performing the operation. This is what makes languages dynamic.

We can't evaluate the unary operator itself until we evaluate the operand subexpression (post-order traversal).

### Truthiness and falsiness

We need to decide what happens in Lox whenever `true` or `false` isn't immediately behind a `!`. Lox follows Ruby's rules where `false` and `nil` are falsy, and everything else is truthy.

```
  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }
```

### Evaluating binary expressions

```
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
      Object left = evaluate(expr.left);
      Object right = evaluate(expr.right); 
  
      switch (expr.operator.type) {
        case BANG_EQUAL: return !isEqual(left, right);
        case EQUAL_EQUAL: return isEqual(left, right);
        case GREATER:
          return (double)left > (double)right;
        case GREATER_EQUAL:
          return (double)left >= (double)right;
        case LESS:
          return (double)left < (double)right;
        case LESS_EQUAL:
          return (double)left <= (double)right;
        case MINUS:
          return (double)left - (double)right;
        case PLUS:
          if (left instanceof Double && right instanceof Double) {
            return (double)left + (double)right;
          } 

          if (left instanceof String && right instanceof String) {
            return (String)left + (String)right;
          }
        case SLASH:
          return (double)left / (double)right;
        case STAR:
          return (double)left * (double)right;
      }
  
      // Unreachable.
      return null;
    }
```

Pretty straightforward. The plus sign, however, can be used to concatenate two strings, so instead of casting the type, we dynamically check the type. 

```
  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }
```

We also need to represent Lox's version of equality, not Java's. But they are similar. Neither does implicit conversion. We just have to specially handle `nil`.

This is all we need for a valid Lox expression. But what if we have an invalid one?

## Runtime Errors

Syntax and static errors are reported before any code is executed. Runtime errors are failures that the language semantics demand we detect and report while the program is running.

We want users to understand a Lox runtime error has occured, not merely copy Java's runtime errors.

[Negating a Muffin](https://craftinginterpreters.com/image/evaluating-expressions/muffin.png)

### Detecting runtime errors

Since our interpreter evaluates nested expressions using recursive method calls, we need to unwind out of all of those whenever there is a runtime error.

In `visitUnaryExpr()`:

```
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
```

```
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }
```

When the check fails, it throws a `RuntimeError`:

```
class RuntimeError extends RuntimeException {
    final Token token;
  
    RuntimeError(Token token, String message) {
       super(message);
       this.token = token;
    }
}
```

Our class tracks the token that identifies where in the user's code in the runtime error came from. This helps users know where to fix their code.

We need similar checking to the binary operators.

```
      case GREATER:
        checkNumberOperands(expr.operator, left, right);
        return (double)left > (double)right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left < (double)right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left <= (double)right;
      case MINUS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left - (double)right;
      case SLASH:
        checkNumberOperands(expr.operator, left, right);
        return (double)left / (double)right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);
        return (double)left * (double)right;
```

```
  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }
```

We evaluate both operands before checking the type of either.

Addition is the odd one out again since it is overloaded for numbers and strings. We just need to fail if neither of the two success cases match.

```
        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }

        throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings.");
```

Errors are now being thrown, we just need to catch them.

## 7.4 Hooking Up the Interpreter

The visit methods are the guts of the interpreter class, where the real work happens. We need to wrap a skin around them to interact with the rest of the program.

The Interpreter's public API:

```
  void interpret(Expr expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }
```

This takes in a syntax tree for an expression and evaluates it. `evaluate()` will then return an object for the result value. And `interpret()` converts that to a string a shows it to the user:

```
  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }
```

Since Java has both floating point and integer types, it wants you to know which one you're using. It tells you by adding an explicit .0 to integer-valued doubles. We don't need this, so we remove it.

### Reporting runtime errors

If a runtime rror is thrown while evaluating the expression, `interpret()` catches it. This lets us report the error to the user and continue on. Currently, all our error reporting code lives in the Lox class, so let's add this method to Lox:

```
  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }
```

We use the token from RuntimeError to tell the user which line of code produced the error. Eventually, it would be nice to show an entire call stack of how that code was executed.

`runtimeError()` sets this Lox field after showing the error:

`static boolean hadRuntimeError = false;`

which helps in the `runFile()` method:

`if (hadRuntimeError) System.exit(70);`

### Running the interpreter

We can now start using the Lox interpreter:

```
public class Lox {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
```

The field is static so successive calls to `run()` inside a REPL session reuse the same interpreter. This will help with global variable in the future.

Then, we replace the previous syntax tree printer in `run()` with:

`interpreter.interpret(expression);`

We can now scan, parse, and execute our language.

[Bare bones interpreter](https://craftinginterpreters.com/image/evaluating-expressions/skeleton.png)