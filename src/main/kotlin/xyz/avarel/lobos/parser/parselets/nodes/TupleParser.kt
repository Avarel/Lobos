package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.nodes.TupleExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Modifier
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser

object TupleParser: PrefixParser {
    override fun parse(parser: Parser, modifiers: List<Modifier>, token: Token): Expr {
        if (parser.match(TokenType.R_PAREN)) {
            return TupleExpr(emptyList(), token.position.span(parser.last.position))
        }

        val expr = parser.parseExpr()

        if (parser.match(TokenType.COMMA)) {
            val exprValues = mutableListOf<Expr>()
            exprValues.add(expr)

            if (!parser.match(TokenType.R_PAREN)) {
                do {
                    exprValues.add(parser.parseExpr())
                } while (parser.match(TokenType.COMMA))
                parser.eat(TokenType.R_PAREN)
            }

            return TupleExpr(exprValues, token.position.span(parser.last.position))
        }

        parser.eat(TokenType.R_PAREN)
        return expr
    }
}