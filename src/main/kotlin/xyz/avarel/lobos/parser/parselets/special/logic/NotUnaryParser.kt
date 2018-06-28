package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ops.LogicalNotOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.span
import xyz.avarel.lobos.parser.Modifier
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser

object NotUnaryParser: PrefixParser {
    override fun parse(parser: Parser, modifiers: List<Modifier>, token: Token): Expr {
        val expr = parser.parseExpr()
        return LogicalNotOperation(expr, token.span(expr))
    }
}