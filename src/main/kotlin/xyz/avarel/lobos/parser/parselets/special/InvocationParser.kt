package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.UnitType
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.generics.TupleType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import java.util.*

object InvocationParser: InfixParser {
    override val precedence: Int = Precedence.POSTFIX

    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!parser.match(TokenType.R_PAREN)) {
            do {
                arguments.add(parser.parseExpr(scope, StmtContext()))
            } while (parser.match(TokenType.COMMA))
            parser.eat(TokenType.R_PAREN)
        }

        if (left.type !is FunctionType) {
            throw SyntaxException("${left.type} is not invokable", left.position)
        }

        val argumentTypes = arguments.map(Expr::type)
        var leftType = left.type as FunctionType

        if (leftType.genericParameters.isNotEmpty()) {
            val inferType = FunctionType(false, argumentTypes, ctx.expectedType ?: UnitType)
            val map = inferGenerics(parser, leftType, inferType, token.position)
            if (map.size != leftType.genericParameters.size) {
                throw SyntaxException("Inference failed", token.position)
            }
            leftType = leftType.template(leftType.genericParameters.map { map[it]!! })
        }

        leftType.checkInvocation(argumentTypes, token.position)

        return InvokeExpr(leftType.returnType, left, arguments, token.position)
    }

    private fun inferGenerics(parser: Parser, parameter: Type, argument: Type, position: Position): Map<GenericParameter, Type> {
        if (parameter !is TypeTemplate) {
            return emptyMap()
        }

        return when {
            parameter is GenericType -> {
                val gp = parameter.genericParameter
                if (parameter.parentType.isAssignableFrom(argument)) {
                    mapOf(gp, argument)
                } else {
                    parser.errors += SyntaxException("Type parameter bound for generic parameter ${gp.name} not satisfied.", position)
                    emptyMap()
                }
            }
            parameter is FunctionType && argument is FunctionType -> {
                if (argument.argumentTypes.size < parameter.argumentTypes.size) {
                    parser.errors += SyntaxException("Can not infer generic parameters (insufficient arguments)", position)
                    return emptyMap()
                }

                val map = mutableMapOf<GenericParameter, Type>()

                parameter.argumentTypes.zip(argument.argumentTypes).forEach { (a, b) ->
                    map.mergeAll(inferGenerics(parser, a, b, position)) { _, v1, v2 ->
                        v1.commonAssignableToType(v2)
                    }
                }

                map.mergeAll(inferGenerics(parser, parameter.returnType, argument.returnType, position)) { _, v1, _ -> v1 }

                map
            }
            parameter is TupleType && argument is TupleType -> {
                if (argument.valueTypes.size < parameter.valueTypes.size) {
                    parser.errors += SyntaxException("Can not infer generic parameters (insufficient values)", position)
                    return emptyMap()
                }

                val map = mutableMapOf<GenericParameter, Type>()

                parameter.valueTypes.zip(argument.valueTypes).forEach { (a, b) ->
                    map.mergeAll(inferGenerics(parser, a, b, position)) { _, v1, v2 ->
                        v1.commonAssignableToType(v2)
                    }
                }

                map
            }
            else -> emptyMap()
        }
    }

    private fun <K, V> mapOf(key: K, value: V) = Collections.singletonMap(key, value)
}