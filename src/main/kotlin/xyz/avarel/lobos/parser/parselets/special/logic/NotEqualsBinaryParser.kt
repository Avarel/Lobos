package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ops.LogicalNotOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence

object NotEqualsBinaryParser: InfixParser {
    override val precedence: Int get() = Precedence.EQUALITY

    override fun parse(parser: Parser, token: Token, left: Expr): Expr {
        val expr = EqualsBinaryParser.parse(parser, token, left)
        return LogicalNotOperation(expr, token.position.span(expr.position))
    }
}