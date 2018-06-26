package xyz.avarel.lobos.parser.parselets.nodes

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object IdentParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val name = token.string

        val effectiveType = scope.getAssumption(name)

        if (effectiveType != null) {
            if (parser.match(TokenType.ASSIGN)) {
                if (stmt.expectedType != null) {
                    throw SyntaxException("Not an expression", token.position)
                }

                val expr = parser.parseExpr(scope, stmt)

                val (declaredType, isMutable) = scope.getDeclaration(name)!!

                if (!isMutable) {
                    throw SyntaxException("Reference $name is not mutable", token.position.span(expr.position))
                }

                val exprType = enhancedInfer(parser, scope.getVariable(name)!!, expr.type, token.position)

                typeCheck(declaredType, exprType, expr.position)

                scope.assumptions[name] = exprType

                return AssignExpr(name, expr, token.position)
            }

            return IdentExpr(name, stmt.assumptions[name] ?: effectiveType, token.position)
        } else {
            throw SyntaxException("Unresolved reference $name", token.position)
        }
    }
}
