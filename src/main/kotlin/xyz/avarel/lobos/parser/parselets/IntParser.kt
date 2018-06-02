package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.IntExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.typesystem.scope.ParserContext

object IntParser: PrefixParser {
    override fun parse(parser: Parser, scope: ParserContext, token: Token): Expr {
        val value = token.string!!.toInt()
        return IntExpr(value, token.position)
    }
}
