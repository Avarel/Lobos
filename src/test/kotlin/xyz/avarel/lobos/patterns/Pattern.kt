package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.lexer.Positional
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException

interface Pattern : Positional {
    fun <R> accept(visitor: PatternVisitor<R>): R
}

fun Parser.parsePattern(): Pattern {
    return parseSinglePattern()
}

fun Parser.parseSinglePattern(): Pattern {
    return when {
        match(TokenType.UNDERSCORE) -> WildcardPattern(last.position)
        match(TokenType.INT) -> I32Pattern(last.string.toInt(), last.position)
        match(TokenType.STRING) -> StrPattern(last.string, last.position)
        else -> throw SyntaxException("Expected pattern", peek().position)
    }
}