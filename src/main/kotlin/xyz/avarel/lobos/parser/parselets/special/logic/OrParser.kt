package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.continuableTypeCheck
import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.parser.parselets.BinaryParser
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
            stmt.assumptions.mergeAll(newCtx.assumptions) { v1, v2 ->
                v1.copy(type = v1.type.commonAssignableToType(v2.type))
            }
        } else {
            stmt.assumptions.clear()
        }

        stmt.inverseAssumptions.mergeAll(newCtx.inverseAssumptions) { v1, v2 ->
            v1.copy(type = v1.type.commonAssignableFromType(v2.type))
        }

        return LogicalAndOperation(left, right, token.position)
    }
}