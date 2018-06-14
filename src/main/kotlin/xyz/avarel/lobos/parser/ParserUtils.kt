package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NullType
import xyz.avarel.lobos.typesystem.generics.*
import xyz.avarel.lobos.typesystem.literals.LiteralFalseType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType
import xyz.avarel.lobos.typesystem.literals.LiteralStrType
import xyz.avarel.lobos.typesystem.literals.LiteralTrueType
import xyz.avarel.lobos.typesystem.scope.ScopeContext

fun Parser.parseBlock(scope: ScopeContext): Expr {
    return parseStatements(scope, TokenType.L_BRACE to TokenType.R_BRACE)
}

fun Parser.parseType(scope: ScopeContext): Type {
    return parseUnionType(scope)
}

fun Parser.parseUnionType(scope: ScopeContext): Type {
    val type = parseSubtractionType(scope)

    if (match(TokenType.PIPE)) {
        val list = mutableListOf<Type>()
        list.add(type)
        do {
            list.add(try { parseSubtractionType(scope) } catch (e: SyntaxException) { InvalidType })
        } while (match(TokenType.PIPE))
        return list.toType()
    }

    return type
}

fun Parser.parseSubtractionType(scope: ScopeContext): Type {
    val type = parseSingleType(scope)

    if (match(TokenType.BANG)) {
        val token = peek()
        val subtractionType = try { parseSingleType(scope) } catch (e: SyntaxException) { InvalidType }

        if (!type.isAssignableFrom(subtractionType)) {
            throw SyntaxException("$subtractionType can not be subtracted from $type", token.position)
        }

        return ExcludedType(type, subtractionType)
    }

    return type
}

fun Parser.parseSingleType(scope: ScopeContext): Type {
    return when {
        match(TokenType.TRUE) -> LiteralTrueType
        match(TokenType.FALSE) -> LiteralFalseType
        match(TokenType.NULL) -> NullType
        match(TokenType.IDENT) -> {
            val ident = last
            val name = ident.string

            val type = scope.getType(name) ?: throw SyntaxException("Unresolved type $name", ident.position)

            if (match(TokenType.LT)) {
                if (type !is TypeTemplate) {
                    throw SyntaxException("Type $type is not a template", last.position)
                }

                val typeParameters = mutableListOf<Type>()

                do {
                    typeParameters.add(parseUnionType(scope))
                } while (match(TokenType.COMMA))

                eat(TokenType.GT)

                type.template(typeParameters)
            } else {
                type
            }
        }
        match(TokenType.INT) -> {
            LiteralIntType(last.string.toInt())
        }
        match(TokenType.STRING) -> LiteralStrType(last.string)
        match(TokenType.L_PAREN) -> {
            val valueTypes = mutableListOf<Type>()
            when {
                match(TokenType.R_PAREN) -> return UnitType
                else -> {
                    val firstType = tryOrInvalid { parseType(scope) }
                    valueTypes.add(firstType)

                    if (match(TokenType.R_PAREN)) {
                        if (nextIs(TokenType.ARROW)) {
                            return constructTupleOrFunctionType(scope, valueTypes)
                        }

                        return firstType
                    }

                    if (match(TokenType.COMMA)) {
                        if (match(TokenType.R_PAREN)) {
                            return TupleType(listOf(firstType))
                        }
                        do {
                            valueTypes.add(tryOrInvalid { parseType(scope) })
                        } while (match(TokenType.COMMA))
                    }

                    eat(TokenType.R_PAREN)
                }
            }

            return constructTupleOrFunctionType(scope, valueTypes)
        }
        else -> throw SyntaxException("Expected type", peek().position)
    }
}

private fun Parser.constructTupleOrFunctionType(scope: ScopeContext, valueTypes: List<Type>): Type {
   return when {
        match(TokenType.ARROW) -> {
            val returnType = tryOrInvalid { parseType(scope) }
            FunctionType(false, valueTypes, returnType)
        }
        else -> TupleType(valueTypes)
    }
}

private inline fun tryOrInvalid(block: () -> Type): Type {
    return try { block() } catch (e: SyntaxException) { InvalidType }
}

fun Parser.parseGenericArgumentsScope(scope: ScopeContext): ScopeContext? {
    if (!match(TokenType.LT)) {
        return null
    }

    val typeScope = scope.subContext()
    do {
        val genericToken = eat(TokenType.IDENT)
        val genericName = genericToken.string

        if (genericName in typeScope.types) {
            errors += SyntaxException("Generic parameter $genericName has already been declared", genericToken.position)
        }

        if (match(TokenType.COLON)) {
            val parentType = parseType(typeScope)
            typeScope.types[genericName] = GenericType(GenericParameter(genericName, parentType))
        } else {
            typeScope.types[genericName] = GenericType(GenericParameter(genericName))
        }
    } while (match(TokenType.COMMA))

    eat(TokenType.GT)
    return typeScope
}

fun Parser.continuableTypeCheck(expectedType: Type, foundType: Type, position: Position) {
    try {
        typeCheck(expectedType, foundType, position)
    } catch (e: SyntaxException) {
        errors.add(e)
    }
}

/**
 * Throws an error if [foundType] can not be assigned to [expectedType].
 */
fun typeCheck(expectedType: Type, foundType: Type, position: Position) {
    if (!expectedType.isAssignableFrom(foundType)) {
        throw SyntaxException("Expected $expectedType but found $foundType", position)
    }
}

/**
 * Throws an error if [this] can not be invoked by [argumentTypes].
 * This requires that [this] is a function type, and that [argumentTypes]
 * must conform to all of the functions argument types.
 */
fun Type.checkInvocation(argumentTypes: List<Type>, position: Position) {
    when {
        this is UnionType -> try { valueTypes.forEach { it.checkInvocation(argumentTypes, position) } } catch (e: SyntaxException) {
            throw SyntaxException("Can not invoke $this with $argumentTypes", position)
        }
        this !is FunctionType -> throw SyntaxException("Can not invoke $this", position)
        this.argumentTypes.size != argumentTypes.size || !canBeInvokedBy(argumentTypes) -> {
            throw SyntaxException(buildString {
                append("Expected ")
                this@checkInvocation.argumentTypes.joinTo(this, prefix = "(", postfix = ")")
                append(" but found ")
                argumentTypes.joinTo(this, prefix = "(", postfix = ")")
            }, position)
        }
    }
}

fun FunctionType.canBeInvokedBy(argumentTypes: List<Type>): Boolean {
    assert(this.argumentTypes.size != argumentTypes.size)
    for (i in argumentTypes.indices) {
        if (!this.argumentTypes[i].isAssignableFrom(argumentTypes[i])) {
            return false
        }
    }
    return true
}

inline fun requireSyntax(value: Boolean, position: Position, lazy: () -> String) {
    if (!value) {
        throw SyntaxException(lazy(), position)
    }
}