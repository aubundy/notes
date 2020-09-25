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