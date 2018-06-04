package xyz.avarel.lobos

import xyz.avarel.lobos.ast.ASTViewer
import xyz.avarel.lobos.lexer.Tokenizer
import xyz.avarel.lobos.parser.DefaultGrammar
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.typesystem.scope.DefaultParserContext

/* Smart Compiler
let y: 1|3|5|7|"string" = "string";
let x: String = y
 */ // The compiler should remember that y is effectively "string"
    // so the assignment to x should be legal


// TODO extern let PI: i32
// TODO extern impl i32

/*
        let y = "hello";
        let x: str = y + 2;
        y = "world";
        return 3;
        let z: "world" = y;
 */

/*
        let a: i32 | null = 3;
        if a == null {
            let b: null = a;
            return ();
        } else {
            let b: i32 = a;
        };
        let b: 3 = a;
 */

fun main(args: Array<String>) {
    val source = """
        let a: any!null = null;
    """.trimIndent()

    val lexer = Tokenizer(reader = source.reader())
    lexer.parse().let {
//        println()
//        println("|> TOKENS:")
//        it.forEach(::println)

        println()
        println("|> SOURCE:")
        println(source)

        val parser = Parser(DefaultGrammar, it)
        val ast = parser.parse(DefaultParserContext.subContext())

        println()
        println("|> ERRORS:")

        val lines =  source.lines()
        parser.errors.forEach {
            val line = lines[it.position.lineNumber.toInt() - 1]
            val msg = buildString {
                append(line)
                append('\n')
                kotlin.repeat(it.position.lineIndex.toInt()) {
                    append(' ')
                }
                append("└── ")
                append(it.message)
            }
            println(msg)
        }

        if (parser.errors.isEmpty()) {
            println("None :)\n")
        }

        println()
        println("|> AST")
        println(buildString { ast.accept(ASTViewer(this, "", true)) })
    }
}