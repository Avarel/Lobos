package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ops.EqualsOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.span
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence

object EqualsBinaryParser: InfixParser {
    override val precedence: Int get() = Precedence.EQUALITY

    override fun parse(parser: Parser, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(precedence)

        return EqualsOperation(left, right, left.span(right))
    }
}