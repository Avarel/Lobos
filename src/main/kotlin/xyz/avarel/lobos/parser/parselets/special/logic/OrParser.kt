package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.continuableTypeCheck
import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.parser.parselets.BinaryParser
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object OrParser: BinaryParser(Precedence.DISJUNCTION, true) {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        parser.continuableTypeCheck(BoolType, left.type, left.position)

        val newCtx = StmtContext(BoolType)
        newCtx.assumptions.putAll(stmt.inverseAssumptions)

        val right = parser.parseExpr(scope, newCtx, precedence)

        parser.continuableTypeCheck(BoolType, right.type, right.position)

        if ((stmt.assumptions.keys + newCtx.assumptions.keys).size == 1) {
            // can only assume if it is only one variable
            stmt.assumptions.mergeAll(newCtx.assumptions, Type::commonAssignableToType)
        } else {
            stmt.assumptions.clear()
        }

        stmt.inverseAssumptions.mergeAll(newCtx.inverseAssumptions, Type::commonAssignableFromType)

        return LogicalAndOperation(left, right, left.position.span(right.position))
    }
}