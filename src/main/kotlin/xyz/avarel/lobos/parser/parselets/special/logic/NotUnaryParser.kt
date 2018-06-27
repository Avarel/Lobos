package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.LogicalNotOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.safe
import xyz.avarel.lobos.parser.typeCheck
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object NotUnaryParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val expr = parser.parseExpr(scope, stmt)

        parser.safe { typeCheck(BoolType, expr.type, expr.position) }

        val tmp = stmt.assumptions
        stmt.assumptions = stmt.inverseAssumptions
        stmt.inverseAssumptions = tmp

        return LogicalNotOperation(expr, token.position.span(expr.position))
    }
}