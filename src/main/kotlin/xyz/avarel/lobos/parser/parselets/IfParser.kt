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
import xyz.avarel.lobos.typesystem.scope.ParserContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object IfParser: PrefixParser {
    override fun parse(parser: Parser, scope: ParserContext, token: Token): Expr {
        val thenContext = scope.subContext()

        // TODO FOR ADVANCED BOOLEEAN COMPOSITIONS
        // NOT & OR -> EXCHANGE INVERSE AND ASSUMPTION
        // AND -> KEEP

        val condition = parser.parseExpr(thenContext)
        typeCheck(BoolType, condition.type, condition.position)

        val thenBranch = parser.parseBlock(thenContext)

        if (thenContext.terminates) {
            scope.assumptions += thenContext.inverseAssumptions
        }

        val elseBranch: Expr?
        val elseContext: ParserContext?

        if (parser.match(TokenType.ELSE)) {
            elseContext = scope.subContext()
            elseBranch = if (parser.nextIs(TokenType.IF)) parser.parseExpr(elseContext)
            else parser.parseBlock(elseContext)
        } else {
            elseBranch = null
            elseContext = null
        }

        val type = when {
            elseBranch != null -> when {
                elseContext!!.terminates -> if (thenContext.terminates) NeverType else thenBranch.type
                else -> elseBranch.type.coerceType(thenBranch.type)
            }
            else -> InvalidType
        }

        return IfExpr(type, condition, thenBranch, elseBranch, token.position)
    }
}

//fun inferAssumption(scope: ParserContext, condition: Expr, inverse: Boolean) {
//    if (condition !is BinaryOperation) return
//    val cast: BinaryOperation = condition
//    val fn = when (cast.operator) {
//        BinaryOperationType.NOT_EQUALS -> if (inverse) Type::filterType else Type::subtract
//        BinaryOperationType.EQUALS -> if (inverse) Type::subtract else Type::filterType
//        else -> return
//    }
//    inferAssumptionExpr(scope, cast.right, cast.left, fn)
//    inferAssumptionExpr(scope, cast.left, cast.right, fn)
//}

fun inferAssumptionExpr(
        scope: ParserContext,
        target: Expr,
        other: Expr,
        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
): Triple<String, VariableInfo, VariableInfo>? {
    if (target !is IdentExpr) return null
    val key = target.name
    val formal = scope.getVariable(key)!!
    val effective = scope.getEffectiveType(key)!!
    val assumption = formal.copy(type = inferEffectiveOrFormal(effective.type, formal.type, other.type, function.first))
    val inverse = formal.copy(type = inferEffectiveOrFormal(effective.type, formal.type, other.type, function.second))
    return Triple(key, assumption, inverse)
}