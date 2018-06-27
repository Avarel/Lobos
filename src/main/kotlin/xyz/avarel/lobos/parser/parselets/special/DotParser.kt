package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.access.PropertyAccessExpr
import xyz.avarel.lobos.ast.expr.access.TupleIndexAccessExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.SyntaxException

object DotParser: InfixParser {
    override val precedence: Int get() = Precedence.DOT

    override fun parse(parser: Parser, token: Token, left: Expr): Expr {
        val ident = parser.eat()
        return when (ident.type) {
            TokenType.INT -> {
                val index = ident.string.toInt()
                TupleIndexAccessExpr(left, index, token.position)
            }
            TokenType.IDENT -> {
                val name = ident.string
                PropertyAccessExpr(left, name, token.position)
            }
            else -> throw SyntaxException("Invalid identifier", ident.position)
        }
    }
}