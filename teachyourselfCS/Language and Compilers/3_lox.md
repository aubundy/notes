# The Lox Language

## Hello, Lox

```
// Your first Lox program!
print “Hello, world!”
```

Lox uses C syntax. Pascal and Smalltalk are more elegant, Scheme is more minimalist.

## A High-Level Language

Lox is most similar to JavaScript, Scheme, and Lua.

### Dynamic Typing

it is dynamically typed, this gives us  a simpler language and shorter book.

### Automatic memory management

High-level languages exist to eliminate error-prone low-level language needs. 

There are two main techniques in managing memory: reference counting and tracing garbage collection “GC”. Ref counters are easier to implement (Python and PHP started out using them), but they have limitations. GC is hard, but we’ll be building one. 

## Data Types

- booleans
    - `true` and `false`
- numbers
    - double-precision floating point
    - basic integers and decimal literals
- strings
    - double quotes
- `nil` (null)
    - statically-typed languages might not need it, but dynamically-typed ones do

## Expressions

- arithmetic
    - `+ - / *`
- comparison and equality
    - `< <= > >=`
    - `== !=`
    - no implicit conversions
- logical operators
    - `!` `and` `or `
- precedence and grouping
    - `()`
    - no bitwise, shift, modulo, or conditional operators

## Statements

expressions produce values, statements produces effects

- `print ;` (expression statement)

## Variables

- `var`

## Control flow

- `if else`
- `while`
- `for`

## Functions

- `functionName();`
- `fun functionName() {}`
- function calls have arguments, function declarations have parameters
- functions that don’t return anything return nil

## Closures

you can declare functions inside other functions
you can access a variable from an inner function declared outside that function

## Classes

Right now, this make Lox halfway to an OOP language, and halfway to a functional language

### Why might a language want to be object oriented?

they’re still really good and helpful even though there was some overkill in the 90s
objects are handy in dynamically-typed languages
methods are very helpful to keep code DRY

### Why is Lox object-oriented?

Since we used OOP concepts all the time, we need to know how they work

### Classes or prototypes?

classes have two main concepts: instances and classes
- instances store the state for each object and have a reference to its class
- classes have the methods and inheritance chain
- in order to call a method on an instance, you first lookup the class, then call the method

prototype-based languages merge these two. No classes, all are objects, and the object may contain state and methods as well as inherit (delegate) from each other. You can do cool stuff with this, but most people just try to recreate classes. It might be because they add more complexity to the user even though they are simpler in the language to create.

We will bake classes into Lox

### Classes in Lox

- `class Name() {}`
- classes are ‘first class’ in Lox
- create instances by calling a class like a function

### Instantiation and initialization

- you can freely add properties onto objects like other dynamically-typed languages
- `this` works to access fields or methods
- `init()` is called automatically when the object is constructed, and parameters passed to the class are forwarded to the initializer

### Inheritance

- supports single inheritance using `<` so you can reuse methods across multiple classes/objects
- `super` allows you to call a method on our own instance without hitting our own methods
- not a pure OOP language because the objects are not instances of a class

## The Standard Library

- `print` to demonstrate code is running
- `clock()` to time our code
- string manipulation, trigonometric functions, file I/O, networking, reading input from the user would make this closer to a real language