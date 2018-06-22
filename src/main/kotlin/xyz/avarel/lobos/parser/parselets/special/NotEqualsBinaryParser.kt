package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.UnaryOperation
import xyz.avarel.lobos.ast.ops.UnaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object NotEqualsBinaryParser: InfixParser {
    override val precedence: Int get() = Precedence.EQUALITY

    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        val expr = EqualsBinaryParser.parse(parser, scope, stmt, token, left) as BinaryOperation

        val tmp = stmt.assumptions
        stmt.assumptions = stmt.inverseAssumptions
        stmt.inverseAssumptions = tmp

        return UnaryOperation(expr.type, expr, UnaryOperationType.NOT, token.position)
    }
}