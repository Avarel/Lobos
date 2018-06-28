package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.GenericParameterAST
import xyz.avarel.lobos.ast.types.basic.IdentTypeAST
import xyz.avarel.lobos.ast.types.basic.NeverTypeAST
import xyz.avarel.lobos.ast.types.basic.NullTypeAST
import xyz.avarel.lobos.ast.types.complex.FunctionTypeAST
import xyz.avarel.lobos.ast.types.complex.TemplatingTypeAST
import xyz.avarel.lobos.ast.types.complex.TupleTypeAST
import xyz.avarel.lobos.ast.types.complex.UnionTypeAST
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.scope.ScopeContext

fun Parser.parseBlock(): Expr {
    return parseStatements(TokenType.L_BRACE to TokenType.R_BRACE)
}

fun Parser.parseType(): AbstractTypeAST {
    return parseUnionType()
}

fun Parser.parseUnionType(): AbstractTypeAST {
    val type = parseSingleType()

    if (match(TokenType.PIPE)) {
        val list = mutableListOf<AbstractTypeAST>()
        list += type
        do {
            list += parseSingleType()
        } while (match(TokenType.PIPE))
        return list.reduce { acc, typeAST -> UnionTypeAST(acc, typeAST, acc.position.span(typeAST.position)) }
    }

    return type
}

fun Parser.parseSingleType(): AbstractTypeAST {
    return when {
        match(TokenType.NULL) -> NullTypeAST(last.position)
        match(TokenType.BANG) -> NeverTypeAST(last.position)
        match(TokenType.IDENT) -> {
            val ident = last
            val name = ident.string

            val type = IdentTypeAST(name, ident.position)

            if (!match(TokenType.LT)) return type

            val typeParameters = mutableListOf<AbstractTypeAST>()
            do {
                typeParameters += parseUnionType()
            } while (match(TokenType.COMMA))
            val gt = eat(TokenType.GT)

            return TemplatingTypeAST(type, typeParameters, ident.position.span(gt.position))
        }
        match(TokenType.L_PAREN) -> {
            val lParen = last
            val valueTypes = mutableListOf<AbstractTypeAST>()

            if (!match(TokenType.R_PAREN)) {
                val firstType = parseType()
                valueTypes += firstType

                if (match(TokenType.R_PAREN)) {
                    if (nextIs(TokenType.ARROW)) {
                        return constructTupleOrFunctionType(valueTypes, lParen.position.span(last.position))
                    }

                    return firstType
                }

                if (match(TokenType.COMMA)) {
                    if (match(TokenType.R_PAREN)) {
                        return TupleTypeAST(listOf(firstType), lParen.position.span(last.position))
                    }
                    do {
                        valueTypes += parseType()
                    } while (match(TokenType.COMMA))
                }

                eat(TokenType.R_PAREN)
            }

            return constructTupleOrFunctionType(valueTypes, lParen.position.span(last.position))
        }
        else -> throw SyntaxException("Expected type", peek().position)
    }
}

private fun Parser.constructTupleOrFunctionType(valueTypes: List<AbstractTypeAST>, position: Section): AbstractTypeAST {
   return when {
        match(TokenType.ARROW) -> {
            val returnType = parseType()
            FunctionTypeAST(valueTypes, returnType, position)
        }
       valueTypes.isEmpty() -> TupleTypeAST(position)
       else -> TupleTypeAST(valueTypes, position)
    }
}



fun ScopeContext.allVariables(): Map<String, Type> {
    return parent?.allVariables()?.let { it + this.variables } ?: this.variables
}

fun Parser.parseGenericParameters(): List<GenericParameterAST> {
    if (!match(TokenType.LT)) return emptyList()

    val genericNames = mutableListOf<String>()
    val genericParameters = mutableListOf<GenericParameterAST>()
    do {
        val genericToken = eat(TokenType.IDENT)
        val genericName = genericToken.string

        if (genericName in genericNames) {
            errors += SyntaxException("Generic parameter $genericName has already been declared", genericToken.position)
        }

        val param = if (match(TokenType.COLON)) {
            val parentType = parseType()
            GenericParameterAST(genericName, parentType)
        } else {
            GenericParameterAST(genericName)
        }

        genericParameters += param
    } while (match(TokenType.COMMA))

    eat(TokenType.GT)
    return genericParameters
}

/**
 * Throws an error if [foundType] can not be assigned to [expectedType].
 */
fun typeCheck(expectedType: Type, foundType: Type, position: Section) {
    if (!expectedType.isAssignableFrom(foundType)) {
        throw SyntaxException("Expected $expectedType but found $foundType", position)
    }
}

//fun checkNotGeneric(expr: Expr, position: Section): Type {
//    val exprType = expr.type
//    if (exprType is TypeTemplate && exprType.genericParameters.isNotEmpty()) {
//        throw SyntaxException("Missing generic type parameters", position)
//    }
//    return exprType
//}

///**
// * Check that [target] is invokable by [arguments].
// * @return [target] return type.
// * @throws SyntaxException if [target] is not a function.
// */
//fun Parser.checkInvocation(target: Expr, arguments: List<Expr>, position: Section): Type {
//    val targetType = target.type
//    if (targetType !is FunctionType) {
//        throw SyntaxException("$targetType can not be invoked", target.position)
//    }
//
//    val targetArgumentTypes = targetType.argumentTypes
//    val argumentTypes = arguments.map(Expr::type)
//
//    if (targetArgumentTypes.size != argumentTypes.size) {
//        errors += SyntaxException("Expected ${targetArgumentTypes.size} arguments, but found ${argumentTypes.size} arguments", position)
//    }
//
//    for (i in targetArgumentTypes.indices) {
//        safe { typeCheck(targetArgumentTypes[i], argumentTypes[i], arguments[i].position) }
//    }
//
//    return targetType.returnType
//}

///**
// * Throws an error if [this] can not be invoked by [argumentTypes].
// * This requires that [this] is a function type, and that [argumentTypes]
// * must conform to all of the functions argument types.
// */
//fun Type.checkInvocation(argumentTypes: List<Type>, position: Section) {
//    when {
//        this is UnionType -> try {
//            left.checkInvocation(argumentTypes, position)
//            right.checkInvocation(argumentTypes, position)
//        } catch (e: SyntaxException) {
//            throw SyntaxException("Can not invoke $this with $argumentTypes", position)
//        }
//        this !is FunctionType -> throw SyntaxException("$this is not a function", position)
//        this.argumentTypes.size != argumentTypes.size || !canBeInvokedBy(argumentTypes) -> {
//            throw SyntaxException(buildString {
//                append("Expected ")
//                this@checkInvocation.argumentTypes.joinTo(this, prefix = "(", postfix = ")")
//                append(" but found ")
//                argumentTypes.joinTo(this, prefix = "(", postfix = ")")
//            }, position)
//        }
//    }
//}

//fun FunctionType.canBeInvokedBy(argumentTypes: List<Type>): Boolean {
//    require(this.argumentTypes.size == argumentTypes.size)
//    return this.argumentTypes.zip(argumentTypes).all { (a, b) -> a.isAssignableFrom(b) }
//}

//inline fun requireSyntax(value: Boolean, position: Section, lazy: () -> String) {
//    if (!value) {
//        throw SyntaxException(lazy(), position)
//    }
//}

//fun inferAssumptionExpr(
//        removeUnitOnly: Boolean,
//        scope: ScopeContext,
//        ctx: StmtContext,
//        target: Expr,
//        other: Expr,
//        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
//): Triple<String, Type, Type>? {
//    if (target !is IdentExpr) return null
//
//    val key = target.name
//    val effectiveType = ctx.assumptions[key] ?: scope.getAssumption(key)!!
//    val otherType = other.type
//
//    if (removeUnitOnly && !otherType.isUnitType) {
//        return null
//    }
//
//    val assumption = function.first(effectiveType, otherType)
//    val inverse = function.second(effectiveType, otherType)
//
//    return Triple(key, assumption, inverse)
//}

inline fun <K, V> MutableMap<K, V>.mergeAll(other: Map<K, V>, remappingFunction: (V, V) -> V) {
    other.forEach { (k, v) ->
        this[k]?.let {
            put(k, remappingFunction(it, v))
        } ?: put(k, v)
    }
}

inline fun <R> Parser.safe(block: () -> R): R? {
    return try {
        block()
    } catch (e: SyntaxException) {
        this.errors += e
        null
    }
}