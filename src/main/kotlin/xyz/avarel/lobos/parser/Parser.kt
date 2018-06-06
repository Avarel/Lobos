package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.UnitExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

class Parser(val grammar: Grammar, val tokens: List<Token>) {
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

    fun skipTillNextIs(type: TokenType) {
        while (!eof) {
            if (nextIs(type)) break
            eat()
        }
    }

    fun parse(scope: ScopeContext): Expr {
        val expr = parseStatements(scope)

        if (!eof) {
            println(peek().type)
            errors.add(SyntaxException("Did not reach end of file", peek().position))
        }

        return expr
    }

    fun parseStatements(scope: ScopeContext, delimiterPair: Pair<TokenType, TokenType>? = null): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        delimiterPair?.let {
            eat(it.first)
            if (match(it.second)) {
                return UnitExpr(last.position)
            }
        }

        val expr = parseExpr(scope, StmtContext())
        if (expr is InvalidExpr) skipTillNextIs(TokenType.SEMICOLON)

        if (!eof && match(TokenType.SEMICOLON)) {
            val list = mutableListOf<Expr>()
            list.add(expr)

            do {
                if (eof) break
                if (delimiterPair != null && match(delimiterPair.second)) {
                    list.add(UnitExpr(last.position))
                    break
                }
                parseExpr(scope, StmtContext()).let {
                    if (it is InvalidExpr) skipTillNextIs(TokenType.SEMICOLON)
                    list.add(it)
                }
            } while (!eof && match(TokenType.SEMICOLON))

            return MultiExpr(list)
        }

        delimiterPair?.let {
            eat(delimiterPair.second)
        }

        return expr
    }

    fun peek(distance: Int = 0) = tokens[index + distance]

    fun nextIs(type: TokenType) = !eof && peek().type == type

    fun parseExpr(scope: ScopeContext, ctx: StmtContext, precedence: Int = 0): Expr {
        if (eof) throw SyntaxException("Expected expression but reached end of file", last.position)

        val token = eat()
        val parser = grammar.prefixParsers[token.type] ?: let {
            errors.add(SyntaxException("Unexpected $token", token.position))
            return InvalidExpr(token.position)
        }
        val expr = try {
            parser.parse(this, scope, ctx, token)
        } catch (e: SyntaxException) {
            errors.add(e)
            return InvalidExpr(token.position)
        }

        return parseInfix(scope, ctx, precedence, expr)
    }

    fun parseInfix(scope: ScopeContext, ctx: StmtContext, precedence: Int, left: Expr): Expr {
        var leftExpr = left
        while (!eof && precedence < this.precedence) {
            val token = eat()
            val parser = grammar.infixParsers[token.type] ?: let {
                errors.add(SyntaxException("Unexpected $token", token.position))
                return InvalidExpr(token.position)
            }

            leftExpr = try {
                parser.parse(this, scope, ctx, token, leftExpr)
            } catch (e: SyntaxException) {
                errors.add(e)
                return InvalidExpr(token.position)
            }
        }
        return leftExpr
    }
}