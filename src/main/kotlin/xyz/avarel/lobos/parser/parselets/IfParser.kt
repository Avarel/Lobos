package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.mergeAll
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object IfParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token): Expr {
        // OR -> keep ctx
        //       actually you know what just learn to fucking combine assumptions left and right
        // AND -> new ctx (with inverse of current ctx)
        //       actually you know what just learn to fucking combine assumptions left and right

        val conditionCtx = StmtContext(true)
        val condition = parser.parseExpr(scope, conditionCtx)
        typeCheck(BoolType, condition.type, condition.position)

        val thenContext = scope.subContext()
        thenContext.assumptions += conditionCtx.assumptions

        val thenBranch = parser.parseBlock(thenContext)

        if (thenContext.terminates) {
            scope.assumptions.mergeAll(conditionCtx.inverseAssumptions) { v1, v2 -> v1.copy(type = v1.type.commonAssignableToType(v2.type)) }
        }

        val elseBranch: Expr?
        val elseContext: ScopeContext?

        if (parser.match(TokenType.ELSE)) {
            elseContext = scope.subContext()
            elseBranch = if (parser.nextIs(TokenType.IF)) parser.parseExpr(elseContext, StmtContext())
            else parser.parseBlock(elseContext)
        } else {
            elseBranch = null
            elseContext = null
        }

        val type = when {
            elseBranch != null -> when {
                elseContext!!.terminates -> if (thenContext.terminates) NeverType else thenBranch.type
                else -> elseBranch.type.commonAssignableToType(thenBranch.type)
            }
            else -> InvalidType
        }

        return IfExpr(type, condition, thenBranch, elseBranch, token.position)
    }
}

fun inferAssumptionExpr(
        scope: ScopeContext,
        ctx: StmtContext,
        target: Expr,
        other: Expr,
        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
): Triple<String, VariableInfo, VariableInfo>? {
    if (target !is IdentExpr) return null
    val key = target.name
    val formal = scope.getVariable(key)!!
    val effective = ctx.assumptions[key] ?: scope.getEffectiveType(key)!!
    val assumption = formal.copy(type = inferEffectiveOrFormal(effective.type, formal.type, other.type, function.first))
    val inverse = formal.copy(type = inferEffectiveOrFormal(effective.type, formal.type, other.type, function.second))
    return Triple(key, assumption, inverse)
}