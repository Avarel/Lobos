package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.ReturnExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.typeCheck
import xyz.avarel.lobos.typesystem.scope.ParserContext

object ReturnParser: PrefixParser {
    override fun parse(parser: Parser, scope: ParserContext, token: Token): Expr {
        val expr = parser.parseExpr(scope)

        if (scope.expectedReturnType == null) {
            throw SyntaxException("Return is not valid in this context", token.position)
        }

        typeCheck(scope.expectedReturnType!!, expr.type, expr.position)

        scope.terminates = true
        return ReturnExpr(expr, token.position)
    }
}