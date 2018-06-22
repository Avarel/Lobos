# The Lobos Project
This project aims to build a smart-ass static analysis lexer and parser.
**It's not meant to be used as a library, but rather, it's meant to be used
as a point of reference (at least for me) to build future parsers.**
If you're an onlooker who is interested in copying this lexer & parser code,
feel free to do so. Keep in mind that this implementation is specialized, so 
you would have to change up more than a few classes if you want to parse your
language. The last thing you need for your parser is to be riddled in generic
hell.

## Example Features

### String Templating
The Lobos compiler desugars string templating at the lexer phase.
```
"$variable is cool."    ->      "" + variable + " is cool."
"${1 + 2} is cool."     ->      "" + (1 + 2) + " is cool."
```
Disabling the initial empty string only requires changing a few lines of code.

### Tuple Types
Tuples are basically unnamed aggregates of values.
```
let unit: () = ();
let tuple: (i32, i64, str) = (1, 2, "3");
let other: ("up" | "down", i32) = ("up", 6);
```

### Literal Types and Union Types
I'm a big fan of literal types from TypeScript, so naturally, I tried to add them.
``` 
let a: "one" = "one";
let b: "two" = a; // this will error out because "two" is incompatible with "one".
```
Combine them with union types and you can have a cool enum like structure.
```
let direction: "up" | "down" | "left" | "right" = "up";
direction = "left";
direction = "right";
direction = "up";
direction = "down";
```
All of those assignments are legal, but try to assign any other string 
and you will get an error.
```
direction = "sideways";     // ERROR! :(
```

### Literal Inferences
Type inference is cool. Literal types are cool. But when the compiler infers
an expression, what type should it use? Lobos offers a compromise. You can
declare a variable to be of a universal types (such as str or i32), but if applicable,
Lobos will assume the literal type of the variable.
```
let y = "hello";        // Formally, y is of type str
y = "world";            // which makes this assignment legal     
let z: "world" = y;     // but Lobos will remember that y is of type "world"
                        // so even if y is formally str, this declaration
                        // is legal.
```

### AST Viewer
Parse this code with `Tokenizer.kt` and `Parser.kt`:
```
let y = "hello";
let x: str = y + 2;
y = "world";
let z: "world" = y;
```
Then visit it with `ASTViewer.kt` and you will get:
```
├── let y
│   └── "hello"
├── let x
│   └── binary ADD
│       ├── variable y
│       └── 2
├── assign y
│   └── "world"
└── let z
    └── variable y
```

### Invalid Tokens and Expressions
Invalid expressions will not crash and stop parsing progress.
```
let a: i32 = ?;
!;
let b = 12;
```
The code above generate the following AST: (given that ? and ! are non-valid expressions)
```
├── [Invalid expression.]
├── [Invalid expression.]
└── let b
    └── 12
```
Similarly, if the lexer does not recognize a character, it will simply return
an invalid token instead of throwing an exception.

### Generics
Generics parameters is supported. Erasure or type templating is dependent on AST transformation.

