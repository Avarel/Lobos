package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.Modifier
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object ExternalParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        if (stmt.expectedType != null) {
            throw SyntaxException("Not an expression", token.position)
        }

        return parser.parseExpr(scope, StmtContext(stmt.modifiers + Modifier.EXTERNAL))
    }
}