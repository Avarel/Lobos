package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.lexer.Section

abstract class AbstractPattern(val name: String, override val position: Section) : Pattern {
    override fun toString() = name
}