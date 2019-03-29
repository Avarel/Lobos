package xyz.avarel.lobos.ast.patterns

import xyz.avarel.lobos.lexer.Section

class VariablePattern(val mutable: Boolean, val name: String, position: Section) : AbstractPattern(buildString {
    if (mutable) {
        append("mut ")
    }
    append(name)
}, position) {
    override fun <R> accept(visitor: PatternVisitor<R>) = visitor.visit(this)
}