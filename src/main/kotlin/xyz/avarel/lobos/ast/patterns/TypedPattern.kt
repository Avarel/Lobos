package xyz.avarel.lobos.ast.patterns

import xyz.avarel.lobos.ast.types.TypeAST
import xyz.avarel.lobos.lexer.Section

class TypedPattern(val target: PatternAST, val type: TypeAST, position: Section) : AbstractPattern(buildString {
    append(target)
    append(": ")
    append(type)
}, position) {
    override fun <R> accept(visitor: PatternVisitor<R>) = visitor.visit(this)
}