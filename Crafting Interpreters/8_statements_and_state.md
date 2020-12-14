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