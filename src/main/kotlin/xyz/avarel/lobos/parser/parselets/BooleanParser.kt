package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.BooleanExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.typesystem.scope.ParserContext

class BooleanParser(private val value: Boolean): PrefixParser {
    override fun parse(parser: Parser, scope: ParserContext, token: Token): Expr {
        return BooleanExpr(value, token.position)
    }
}