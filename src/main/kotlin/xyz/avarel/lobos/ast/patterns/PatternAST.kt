package xyz.avarel.lobos.ast.patterns

import xyz.avarel.lobos.lexer.Sectional
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.parseTypeAST

interface PatternAST : Sectional {
    fun <R> accept(visitor: PatternVisitor<R>): R
}

fun Parser.parsePattern(): PatternAST {
    val pattern = parseSinglePattern()

    return if (match(TokenType.COLON)) {
        val type = parseTypeAST()
        TypedPattern(pattern, type, pattern.span(type))
    } else {
        pattern
    }
}

fun Parser.parseSinglePattern(): PatternAST {
    return when {
        match(TokenType.UNDERSCORE) -> WildcardPattern(last.section)
        match(TokenType.INT) -> I32Pattern(last.string.toInt(), last.section)
        match(TokenType.STRING) -> StrPattern(last.string, last.section)
        match(TokenType.L_PAREN) -> {
            val lParen = last.section
            val list = mutableListOf<PatternAST>()
            if (!match(TokenType.R_PAREN)) {
                do {
                    list += parsePattern()
                } while (match(TokenType.COMMA))
                eat(TokenType.R_PAREN)
            }
            TuplePattern(list, lParen.span(last.section))
        }
        match(TokenType.IDENT) -> VariablePattern(false, last.string, last.section)
        match(TokenType.MUT) -> {
            val mutToken = last
            VariablePattern(true, eat(TokenType.IDENT).string, mutToken.span(last))
        }
        else -> throw SyntaxException("Expected pattern", peek().section)
    }
}