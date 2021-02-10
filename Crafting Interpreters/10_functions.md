# Functions

Previous chapters really only supply a piece of a puzzle. This chapter will take those pieces, add a couple more, and assemble them all into support for real user-defined functions and function calls.

![Lambda](https://craftinginterpreters.com/image/functions/lambda.png)

## Function Calls

C-style function call syntax - 

`average(1, 2);`

The name of the function being called isn't actually part of the call syntax. The callee can be any expression that evaluates to a function. For example - 

`getCallback()();`

This has two call expressions. The first () has getCallback as its callee, and the second call has the entire getCallback() expression as its callee. Parentheses following an expression indicate a function call.

This "operator" has higher precedence than any other operator, even unary ones.

```
unary          → ( "!" | "-" ) unary | call ;
call           → primary ( "(" arguments? ")" )* ;
```

This rule matches a primary expression followed by zero or more function calls. If no parentheses, this parses a bare primary expression. 

The argument list grammar -

`arguments      → expression ( "," expression )* ;`

Arguments require at least one expression, followed by zero or more other expressions, each preceded by a comma. The call rule considers arguments optional.

GenerateAst.java - 

`      "Call     : Expr callee, Token paren, List<Expr> arguments",`

It stores the callee expression and a list of expressions for the arguments. It also stores the token for the closing parenthesis. We'll use that token's location when we report a runtime error caused by a function call.

Parser.java - unary() -

`return call();`

```
  private Expr call() {
    Expr expr = primary();

    while (true) { 
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN,
                          "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }
```

The code is moved around here to be a little cleaner. First we parse a primary expression, then for each ",", we call finishCall() to parse the call expression using the previously parse expression as the callee. The returned expression becomes the new `expr` and we loop to see if the result itself is called.

We first check if the next token is ")", if so, we don't try to parse any arguments. Otherwise we keep looking for commas after expressions. When we don't find a comma, we consume the closing parenthesis. Finally, we wrap the callee and those arguments up into a call AST node.

### Maximum argument counts

Right now, our parsing arguments loop has no bound. Languages take various approaches to limiting argument counts. C says to support at least 127 arguments; Java says to accept no more than 255. In order to have our two interpreters be compatible with each other, we will add the same limiter to both.

`finishCall()` - 

```
if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
```

This just reports an error instead of throwing an error since the parser won't be in a confused state if this happens.

### Interpreting function calls

`Interpreter.java` -  

```
import java.util.ArrayList;

...

@Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) { 
      arguments.add(evaluate(argument));
    }

    LoxCallable function = (LoxCallable)callee;
    return function.call(this, arguments);
  }
```

First we evaluate the expression for the callee. Then we evaluate each of the argument expressions in order and store the resulting values in a list.

Once the callee and arguments are ready, we just need to perform the call. We do this by casting the callee to a `LoxCallable` and then invoking `call()` on it.

`LoxCallable.java` - 

```
package com.craftinginterpreters.lox;

import java.util.List;

interface LoxCallable {
  Object call(Interpreter interpreter, List<Object> arguments);
}
```

We pass in the interpreter in case the class implementing `call()` needs it. Then we give the list of evaluated argument values here. The implementer's job is then to return the value that the call expression produces.

### Call type errors

We need to make the visit method a little more robust since it ignores a couple of failure modes. What if the callee isn't something we can call? (`"string"()`). JVM will throw a `ClassCastException`, but we want to throw our own exception type.

`visitCallExpr()` - 

```
    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren,
          "Can only call functions and classes.");
    }
```

### Checking arity

Arity in functions is determined by the number of parameters it declares.

```
fun add(a, b, c) {
  print a + b + c;
}
```

Calling `add()` with less than or greater than 3 arguments should be handled. JavaScript discards extra arguments. Python raises a runtime error if the argument list is too long or too short. We will take Python's approach.

`visitCallExpr()` - 

```
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }
```

`LoxCallable.java` - 

`int arity();`

We could push the arity checking into the concrete imiplementation of `call()`, but since multiple classes are implementing `LoxCallable`, that would end up with redundant validation spread across a few classes.

## Native Functions

We can now theoretically call functions, but we have no functions to call. Before we add user-defined functions, we need to add native functions. These are exposed to user code by the interreter, but implemented in the host language (Java for us). These are sometimes called primitives, external functions, or foreign functions.

Since these can be called while the user's program is running, they form part of the implementation's runtime.

These are key to making your language good at doing useful stuff since they provide access to the fundamental services that all programs are defined in terms of. 

Many languages also allow users to provide their own native functions. A foreign function interface makes this possible (also called native extension or native interface). We won't define a FFI for jlox, but we'll add one native function to get a taste.

### Telling time

We will care a lot more about performance in Part III. This will mean using benchmarks to measure the time it takes to exercise some corner of the interpreter.

We could measure the time it takes to start up the interpreter, run the benchmark, and exit, but that adds a lot of overhead like JVM startup time, OS stuff, etc.

Another solution is to have the benchmark script itself measure the time elapsed between two points in the code. So, Lox needs to be able to tell time.

We'll add a clock native function that returns the number of seconds that have passed since some fixed point in time.

`Interpreter.java` - 

```
final Environment globals = new Environment();
private Environment environment = globals;
```

The `environment` field in the interpreter changes as we enter and exit local scopes. It tracks the current environment. `globals` holds a fixed reference to the outermost globl environment. Then, we stuff the native function in the global scope -

```
  Interpreter() {
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() { return 0; }

      @Override
      public Object call(Interpreter interpreter,
                         List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() { return "<native fn>"; }
    });
```

This defines a variable named "clock" whose value is an anonymous class that implements `LoxCallable`. It takes no arguments. The implementation of `call()` calls the corresponding Java function and converts the result to a double value in seconds.

If we wanted to add other native functions like reading input from the user, working with files, etc., we could add them each as their own anonymous class that implements `LoxCallable`. But `clock()` is all we need for this book.