# A Map of the Territory
This book is about language implementation, not a language itself
## Parts of a language
Not much has changed about languages even though so much has changed about computers

![Implementation analogy image](http://craftinginterpreters.com/image/a-map-of-the-territory/mountain.png)

### Scanning/lexing 
A scanner takes a string of characters and chunks them together into words/tokens

### Parsing
This happens next and parsing groups tokens into larger expressions
It turns the tokens into a larger tree structure based off the grammar - abstract syntax tree AST
Will also tell us syntax errors

### Static analysis
Analyze the components of the AST 
Binding/resolution - take each identifier of the code and find out where it comes from
Scope is important here, static type checking and type errors (dynamic typing is different)
But we must store this analysis, sometimes in attributes that are apart of nodes in the syntax tree, lookup/symbol table, or to transform the tree entirely
This ends the “front end” - only concerned about the source language the user types
“Back end” - concerned about the final architecture the code runs on
“Middle end” - interface between these two

### Intermediate representation
Compilers basically organize the data one stage at a time in order to make the job easier for the next stage
Code will be stored in intermediate representation that isn’t tied to source or destination, this helps with using multiple languages/target platforms

### Optimization
Once we know what the program means, we can swap it out for one with the same semantics but optimized
Constant folding - evaluate an expression at compile time and replace the code with the result of the expression
Big part of programming language business

### Code generation
Last step is to generate the code so the machine can run it
Officially in the “back end”
Generate code for CPU or virtual machine?
Native code runs faster, but takes longer to generate
Speaking CPU language also ties you down to specific hardware
Bytecode - code generated for virtual/idealized machine

### Virtual Machine
If your compiler produces bytecode, you need to translate it into machine code
You can create a mini-compiler for this, reusing a lot of your previous architecture
Writing a virtual machine allows you to translate the bytecode more simply and portably, but it is slower because every instruction is simulated at runtime

### Runtime
We can now execute the code and run it
But we will need some additional services while the program runs
If we have automated memory management, we need a garbage collector to reclaim unused bits
If we have “instance of” test, we need a representation of the object to check the type
Fully compiled languages have their runtime directly inserted into the executable
Java, Python, and JS have a runtime that runs on a virtual machine or interpreter

## Short cuts and alternatives
Sometimes languages cut corners or compile differently

### Single-pass compilers
Combine parsing, analysis, and code generating to output code directly in the parser. But you can’t have any intermediate storage (syntax tree) and you can’t revisit early parts of the code 
Pascal and C have this limitation (you couldn’t even store an entire source file in memory)

### Tree-walk interpreters
Language begins executing code right after parsing it into an AST. It traverses the tree one node/branch at a time
Tends to be slow, useful for small/student projects 
Our first interpreter works this way

### Transpilers
Since “back end” takes a lot of work unless you already have an IR to target, you can treat another language as an intermediate representation. 
Instead of lowering the semantics to a primitive target language, we produce a string of valid code in another language and use its compiling resources

### Just-in-time compilation
Compile straight into machine, but difficult to do since everyone has different machines 
They insert profile hooks into generates code to see which regions are most performance critical and what data is flowing through them. Over time, these hotspots will get automatic recompiling optimizations

### Compilers and interpreters
What’s the difference between a compiler and interpreter? Is similar to asking the difference between a fruit and vegetable 
Compiling is an implementation technique that involves translating code to - usually - lower level code
Compilers don’t execute code, but interpreters take code and runs the program from source
￼
![Language Venn Diagram](http://craftinginterpreters.com/image/a-map-of-the-territory/venn.png)

Our second interpreter will live in the middle region too because it will internally compile to bytecode
