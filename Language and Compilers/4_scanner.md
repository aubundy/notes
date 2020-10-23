# Scanning

## The Interpreter Framework

Lox is a scripting language, so it executes directly from source. One way to run code is to start jlox from the command line and give it a path to a file to read and execute:

```
private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
  }
```

The other way to run code is to do it interactively. Fire up jlox with no arguments, and you can enter and execute code one line at a time.

```
private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
    }
  }
```

`readLine()` function reads a line of input from the user on the command line and returns the result.

Both prompt and file runner wrap this core function:

```
private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // For now, just print the tokens.
    for (Token token : tokens) {
      System.out.println(token);
    }
  }
```

It doesn’t interpret anything yet, but it prints out the tokens.

### Error handling

Languages that are usable handle errors gracefully

We need to give the user all of the information they need to understand what went wrong and guide them gently back to where they are trying to go. Lox will be pretty bare bones (no interactive debuggers, static analyzers, etc.)

```
static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
```

this tells user that some type of syntax error occurred on a specific line. It would be more helpful to also tell them where in the line the error occurred.

It’s good engineering practice to separate the code that generates the errors from the code that reports them. Ideally we would have a type of “ErrorReporter” interface that gets passed to the scanner and parser so we can swap out different reporting strategies.

## Lexemes and Tokens

There are 5 lexemes in this line of Lox code. Lexemes bundled with other data results in tokens.

`var language = "lox”;`

### Token type

Parsers look for keywords, operators, punctuation, and literal types

### Literal value

numbers, strings, etc. The scanner walks through each character, so it can convert it to the real runtime value that will be used by the interpreter later.

### Location information

just like with tracking errors, we want to keep track of where tokens are in the code

```
package com.craftinginterpreters.lox;

class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line; 

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
```

## Regular Languages and Expressions

Starting at the first character of the source code, it figures out what lexeme it belongs to, and consumes it and any following characters that are a part of that lexeme. At the end of that lexeme, it produces a token. Then it loops back and does it all over again. (Similar to regex)

![Lexical analygator](http://craftinginterpreters.com/image/scanning/lexigator.png)

## The Scanner Class

```
package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  Scanner(String source) {
    this.source = source;
  }
}
```

Store the raw source code as a simple string, and have a list ready to fill with the tokens we generate.

```
List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
}
```

This is the scanner’s loop. It loops until it is out of character, then it appends “end of file” token. It is dependent on a few fields to keep track of where in the source code we are:

```
private int start = 0;
private int current = 0;
private int line = 1;
```

This helper function tells use if we’ve run through all the characters:

```
private boolean isAtEnd() {
  return current >= source.length();
}
```

## Recognizing Lexemes

```
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 
    }
  }
```
scan a single token at the start of the loop. if they are a single character, pick a token type for it and consume the next character.

We also need some helper methods to consume next character and return it, inputs, and outputs.

```
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
```

### Lexical errors

If there are characters Lox doesn’t use, they get silently discarded

```
default:
        Lox.error(line, "Unexpected character.");
        break;
```

We keep scanning even if there’s an error since there may be more. It would be a good experience for the user if we combine all invalid character errors into a single error. `hadError` will prevent us from executing any of the bad code.

### Operators

Certain operators need a two-character combo.

```
case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
```

These need a new helper method:

```
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
```

This only consumes the current character if it is what we’re looking for. (like a conditional advance)

## Longer Lexemes

the `/` operator needs a special case

```
case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;
```

If we find a second `/`, we need to keep consuming characters until we reach the end of the line.

Another helper:

```
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
```

like advance, except it doesn’t consume the character (called lookahead). The shorter the lookahead, the faster the scanner. We have a 1-character lookahead (most popular languages only have 1 or 2-character lookahead).

Even though comments are lexemes, the parser doesn’t need them, so we don’t call `addToken()` at the end of the comment. 

```
case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;
```

With whitespace, we go back to the beginning of the san loop. A new lexeme starts after the whitespace character

### String literals

Let’s tackle strings first since they always start with “”.

`case '"': string(); break;`

```
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    // Unterminated string.
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
```

Consume characters until it hits the “ that ends the string. It also reports an error for never seeing a second “. It also supports multi-line strings. (increment `line` when we see a new line)

We also produce the actual string value that will be used by the interpreter later. We just need `substring()` to strip off the surrounding quotes.

### Number literals

numbers in Lox are floating point at runtime, but it supports integer and decimal literals. No leading or trailing decimal point allowed.

Look for digits, but don’t build a case for each one

```
      default:
        if (isDigit(c)) {
          number();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
```

This needs the following helpers:

```
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  } 
```

```
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }
```

Once we know we have a number, we use a separate method to consume the rest of the literal. (like with strings)

```
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
```

Here we look 2 characters ahead to see if there’s a number after the decimal. We then use Java’s `Double` type to produce a value for the numbers.

## Reserved Words and Identifiers

We now need to implement identifiers and reserved words in our scanner. Maximal munch principle says whenever two lexical grammar rules can both match a chunk of code the scanner is looking at, whichever one matches the most characters wins.

Assume any lexeme starting with a letter or underscore is an identifier

```
      default:
        if (isDigit(c)) {
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
```

```
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    addToken(IDENTIFIER);
  }
```

```  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
```

To handle keywords, we see if the identifier’s lexeme is one of the reserved words. We’ll use a token type specific to that keyword, saved in a Map.

```
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }
```

In `identifier()`, check if our identifier matches a keyword

```
    // See if the identifier is a reserved word.
    String text = source.substring(start, current);

    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
```

Now we have a working scanner.
