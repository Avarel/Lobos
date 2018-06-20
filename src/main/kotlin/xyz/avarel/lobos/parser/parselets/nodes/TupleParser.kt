package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.TupleExpr
import xyz.avarel.lobos.ast.nodes.UnitExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object TupleParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        if (parser.match(TokenType.R_PAREN)) {
            return UnitExpr(token.position)
        }

        val expr = parser.parseExpr(scope, StmtContext())

        if (parser.match(TokenType.COMMA)) {
            val exprValues = mutableListOf<Expr>()
            exprValues.add(expr)

            if (!parser.match(TokenType.R_PAREN)) {
                do {
                    exprValues.add(parser.parseExpr(scope, stmt))
                } while (parser.match(TokenType.COMMA))
                parser.eat(TokenType.R_PAREN)
            }

            return TupleExpr(exprValues, token.position)
        }

        parser.eat(TokenType.R_PAREN)
        return expr
    }
}