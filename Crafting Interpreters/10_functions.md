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
```

The code is moved around here to be a little cleaner. First we parse a primary expression, then for each ",", we call finishCall() to parse the call expression using the previously parse expression as the callee. The returned expression becomes the new `expr` and we loop to see if the result itself is called.

We first check if the next token is ")", if so, we don't try to parse any arguments. Otherwise we keep looking for commas after expressions. When we don't find a comma, we consume the closing parenthesis. Finally, we wrap the callee and those arguments up into a call AST node.