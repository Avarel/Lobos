package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.*
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.generics.SubtractionType
import xyz.avarel.lobos.typesystem.generics.TupleType
import xyz.avarel.lobos.typesystem.generics.UnionType
import xyz.avarel.lobos.typesystem.scope.ParserContext

fun Parser.parseBlock(scope: ParserContext): Expr {
    return parseStatements(scope, TokenType.L_BRACE to TokenType.R_BRACE)
}

fun Parser.parseType(scope: ParserContext): Type {
    return parseUnionType(scope)
}

fun Parser.parseUnionType(scope: ParserContext): Type {
    val type = parseSubtractionType(scope)

    if (match(TokenType.PIPE)) {
        val list = mutableListOf<Type>()
        list.add(type)
        do {
            list.add(try { parseSubtractionType(scope) } catch (e: SyntaxException) { InvalidType })
        } while (match(TokenType.PIPE))

        val optimizedList = list.distinct()

        return when {
            optimizedList.size == 2 && LiteralTrueType in optimizedList && LiteralFalseType in optimizedList -> BoolType
            AnyType in optimizedList -> AnyType
            else -> UnionType(optimizedList)
        }
    }

    return type
}

fun Parser.parseSubtractionType(scope: ParserContext): Type {
    val type = parseSingleType(scope)

    if (match(TokenType.BANG)) {
        val token = peek()
        val subtractionType = try { parseSingleType(scope) } catch (e: SyntaxException) { InvalidType }

        if (!type.isAssignableFrom(subtractionType)) {
            throw SyntaxException("$subtractionType can not be subtracted from $type", token.position)
        }

        return SubtractionType(type, subtractionType)
    }

    return type
}

fun Parser.parseSingleType(scope: ParserContext): Type {
    return when {
        match(TokenType.TRUE) -> LiteralTrueType
        match(TokenType.FALSE) -> LiteralFalseType
        match(TokenType.NULL) -> NullType
        match(TokenType.IDENT) -> {
            val ident = last
            val name = ident.string!!
            scope.getType(name) ?: throw SyntaxException("Unresolved type $name", ident.position)
        }
        match(TokenType.INT) -> {
            val start = last.string!!.toInt()
            when {
                match(TokenType.RANGE_IN) -> {
                    val endExpr = eat(TokenType.INT)
                    val end = endExpr.string!!.toInt()

                    when {
                        end < start -> throw SyntaxException("start[$start] must be <= end[$end]", endExpr.position)
                        end == start -> LiteralIntType(start)
                        else -> LiteralIntRangeInclusiveType(start, end)
                    }
                }
                match(TokenType.RANGE_EX) -> {
                    val endExpr = eat(TokenType.INT)
                    val end = endExpr.string!!.toInt()

                    when {
                        end <= start -> throw SyntaxException("start[$start] must be < end[$end]", endExpr.position)
                        else -> LiteralIntRangeExclusiveType(start, end)
                    }
                }
                else -> LiteralIntType(start)
            }
        }
        match(TokenType.STRING) -> LiteralStringType(last.string!!)
        match(TokenType.L_PAREN) -> {
            val valueTypes = mutableListOf<Type>()
            when {
                match(TokenType.R_PAREN) -> {}
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

private fun Parser.constructTupleOrFunctionType(scope: ParserContext, valueTypes: List<Type>): Type {
   return when {
        match(TokenType.ARROW) -> {
            val returnType = tryOrInvalid { parseType(scope) }
            FunctionType(false, valueTypes, returnType)
        }
        valueTypes.isEmpty() -> TupleType.Unit
        else -> TupleType(valueTypes)
    }
}

private inline fun tryOrInvalid(block: () -> Type): Type {
    return try { block() } catch (e: SyntaxException) { InvalidType }
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

fun Type.coerceType(other: Type): Type {
    return when {
        this == other -> this
        this === NeverType -> other
        other === NeverType -> this
        other === InvalidType || this === InvalidType -> InvalidType
        this is UnionType && other is UnionType -> UnionType(this.valueTypes + other.valueTypes)
        this is UnionType -> UnionType(this.valueTypes.toMutableList().also { it.add(other) })
        other is UnionType -> UnionType(mutableListOf(this).also { it.addAll(other.valueTypes) })
        else ->  UnionType(listOf(this, other))
    }
}

fun inferEffectiveOrFormal(effectiveType: Type, formalType: Type, other: Type, function: (Type, Type) -> Type): Type {
    val type = function(formalType, other)
    return if (type.isAssignableFrom(effectiveType)) effectiveType else type
}

fun Type.subtract(other: Type): Type {
    return when {
        other == this -> NeverType
        this is UnionType -> this.valueTypes.filter { it != other }.let {
            if (it.isEmpty()) NeverType else UnionType(it)
        }
        this === BoolType && other === LiteralTrueType -> LiteralFalseType
        this === BoolType && other === LiteralFalseType -> LiteralTrueType
        this === AnyType && other === NullType -> SubtractionType(this, other)
        this === StrType
                && other is LiteralStringType -> SubtractionType(this, other)
        this === I32Type
                && (other is LiteralIntType
                || other is LiteralIntRangeExclusiveType
                || other is LiteralIntRangeInclusiveType) -> SubtractionType(this, other)
        this is LiteralIntRangeInclusiveType
                && other is LiteralIntType
                && other.value in this.start..this.end -> SubtractionType(this, other)
        this is LiteralIntRangeExclusiveType
                && other is LiteralIntType
                && other.value in this.start until this.end -> SubtractionType(this, other)
        this is SubtractionType && this.targetType.isAssignableFrom(other) -> {
            SubtractionType(this.targetType, this.subtractedType.coerceType(other))
        }
        else -> this
    }
}

fun Type.filter(other: Type): Type {
    return when {
        other == this -> this
        this is UnionType -> {
            if (valueTypes.contains(other)) other else NeverType
        }
        this === BoolType
                && (other === LiteralTrueType
                || other === LiteralFalseType) -> other
        this === StrType
                && other is LiteralStringType -> other
        this === I32Type
                && (other is LiteralIntType
                || other is LiteralIntRangeExclusiveType
                || other is LiteralIntRangeInclusiveType) -> other
        this is LiteralIntRangeInclusiveType
                && other is LiteralIntType
                && other.value in this.start..this.end -> other
        this is LiteralIntRangeExclusiveType
                && other is LiteralIntType
                && other.value in this.start until this.end -> other
        this is SubtractionType -> if (this.subtractedType.isAssignableFrom(other)) NeverType else {
            this.targetType.filter(other)
        }
        else -> NeverType
    }
}