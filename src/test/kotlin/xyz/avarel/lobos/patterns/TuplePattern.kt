package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.lexer.Section

class TuplePattern(val list: List<Pattern>, position: Section) : AbstractPattern(list.joinToString(prefix = "(", postfix = ")"), position) {
    override fun <R> accept(visitor: PatternVisitor<R>) = visitor.visit(this)
}