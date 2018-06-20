package xyz.avarel.lobos.parser

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

interface PrefixParser {
    fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr
}