package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.misc.InvalidExpr
import xyz.avarel.lobos.ast.expr.misc.MultiExpr
import xyz.avarel.lobos.ast.expr.nodes.TupleExpr
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.lexer.Tokenizer

class Parser(val grammar: Grammar, val fileName: String, val tokens: List<Token>) {
    constructor(grammar: Grammar, lexer: Tokenizer): this(grammar, lexer.fileName, lexer.parse())

    val errors = mutableListOf<SyntaxException>()

    private var index: Int = 0
    val eof get() = index == tokens.size
    val last get() = tokens[index - 1]

    private val precedence get() = grammar.infixParsers[peek(0).type]?.precedence ?: 0

    fun eat() = tokens[index++]

    fun eat(type: TokenType): Token {
        if (eof) throw SyntaxException("Expected $type but reached end of file", last.position)
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

    fun parse(): Expr {
        if (eof) return TupleExpr(Section(fileName, 0, 0, 0))
        val expr = parseStatements()

        if (!eof) {
            val token = peek()
            errors += SyntaxException("Did not reach end of file. Found token $token", token.position)
        }

        return expr
    }

    fun parseStatements(delimiterPair: Pair<TokenType, TokenType>? = null): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        delimiterPair?.first?.let(this::eat)
        matchCompleteAny(TokenType.SEMICOLON, TokenType.NL)

        val list = mutableListOf<Expr>()
        matchCompleteAny(TokenType.SEMICOLON, TokenType.NL)

        do {
            if (eof || (delimiterPair != null && nextIs(delimiterPair.second))) {
                break
            }
            val expr = parseExpr()

            if (expr is InvalidExpr) {
                if (delimiterPair != null) {
                    skipUntil(delimiterPair.second, TokenType.SEMICOLON, TokenType.NL)
                } else {
                    skipUntil(TokenType.SEMICOLON, TokenType.NL)
                }
            } else {
                list += expr
            }
        } while (!eof && matchCompleteAny(TokenType.SEMICOLON, TokenType.NL))

        delimiterPair?.second?.let(this::eat)

        return when {
            list.isEmpty() -> TupleExpr(last.position)
            list.size == 1 -> list[0]
            else -> MultiExpr(list)
        }
    }

    fun parseExpr(precedence: Int = 0, modifiers: List<Modifier> = emptyList()): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        val token = eat()
        val parser = grammar.prefixParsers[token.type] ?: let {
            errors += SyntaxException("Unexpected $token", token.position)
            return InvalidExpr(token.position)
        }
        val expr = try {
            parser.parse(this, modifiers, token)
        } catch (e: SyntaxException) {
            errors += e
            return InvalidExpr(e.position)
        }

        return parseInfix(precedence, expr)
    }

    fun parseInfix(precedence: Int, left: Expr): Expr {
        var leftExpr = left
        while (!eof && precedence < this.precedence) {
            val token = eat()
            val parser = grammar.infixParsers[token.type] ?: let {
                errors += SyntaxException("Unexpected $token", token.position)
                return InvalidExpr(token.position)
            }

            leftExpr = try {
                parser.parse(this, token, leftExpr)
            } catch (e: SyntaxException) {
                errors += e
                return InvalidExpr(e.position)
            }
        }
        return leftExpr
    }
}