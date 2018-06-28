package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.declarations.LetExpr
import xyz.avarel.lobos.ast.expr.external.ExternalLetExpr
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.lexer.span
import xyz.avarel.lobos.parser.*

object LetParser: PrefixParser {
    override fun parse(parser: Parser, modifiers: List<Modifier>, token: Token): Expr {
        val isMutable = parser.match(TokenType.MUT)

        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val type: AbstractTypeAST? = if (parser.match(TokenType.COLON)) parser.parseType() else null

        if (Modifier.EXTERNAL in modifiers) {
            if (type == null) throw SyntaxException("Type annotation required for extern definitions", token.position)
            return ExternalLetExpr(isMutable, name, type, token.span(type))
        }

        parser.eat(TokenType.ASSIGN)

        val expr = parser.parseExpr()
        return LetExpr(isMutable, name, type, expr, token.span(expr))
    }
}