package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ops.LogicalAndOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.parselets.BinaryParser

object OrParser: BinaryParser(Precedence.DISJUNCTION, true) {
    override fun parse(parser: Parser, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(precedence)

        return LogicalAndOperation(left, right, left.position.span(right.position))
    }
}