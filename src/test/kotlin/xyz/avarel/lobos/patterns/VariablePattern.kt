package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.ast.types.TypeAST
import xyz.avarel.lobos.lexer.Section

class VariablePattern(val mutable: Boolean, name: String, val type: TypeAST?, position: Section) : AbstractPattern(buildString {
    if (mutable) {
        append("mut ")
    }
    append(name)
    if (type != null) {
        append(": ")
        append(type)
    }
}, position) {
    override fun <R> accept(visitor: PatternVisitor<R>) = visitor.visit(this)
}