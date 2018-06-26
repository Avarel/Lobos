package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.*
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.base.NullType
import xyz.avarel.lobos.typesystem.base.UnitType
import xyz.avarel.lobos.typesystem.complex.ExcludedType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.complex.TupleType
import xyz.avarel.lobos.typesystem.complex.UnionType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.literals.LiteralFalseType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType
import xyz.avarel.lobos.typesystem.literals.LiteralStrType
import xyz.avarel.lobos.typesystem.literals.LiteralTrueType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

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
        list += type
        do {
            list += try { parseSubtractionType(scope) } catch (e: SyntaxException) { InvalidType }
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
        match(TokenType.BANG) -> NeverType
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
                    typeParameters += parseUnionType(scope)
                } while (match(TokenType.COMMA))

                eat(TokenType.GT)

                if (typeParameters.size != type.genericParameters.size) {
                    throw SyntaxException("Expected ${type.genericParameters.size} type arguments, found ${typeParameters.size}", last.position)
                }

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

            if (!match(TokenType.R_PAREN)) {
                val firstType = tryOrInvalid { parseType(scope) }
                valueTypes += firstType

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
                        valueTypes += tryOrInvalid { parseType(scope) }
                    } while (match(TokenType.COMMA))
                }

                eat(TokenType.R_PAREN)
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
        valueTypes.isEmpty() -> UnitType
        else -> TupleType(valueTypes)
    }
}

private inline fun tryOrInvalid(block: () -> Type): Type {
    return try { block() } catch (e: SyntaxException) { InvalidType }
}

fun ScopeContext.allVariables(): Map<String, Type> {
    return parent?.allVariables()?.let { it + this.variables } ?: this.variables
}

fun Parser.parseGenericArgumentsScope(scope: ScopeContext): Pair<List<GenericParameter>, ScopeContext>? {
    if (!match(TokenType.LT)) {
        return null
    }

    val genericParameters = mutableListOf<GenericParameter>()
    val typeScope = scope.subContext()
    do {
        val genericToken = eat(TokenType.IDENT)
        val genericName = genericToken.string

        if (genericName in typeScope.types) {
            errors += SyntaxException("Generic parameter $genericName has already been declared", genericToken.position)
        }

        val param = if (match(TokenType.COLON)) {
            val parentType = parseType(typeScope)
            GenericParameter(genericName, parentType)
        } else {
            GenericParameter(genericName)
        }

        genericParameters += param
        typeScope.types[genericName] = GenericType(param)
    } while (match(TokenType.COMMA))

    eat(TokenType.GT)
    return genericParameters to typeScope
}

fun Parser.continuableTypeCheck(expectedType: Type, foundType: Type, position: Section) {
    try {
        typeCheck(expectedType, foundType, position)
    } catch (e: SyntaxException) {
        errors += e
    }
}

/**
 * Throws an error if [foundType] can not be assigned to [expectedType].
 */
fun typeCheck(expectedType: Type, foundType: Type, position: Section) {
    if (!expectedType.isAssignableFrom(foundType)) {
        throw SyntaxException("Expected $expectedType but found $foundType", position)
    }
}

/**
 * Throws an error if [this] can not be invoked by [argumentTypes].
 * This requires that [this] is a function type, and that [argumentTypes]
 * must conform to all of the functions argument types.
 */
fun Type.checkInvocation(argumentTypes: List<Type>, position: Section) {
    when {
        this is UnionType -> try {
            left.checkInvocation(argumentTypes, position)
            right.checkInvocation(argumentTypes, position)
        } catch (e: SyntaxException) {
            throw SyntaxException("Can not invoke $this with $argumentTypes", position)
        }
        this !is FunctionType -> throw SyntaxException("$this is not a function", position)
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
    require(this.argumentTypes.size == argumentTypes.size)
    return this.argumentTypes.zip(argumentTypes).all { (a, b) -> a.isAssignableFrom(b) }
}

inline fun requireSyntax(value: Boolean, position: Section, lazy: () -> String) {
    if (!value) {
        throw SyntaxException(lazy(), position)
    }
}

fun inferAssumptionExpr(
        removeUnitOnly: Boolean,
        scope: ScopeContext,
        ctx: StmtContext,
        target: Expr,
        other: Expr,
        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
): Triple<String, Type, Type>? {
    if (target !is IdentExpr) return null

    val key = target.name
    val effectiveType = ctx.assumptions[key] ?: scope.getAssumption(key)!!
    val otherType = other.type

    if (removeUnitOnly && !otherType.isUnitType) {
        return null
    }

    val assumption = function.first(effectiveType, otherType)
    val inverse = function.second(effectiveType, otherType)

    return Triple(key, assumption, inverse)
}

inline fun <K, V> MutableMap<K, V>.mergeAll(other: Map<K, V>, remappingFunction: (V, V) -> V) {
    other.forEach { (k, v) ->
        this[k]?.let {
            put(k, remappingFunction(it, v))
        } ?: put(k, v)
    }
}

fun enhancedInfer(parser: Parser, target: Type, subject: Type, position: Section): Type {
    return if (subject is TypeTemplate && subject.genericParameters.isNotEmpty()) {
        val map = try {
            subject.extract(target)
        } catch (e: IllegalArgumentException) {
            parser.errors += SyntaxException(e.message ?: "Failed to infer generic parameters", position)
            emptyMap<GenericParameter, Type>()
        }

        if (map.size != subject.genericParameters.size || !map.keys.containsAll(subject.genericParameters)) {
            throw SyntaxException("Failed to infer generic parameters", position)
        }

        return subject.template(map)
    } else subject
}

fun enhancedCheckInvocation(parser: Parser, selfInvocation: Boolean, target: Type, arguments: List<Expr>, returnType: Type?, position: Section): Type {
    if (target !is FunctionType) {
        throw SyntaxException("$target is not invokable", position)
    }

    var fnType = target
    var argumentTypes = arguments.map(Expr::type)
    if (fnType.genericParameters.isNotEmpty()) {
        val map = enhancedExtract(parser, fnType, arguments, returnType, position)
        if (map.size != fnType.genericParameters.size || !map.keys.containsAll(fnType.genericParameters)) {
            throw SyntaxException("Failed to infer generic parameters", position)
        }
        fnType = fnType.template(map)
    }

    if (fnType.argumentTypes.size == argumentTypes.size && argumentTypes.filterIsInstance<TypeTemplate>().isNotEmpty()) {
        argumentTypes = argumentTypes.zip(fnType.argumentTypes)
                .mapIndexed { i, (a, b) ->
                    a to try {
                        a.extract(b)
                    } catch (e: IllegalArgumentException) {
                        throw SyntaxException(e.message ?: "Failed to infer generic parameters", arguments[i].position)
                    }
                }.map { (a, map) ->
                    a.template(map)
                }
    }

    if (selfInvocation && (!fnType.selfArgument || !fnType.argumentTypes[0].isAssignableFrom(argumentTypes[0]))) {
        parser.errors += SyntaxException("$target can not be invoked in self position", position)
    }

    fnType.checkInvocation(argumentTypes, position)
    return fnType.returnType
}

fun enhancedExtract(parser: Parser, target: FunctionType, arguments: List<Expr>, returnType: Type?, position: Section): Map<GenericParameter, Type> {
    if (arguments.size < target.argumentTypes.size) {
        parser.errors += SyntaxException("Can not infer, insufficient arguments", position)
        return emptyMap()
    }

    val extracted = mutableMapOf<GenericParameter, Type>()

    target.argumentTypes.zip(arguments) { a, b ->
        val map = try {
            a.extract(b.type)
        } catch (e: IllegalArgumentException) {
            parser.errors += SyntaxException(e.message ?: "Inference failed", b.position)
            return@zip
        }

        extracted.mergeAll(map, Type::commonAssignableToType)
    }

    if (returnType != null) {
        if ((target.returnType as? TypeTemplate)?.genericParameters?.let { extracted.keys.containsAll(it) } == false) {
            try {
                extracted.mergeAll(target.returnType.extract(returnType)) { v1, _ -> v1 }
            } catch (e: IllegalArgumentException) {
                parser.errors += SyntaxException(e.message ?: "Inference failed", position)
            }
        }
    }

    return extracted
}