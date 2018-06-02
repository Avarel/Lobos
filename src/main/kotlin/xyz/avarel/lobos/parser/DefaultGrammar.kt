package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.parselets.*

object DefaultGrammar: Grammar(hashMapOf(), hashMapOf()) {
    init {
        prefix(TokenType.LET, LetParser)
        prefix(TokenType.INT, IntParser)
        prefix(TokenType.STRING, StringParser)
        prefix(TokenType.IDENT, IdentParser)
        prefix(TokenType.TRUE, BooleanParser(true))
        prefix(TokenType.FALSE, BooleanParser(false))
        prefix(TokenType.L_PAREN, TupleParser)

        infix(TokenType.PLUS, BinaryOperatorParser(Precedence.ADDITIVE, BinaryOperationType.ADD))
        infix(TokenType.MINUS, BinaryOperatorParser(Precedence.ADDITIVE, BinaryOperationType.SUBTRACT))
        infix(TokenType.ASTERISK, BinaryOperatorParser(Precedence.MULTIPLICATIVE, BinaryOperationType.MULTIPLY))
        infix(TokenType.F_SLASH, BinaryOperatorParser(Precedence.MULTIPLICATIVE, BinaryOperationType.DIVIDE))
    }

    fun prefix(type: TokenType, parselet: PrefixParser) {
        if (type in prefixParsers) {
            throw IllegalStateException("INTERNAL: attempted to override existing $type parselet")
        }
        prefixParsers[type] = parselet
    }

    fun infix(type: TokenType, parselet: InfixParser) {
        if (type in infixParsers) {
            throw IllegalStateException("INTERNAL: attempted to override existing $type parselet")
        }
        infixParsers[type] = parselet
    }
}