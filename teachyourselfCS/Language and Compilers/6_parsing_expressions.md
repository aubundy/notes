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