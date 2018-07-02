package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.DeclarationsAST
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.declarations.LetExpr
import xyz.avarel.lobos.ast.expr.declarations.ModuleExpr
import xyz.avarel.lobos.ast.expr.declarations.NamedFunctionExpr
import xyz.avarel.lobos.ast.expr.external.ExternalLetExpr
import xyz.avarel.lobos.ast.expr.external.ExternalNamedFunctionExpr
import xyz.avarel.lobos.ast.expr.misc.MultiExpr
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

val declarationTokens = listOf(
        TokenType.MOD,
        TokenType.LET,
        TokenType.DEF,
        TokenType.EXTERNAL,
        TokenType.TYPE
)

fun Parser.matchAllWhitespace(): Boolean {
    val type = arrayOf(TokenType.SEMICOLON, TokenType.NL)
    return if (nextIsAny(*type)) {
        do eat() while (nextIsAny(*type))
        true
    } else {
        false
    }
}

fun Parser.parseDeclarations(): DeclarationsAST {
    val expr = parseStatements(TokenType.L_BRACE to TokenType.R_BRACE, emptyList(), declarationTokens)

    val declarationsAST = DeclarationsAST()

    val list = if (expr is MultiExpr) {
        expr.list
    } else {
        listOf(expr)
    }

    list.forEach { it ->
        when (it) {
            is NamedFunctionExpr,
            is ExternalNamedFunctionExpr -> declarationsAST.functions += it
            is LetExpr,
            is ExternalLetExpr -> declarationsAST.variables += it
            is ModuleExpr -> declarationsAST.modules += it
            else -> throw IllegalStateException()
        }
    }

    return declarationsAST
}

fun Parser.parseBlock(
        modifiers: List<Modifier> = emptyList(),
        allowedTokens: List<TokenType> = emptyList()
): Expr {
    return parseStatements(TokenType.L_BRACE to TokenType.R_BRACE, modifiers, allowedTokens)
}

fun Parser.parseTypeAST(): AbstractTypeAST {
    return parseUnionTypeAST()
}

fun Parser.parseUnionTypeAST(): AbstractTypeAST {
    val type = parseSingleTypeAST()

    if (match(TokenType.PIPE)) {
        val list = mutableListOf<AbstractTypeAST>()
        list += type
        do {
            list += parseSingleTypeAST()
        } while (match(TokenType.PIPE))
        return list.reduce { acc, typeAST -> UnionTypeAST(acc, typeAST, acc.position.span(typeAST.position)) }
    }

    return type
}

fun Parser.parseSingleTypeAST(): AbstractTypeAST {
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
                typeParameters += parseUnionTypeAST()
            } while (match(TokenType.COMMA))
            val gt = eat(TokenType.GT)

            return TemplatingTypeAST(type, typeParameters, ident.position.span(gt.position))
        }
        match(TokenType.L_PAREN) -> {
            val lParen = last
            val valueTypes = mutableListOf<AbstractTypeAST>()

            if (!match(TokenType.R_PAREN)) {
                val firstType = parseTypeAST()
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
                        valueTypes += parseTypeAST()
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
            val returnType = parseTypeAST()
            FunctionTypeAST(valueTypes, returnType, position)
        }
        valueTypes.isEmpty() -> TupleTypeAST(position)
        else -> TupleTypeAST(valueTypes, position)
    }
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
            val parentType = parseTypeAST()
            GenericParameterAST(genericName, parentType)
        } else {
            GenericParameterAST(genericName)
        }

        genericParameters += param
    } while (match(TokenType.COMMA))

    eat(TokenType.GT)
    return genericParameters
}

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