package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.NullExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object NullParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        return NullExpr(token.position)
    }
}
