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

object OrParser: BinaryParser(Precedence.DISJUNCTION, true) {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        parser.continuableTypeCheck(BoolType, left.type, left.position)

        val newCtx = StmtContext(BoolType)
        newCtx.assumptions += ctx.inverseAssumptions

        val right = parser.parseExpr(scope, newCtx, precedence)

        parser.continuableTypeCheck(BoolType, right.type, right.position)

        if ((ctx.assumptions.keys + newCtx.assumptions.keys).size == 1) {
            // can only assume if it is only one variable
            ctx.assumptions.mergeAll(newCtx.assumptions) { v1, v2 ->
                v1.copy(type = v1.type.commonAssignableToType(v2.type))
            }
        } else {
            ctx.assumptions.clear()
        }

        ctx.inverseAssumptions.mergeAll(newCtx.inverseAssumptions) { v1, v2 ->
            println("$v1 $v2")
            v1.copy(type = v1.type.commonAssignableFromType(v2.type))
        }

        return LogicalAndOperation(left, right, token.position)
    }
}