package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ModuleExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.parseBlock
import xyz.avarel.lobos.typesystem.base.ModuleType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object ModuleParser : PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        if (stmt.expectedType != null) throw SyntaxException("Not an expression", token.position)

        val name = parser.eat(TokenType.IDENT).string

        val scopeContext = scope.subContext()
        val body = parser.parseBlock(scopeContext)

        scope.putVariable(name, ModuleType(name, scopeContext), false)

        return ModuleExpr(name, body, token.position)
    }
}