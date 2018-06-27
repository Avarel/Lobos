package xyz.avarel.lobos.parser.parselets.special.logic

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.parser.parselets.BinaryParser
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object AndParser: BinaryParser(Precedence.CONJUNCTION, true) {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token, left: Expr): Expr {
        parser.safe { typeCheck(BoolType, left.type, left.position) }

        val newCtx = StmtContext(BoolType)
        newCtx.assumptions.putAll(stmt.assumptions)

        val right = parser.parseExpr(scope, newCtx, precedence)

        parser.safe { typeCheck(BoolType, right.type, right.position) }

        stmt.assumptions.mergeAll(newCtx.assumptions, Type::commonAssignableFromType)

        if ((stmt.inverseAssumptions.keys + newCtx.inverseAssumptions.keys).size == 1) {
            // cant trust any assumptions about outside if depends on multiple variables
            stmt.inverseAssumptions.mergeAll(newCtx.inverseAssumptions, Type::commonAssignableToType)
        } else {
            stmt.inverseAssumptions.clear()
        }


        return LogicalAndOperation(left, right, left.position.span(right.position))
    }
}