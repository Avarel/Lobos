package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.ReturnExpr
import xyz.avarel.lobos.ast.nodes.UnitExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.typeCheck
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object ReturnParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token): Expr {
        val expr = if (parser.match(TokenType.SEMICOLON)) {
            UnitExpr(parser.last.position)
        } else {
            parser.parseExpr(scope, ctx)
        }

        if (scope.expectedReturnType == null) {
            throw SyntaxException("Return is not valid in this context", token.position)
        }

        typeCheck(scope.expectedReturnType!!, expr.type, expr.position)

        scope.terminates = true
        return ReturnExpr(expr, token.position)
    }
}