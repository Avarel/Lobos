package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.typesystem.scope.ParserContext

interface InfixParser {
    val precedence: Int
    fun parse(parser: Parser, scope: ParserContext, token: Token, left: Expr): Expr
}