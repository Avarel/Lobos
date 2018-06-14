package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.BinaryOperationType
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.parselets.BinaryOperatorParser
import xyz.avarel.lobos.parser.parselets.inferAssumptionExpr
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object NotEqualsBinaryParser: BinaryOperatorParser(Precedence.EQUALITY, BinaryOperationType.EQUALS) {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        val expr = super.parse(parser, scope, ctx, token, left) as BinaryOperation

        inferAssumptionExpr(true, scope, ctx, expr.left, expr.right, Type::exclude to Type::filter)?.let { (name, a, b) ->
            ctx.assumptions[name] = a
            ctx.inverseAssumptions[name] = b
        }
        inferAssumptionExpr(true, scope, ctx, expr.right, expr.left, Type::exclude to Type::filter)?.let { (name, a, b) ->
            ctx.assumptions[name] = a
            ctx.inverseAssumptions[name] = b
        }

        return expr //TODO apply unary not
    }
}