package xyz.avarel.lobos.parser.parselets.special

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.InfixParser
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.Precedence
import xyz.avarel.lobos.parser.enhancedCheckInvocation
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object InvocationParser: InfixParser {
    override val precedence: Int = Precedence.POSTFIX

    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token, left: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!parser.match(TokenType.R_PAREN)) {
            do {
                arguments.add(parser.parseExpr(scope, StmtContext()))
            } while (parser.match(TokenType.COMMA))
            parser.eat(TokenType.R_PAREN)
        }

        val returnType = enhancedCheckInvocation(parser, left.type, arguments, ctx.expectedType, token.position)

        return InvokeExpr(returnType, left, arguments, token.position)
    }
}