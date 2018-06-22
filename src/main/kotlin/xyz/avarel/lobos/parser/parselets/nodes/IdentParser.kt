package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object IdentParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val name = token.string

        val effectiveType = scope.getEffectiveType(name)

        if (effectiveType != null) {
            if (parser.match(TokenType.ASSIGN)) {
                if (stmt.expectedType != null) {
                    throw SyntaxException("Not an expression", token.position)
                }

                val expr = parser.parseExpr(scope, stmt)

                val currentInfo = scope.getVariable(name)!!

                if (!currentInfo.mutable) {
                    throw SyntaxException("Reference $name is not mutable", token.position)
                }

                val exprType = inferGeneric(scope.getVariable(name)!!.type, expr.type, token.position)

                typeCheck(currentInfo.type, exprType, expr.position)

                scope.assumptions[name] = VariableInfo(currentInfo.mutable, expr.type)

                return AssignExpr(name, expr, token.position)
            }

            return IdentExpr(name, stmt.assumptions[name]?.type ?: effectiveType.type, token.position)
        } else {
            throw SyntaxException("Unresolved reference $name", token.position)
        }
    }
}
