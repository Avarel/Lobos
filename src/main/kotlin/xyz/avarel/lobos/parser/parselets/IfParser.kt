package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
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
        val conditionCtx = StmtContext(BoolType)
        val condition = parser.parseExpr(scope, conditionCtx)
        typeCheck(BoolType, condition.type, condition.position)

        val thenContext = scope.subContext()
        thenContext.assumptions += conditionCtx.assumptions

        val thenBranch = parser.parseBlock(thenContext)

        val elseBranch: Expr?
        val elseContext: ScopeContext?

        if (parser.match(TokenType.ELSE)) {
            elseContext = scope.subContext()
            elseContext.assumptions += conditionCtx.inverseAssumptions
            elseBranch = if (parser.nextIs(TokenType.IF)) parser.parseExpr(elseContext, StmtContext())
            else parser.parseBlock(elseContext)
        } else {
            if (ctx.expectedType != null) {
                parser.errors += SyntaxException("if expression must have else branch", token.position)
            }
            elseBranch = null
            elseContext = null
        }

        if (thenContext.terminates) {
            scope.assumptions += conditionCtx.inverseAssumptions
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
        removeUnitOnly: Boolean,
        scope: ScopeContext,
        ctx: StmtContext,
        target: Expr,
        other: Expr,
        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
): Triple<String, VariableInfo, VariableInfo>? {
    if (target !is IdentExpr) return null

    val key = target.name
    val effective = ctx.assumptions[key] ?: scope.getEffectiveType(key)!!

    if (removeUnitOnly && !other.type.isUnitType) {
        return null
    }

    val assumption = effective.copy(type = function.first(effective.type, other.type))
    val inverse = effective.copy(type = function.second(effective.type, other.type))

    return Triple(key, assumption, inverse)
}