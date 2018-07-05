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

### Union Types
// TODO

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

