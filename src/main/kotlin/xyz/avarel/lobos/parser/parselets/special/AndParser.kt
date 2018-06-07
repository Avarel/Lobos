package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.mergeAll
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.continuableTypeCheck
import xyz.avarel.lobos.parser.parselets.BinaryParser
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object AndParser: BinaryParser(Precedence.CONJUNCTION, true) {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        parser.continuableTypeCheck(BoolType, left.type, left.position)

        val newCtx = StmtContext(ctx.mustBeExpr)
        newCtx.assumptions += ctx.assumptions

        val right = parser.parseExpr(scope, newCtx, precedence)

        parser.continuableTypeCheck(BoolType, right.type, right.position)

        ctx.assumptions.mergeAll(newCtx.assumptions) { v1, v2 ->
            v1.copy(type = v1.type.commonAssignableFromType(v2.type))
        }

        if ((ctx.inverseAssumptions.keys + newCtx.inverseAssumptions.keys).size == 1) {
            // cant trust any assumptions about outside if depends on multiple variables
            ctx.inverseAssumptions.mergeAll(newCtx.inverseAssumptions) { v1, v2 ->
                v1.copy(type = v1.type.commonAssignableToType(v2.type))
            }
        } else {
            ctx.inverseAssumptions.clear()
        }


        return LogicalAndOperation(left, right, token.position)
    }
}