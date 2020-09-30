# Parsing Expressions

## Ambiguity and the Parsing Game

To parse, we accept a string (series of tokens) and map the tokens to terminals in the grammar to figure out which rules could have created that string. “Could have” is on purpose, because it is possible to create a grammar that is ambiguous, meaning multiple rules could create the same string. When generating a string, it doesn’t matter how you got it, but it does matter when it comes to parsing. It may mean your parser can misunderstand the user’s code.

Here’s the Lox grammar from last chapter:

```
expression → literal
           | unary
           | binary
           | grouping ;

literal    → NUMBER | STRING | "false" | "true" | "nil" ;
grouping   → "(" expression ")" ;
unary      → ( "-" | "!" ) expression ;
binary     → expression operator expression ;
operator   → "==" | "!=" | "<" | "<=" | ">" | ">="
           | "+"  | "-"  | "*" | "/" ;
```

![6 / 3 - 1](http://craftinginterpreters.com/image/parsing-expressions/tokens.png)

Depending on our grammar’s level of ambiguity, our parser could produce two different syntax trees:

![Different syntax trees](http://craftinginterpreters.com/image/parsing-expressions/syntax-trees.png)

Precedence (which operator is evaluated first in an expression with a mixture of operators) and associativity (which operator is evaluated first in a series of the same operators) are very important. Operators are left-associative (operators on the left are evaluated before the right). Assignment is right-associative. We’ll use the same precedence rules as C. 

We will create a different rule for each precedence level (some will be recursive to handle `!!` or `1 * 2 / 3`):

```
expression     → equality
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | primary ;
primary        → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;
```

Each level matches itself and the levels above it. `primary` covers literals and parenthesized grouping expressions. This grammar is more complex, but eliminates some ambiguity.

## Recursive Descent Parsing

There are multiple parsing techniques we could use, but we'll stick with recursive descent (V8 uses this).

It is considered top-down because it starts at the outermost rule (`expression` in our case), and walks down the nested subexpressions before reaching the leaves of the syntax tree. It is considered recursive since the grammar rules map 1-1 to imperative code.

### The parser class

Each rule becomes a method inside this class:

```
package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }
}
```

It consumes a sequence like the scanner, but now we're working with whole tokens. 

The first rule `expression` simply expands into the `equality` rule.

```
  private Expr expression() {
    return equality();
  }
```

Each method for parsing a grammar rule produces a syntax tree for that rule and returns it to the caller. If it contains a nonterminal, like a reference to another rule, we call that rule's method.

`equality → comparison ( ( "!=" | "==" ) comparison )* ;` becomes:

```
  private Expr equality() {
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
```

Add a `match` and `check` method to end the while loop inside the `equality` method. Check if the current token is of a certain type.

```
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }
```

`advance()` will consume the token and return it.

```
  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
```

`isAtEnd()` checks if we've run out of tokens to parse. `peek()` returns the current token we've yet to consume and `previous()` returns the most recently consumed token.

So if we are inside the `while` loop in the `equality()` function, then we know we've run across `!=` or `==`. It grabs that operator with `previous()` then calls `comparison()` again to parse the right-hand operand. It then combines the operator and two operands into a new `Expr.Binary` syntax tree node, and then loops around, storing the expression back in the same `expr` variable.

[Parsing a == b == c == d == e](http://craftinginterpreters.com/image/parsing-expressions/sequence.png)

The parser falls out of the loop once it hits a token that's not an equality operator. When that happens, it returns the expression. If there's never an equality operator, `comparison()` is returned.

`comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;` in Java becomes:

```
  private Expr comparison() {
    Expr expr = addition();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
```

The grammar rule and corresponding code is virtually the same as `equality()`. The only difference are the token types we look for with `match()` and the method we call for operands.

The last two binary operator rules follow the same pattern:

```
  private Expr addition() {
    Expr expr = multiplication();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr multiplication() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
```

Now to the unary operators - `unary → ( "!" | "-" ) unary | primary ;`

The code looks a little different:

```
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }
```

If the current token is '!' or '-', we must have a unary expression. If so, grab the token, and recursively call `unary()` to grab the operand. We're finished once we wrap it in a unary expression syntax tree.

That brings us to primary expressions. `primary → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;`

```
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
  }
```

It's pretty straitforward, except when it comes across '('. If it doesn't find a corresponding ')', it's an error.

## Syntax Errors

A parser has two jobs:

1. Given a valid sequence of tokens, produce a corresponding syntax tree.
2. Detect any errors and tell the user if there the sequence of tokens is not valid.

When the user doesn't know the syntax is wrong, it is up to the parser to tell them so. The way your parser returns errors is a big part of it's user interface. But good error-handling is hard since we can't read the user's mind.

We have to detect and report the error so the incorrect syntax tree doesn't go on to the interpreter.

It must not crash or hang. Even though it isn't valid code, it is still valid to the parser, since the users use the parser to learn what syntax is allowed.

We also must be fast. Parsing doesn't take a whole coffee break, but the users do expect to parse and reparse as they type.

We also must report each distinct error all at once. It would be really frustrating fixing an error, only to see a chain of others one after the other. But we also must minimize cascading errors. We don't want ten errors to appear and be fixed by one thing. This would scare users into thinking their code is worse than it is.

### Panic mode error recovery

One recovery technique that has stood the test of time is called 'panic mode'. As soon as it detects one error, the parser enters panic mode.

It then trys to get its state and the oncoming tokens aligned so they still make sense to the parser's grammar rules. This process is called synchronization.
To synchronize, we will pick one rule in the grammar rules as the synchronization point. The parser will fix its state by jumping out of any nested productions until it gets back to that rule. Then it synchronizes the token stream by discarding tokens until they match that rule. 
This produces a trade off of not reporting any actual errors or cascaded errors among those discarded tokens.

Synchronizing usually takes place between statements, but since we don't have those yet, we'll just set up the code for later use.

### Entering panic mode

```
  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
```

`consume()` is similar to `match()` in that it checks to see if the next token is of the expected type, if not, it throws an error.

```
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }
```

In Lox.java, we add:

```
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
```

This reports an error at a given token. It shows the token and its location. Inside `error()`, it returns

`private static class ParseError extends RuntimeException {}`

so that the caller can decide whether to unwind or not.

### Synchronizing a recursive descent parser

With recursiv descent, we use Java's own call stack to know what the parser is doing. In order to reset the state in the case of an error, we need to reset the call stack. 

We'll throw a ParseError object and catch it in the statement boundaries. Then we just need to synchronize the tokens.
We want to discard tokens until we're at the next statement. ';' usually end a statement and a keyword usually begins a statement, so we'll look for those.

```
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
```

## Wiring up the Parser

If we get all the way to the `primary()` rule and no cases match, we need to handle that error:

`throw error(peek(), "Expect expression.");`

Then we write an initial method to call our parser:

```
  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }
```

This handles a single expression (it will handle whole statements later) and returns it. Since syntax error recovery is the parser's job, we don't want the ParseError to go on to the interpreter.

Now, we connect the parser to the Lox class inside `run()`- 

```
    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;

    System.out.println(new AstPrinter().print(expression));
```