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

/*
        let y = "hello";
        let x: str = y + 2;
        y = "world";
        let z: "world" = y;
 */

fun main(args: Array<String>) {
    val source = """
let unit: () = ();
let tuple: (i32, i64, str) = (1, 2, "3");
let other: ("up" | "down", i32) = ("up", 6);
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
        parser.errors.forEach {
            println(it.message)
        }
        if (parser.errors.isEmpty()) {
            println("None :)")
        }

        println(buildString { ast.accept(ASTViewer(this, "", true)) })
    }
//
//    println("\n BASE TUPLE")
//    val base = TupleType(listOf(TupleType(listOf(A, B)), AnyType, A, LongType))
//    println("$base            ${base.genericTypes}")
//
//    println("\n APPLY INT AND LONG")
//    println(base.template(listOf(IntType, LongType)))
//
//    //val fn = FunctionType(listOf(GenericParameter("A"), GenericParameter("B")), listOf(A, TupleType.Unit, B), TupleType(listOf(A, B)))
//    val fn = FunctionType(listOf(GenericParameter("A")), emptyList(), FunctionType(emptyList(), A))
//
//
//    println("\n BASE GENERIC FUNC")
//    println(fn)
//
//    println("\n APPLY INT AND LONG")
//    println(fn.template(listOf(IntType)))

//    println(base.template(listOf(LongType)).isAssignableFrom(base.template(listOf(IntType))))
    //println("whytho${Position("hello_world.lbs", 1, 2)}")
}