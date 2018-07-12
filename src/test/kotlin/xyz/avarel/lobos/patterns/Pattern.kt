package xyz.avarel.lobos.patterns

import xyz.avarel.lobos.lexer.Sectional
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException

interface Pattern : Sectional {
    fun <R> accept(visitor: PatternVisitor<R>): R
}

fun Parser.parsePattern(): Pattern {
    return parseSinglePattern()
}

fun Parser.parseSinglePattern(): Pattern {
    return when {
        match(TokenType.UNDERSCORE) -> WildcardPattern(last.section)
        match(TokenType.INT) -> I32Pattern(last.string.toInt(), last.section)
        match(TokenType.STRING) -> StrPattern(last.string, last.section)
        match(TokenType.L_PAREN) -> {
            val lParen = last.section
            val list = mutableListOf<Pattern>()
            if (!match(TokenType.R_PAREN)) {
                do {
                    list += parsePattern()
                } while (match(TokenType.COMMA))
                eat(TokenType.R_PAREN)
            }
            TuplePattern(list, lParen.span(last.section))
        }
        else -> throw SyntaxException("Expected pattern", peek().section)
    }
}