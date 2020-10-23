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

### Detecting runtime errors

