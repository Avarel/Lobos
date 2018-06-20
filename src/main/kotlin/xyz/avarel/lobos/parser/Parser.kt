package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.DummyExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.UnitExpr
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.lexer.Tokenizer
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

class Parser(val grammar: Grammar, val fileName: String, val tokens: List<Token>) {
    constructor(grammar: Grammar, lexer: Tokenizer): this(grammar, lexer.fileName, lexer.parse())

    val errors = mutableListOf<SyntaxException>()

    private var index: Int = 0
    val eof get() = index == tokens.size
    val last get() = tokens[index - 1]

    private val precedence get() = grammar.infixParsers[peek(0).type]?.precedence ?: 0

    fun eat() = tokens[index++]

    fun eat(type: TokenType): Token {
        val token = peek()
        if (token.type != type) {
            throw SyntaxException("Expected $type but found ${token.type}", token.position)
        }
        return eat()
    }

    fun match(type: TokenType): Boolean {
        return if (nextIs(type)) {
            eat()
            true
        } else {
            false
        }
    }

    fun matchAny(vararg type: TokenType): Boolean {
        return if (nextIsAny(*type)) {
            eat()
            true
        } else {
            false
        }
    }

    fun matchComplete(type: TokenType): Boolean {
        return if (nextIs(type)) {
            while (nextIs(type)) {
                eat()
            }
            true
        } else {
            false
        }
    }

    fun matchCompleteAny(vararg type: TokenType): Boolean {
        return if (nextIsAny(*type)) {
            while (nextIsAny(*type)) {
                eat()
            }
            true
        } else {
            false
        }
    }

    fun peek(distance: Int = 0) = tokens[index + distance]

    fun nextIs(type: TokenType) = !eof && peek().type == type

    fun nextIsAny(vararg types: TokenType) = !eof && types.any { nextIs(it) }

    fun peekAheadUntil(vararg type: TokenType): List<Token> {
        if (eof) return emptyList()
        val list = mutableListOf<Token>()
        var distance = 0
        while (!eof && !nextIsAny(*type)) {
            list += peek(distance++)
        }
        return list
    }

    fun skipUntil(vararg type: TokenType) {
        while (!eof && !nextIsAny(*type)) {
            eat()
        }
    }

    fun parse(scope: ScopeContext): Expr {
        if (eof) return UnitExpr(Position(fileName, 0, 0))
        val expr = parseStatements(scope)

        if (!eof) {
            val token = peek()
            errors += SyntaxException("Did not reach end of file. Found token $token", token.position)
        }

        return expr
    }

    fun parseStatements(scope: ScopeContext, delimiterPair: Pair<TokenType, TokenType>? = null): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        delimiterPair?.first?.let(this::eat)

        val list = mutableListOf<Expr>()

        do {
            if (eof || (delimiterPair != null && nextIs(delimiterPair.second))) {
                break
            }
            val expr = parseExpr(scope, StmtContext())

            if (expr is InvalidExpr) {
                if (delimiterPair != null) {
                    skipUntil(delimiterPair.second, TokenType.SEMICOLON, TokenType.NL)
                } else {
                    skipUntil(TokenType.SEMICOLON, TokenType.NL)
                }
            } else if (expr !== DummyExpr) {
                list += expr
            }
        } while (!eof && matchCompleteAny(TokenType.SEMICOLON, TokenType.NL))

        delimiterPair?.second?.let(this::eat)

        return when {
            list.isEmpty() -> UnitExpr(last.position)
            list.size == 1 -> list[0]
            else -> MultiExpr(list)
        }
    }

    fun parseExpr(scope: ScopeContext, ctx: StmtContext, precedence: Int = 0): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        val token = eat()
        val parser = grammar.prefixParsers[token.type] ?: let {
            errors += SyntaxException("Unexpected $token", token.position)
            return InvalidExpr(token.position)
        }
        val expr = try {
            parser.parse(this, scope, ctx, token)
        } catch (e: SyntaxException) {
            errors += e
            return InvalidExpr(token.position)
        }

        return when {
            expr === DummyExpr -> expr
            else -> parseInfix(scope, ctx, precedence, expr)
        }
    }

    fun parseInfix(scope: ScopeContext, ctx: StmtContext, precedence: Int, left: Expr): Expr {
        var leftExpr = left
        while (!eof && precedence < this.precedence) {
            val token = eat()
            val parser = grammar.infixParsers[token.type] ?: let {
                errors += SyntaxException("Unexpected $token", token.position)
                return InvalidExpr(token.position)
            }

            leftExpr = try {
                parser.parse(this, scope, ctx, token, leftExpr)
            } catch (e: SyntaxException) {
                errors += e
                return InvalidExpr(token.position)
            }
        }
        return leftExpr
    }
}