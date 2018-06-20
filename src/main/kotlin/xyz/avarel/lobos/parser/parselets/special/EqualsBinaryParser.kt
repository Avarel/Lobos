package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.EqualsOperation
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.inferAssumptionExpr
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object EqualsBinaryParser: InfixParser {
    override val precedence: Int get() = Precedence.EQUALITY

    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        val right = parser.parseExpr(scope, StmtContext(AnyType), precedence)

        inferAssumptionExpr(
                true,
                scope,
                ctx,
                left,
                right,
                Type::filter to Type::exclude
        )?.let { (name, a, b) ->
            ctx.assumptions[name] = a
            ctx.inverseAssumptions[name] = b
        }
        inferAssumptionExpr(
                true,
                scope,
                ctx,
                right,
                left,
                Type::filter to Type::exclude
        )?.let { (name, a, b) ->
            ctx.assumptions[name] = a
            ctx.inverseAssumptions[name] = b
        }

        return EqualsOperation(left, right, token.position)
    }
}