package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.nodes.F64Expr
import xyz.avarel.lobos.ast.nodes.I32Expr
import xyz.avarel.lobos.ast.nodes.I64Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.typesystem.base.F64Type
import xyz.avarel.lobos.typesystem.base.I64Type
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object IntParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        return when (stmt.expectedType) {
            F64Type -> {
                val value = token.string.toDouble()
                F64Expr(value, token.position)
            }
            I64Type -> {
                val value = token.string.toLong()
                I64Expr(value, token.position)
            }
            else -> {
                val value = token.string.toInt()
                I32Expr(value, token.position)
            }
        }
    }
}
