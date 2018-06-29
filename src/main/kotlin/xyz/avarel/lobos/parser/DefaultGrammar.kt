package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.expr.ops.BinaryOperationType
import xyz.avarel.lobos.ast.expr.ops.UnaryOperationType
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.parselets.BinaryOperatorParser
import xyz.avarel.lobos.parser.parselets.IfParser
import xyz.avarel.lobos.parser.parselets.UnaryOperatorParser
import xyz.avarel.lobos.parser.parselets.declarations.*
import xyz.avarel.lobos.parser.parselets.nodes.*
import xyz.avarel.lobos.parser.parselets.special.DotParser
import xyz.avarel.lobos.parser.parselets.special.InvocationParser
import xyz.avarel.lobos.parser.parselets.special.ReturnParser
import xyz.avarel.lobos.parser.parselets.special.logic.*

object DefaultGrammar: Grammar(hashMapOf(), hashMapOf()) {
    init {
        prefix(TokenType.INT, IntParser)
        prefix(TokenType.DECIMAL, DecimalParser)

        prefix(TokenType.MOD, ModuleParser)
        prefix(TokenType.STRING, StringParser)
        prefix(TokenType.IDENT, IdentParser)
        prefix(TokenType.TRUE, BooleanParser(true))
        prefix(TokenType.FALSE, BooleanParser(false))
        prefix(TokenType.L_PAREN, TupleParser)
        prefix(TokenType.RETURN, ReturnParser)
        prefix(TokenType.IF, IfParser)
        prefix(TokenType.NULL, NullParser)

        prefix(TokenType.LET, LetParser)
        prefix(TokenType.TYPE, TypeAliasParser)
        prefix(TokenType.DEF, FunctionParser)
        prefix(TokenType.EXTERNAL, ExternalParser)
        prefix(TokenType.BANG, NotUnaryParser)

        prefix(TokenType.PLUS, UnaryOperatorParser(UnaryOperationType.POSITIVE))
        prefix(TokenType.MINUS, UnaryOperatorParser(UnaryOperationType.NEGATIVE))

        infix(TokenType.DOT, DotParser)
        infix(TokenType.L_PAREN, InvocationParser)
        infix(TokenType.EQ, EqualsBinaryParser)
        infix(TokenType.NEQ, NotEqualsBinaryParser)
        infix(TokenType.PLUS, BinaryOperatorParser(Precedence.ADDITIVE, BinaryOperationType.ADD))
        infix(TokenType.MINUS, BinaryOperatorParser(Precedence.ADDITIVE, BinaryOperationType.SUBTRACT))
        infix(TokenType.ASTERISK, BinaryOperatorParser(Precedence.MULTIPLICATIVE, BinaryOperationType.MULTIPLY))
        infix(TokenType.F_SLASH, BinaryOperatorParser(Precedence.MULTIPLICATIVE, BinaryOperationType.DIVIDE))

        infix(TokenType.AND, AndParser)
        infix(TokenType.OR, OrParser)
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