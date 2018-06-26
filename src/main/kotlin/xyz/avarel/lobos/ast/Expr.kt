package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type

interface Expr {
    val type: Type
    val position: Section

    fun <R> accept(visitor: ExprVisitor<R>): R

//    fun ast(builder: StringBuilder, indent: String, isTail: Boolean) {
//        builder.append(indent).append(if (isTail) "└── " else "├── ").append(toString())
//    }
//
//    fun ast(label: String, builder: StringBuilder, indent: String, tail: Boolean) {
//        builder.append(indent).append(if (tail) "└── " else "├── ").append(label).append(':')
//
//        builder.append('\n')
//        ast(builder, indent + if (tail) "    " else "│   ", true)
//    }
}