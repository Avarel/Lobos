package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.typesystem.scope.ParserContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object IdentParser: PrefixParser {
    override fun parse(parser: Parser, scope: ParserContext, token: Token): Expr {
        val name = token.string!!

        val effectiveType = scope.getEffectiveType(name)

        if (effectiveType != null) {
            if (parser.match(TokenType.ASSIGN)) {
                val expr = parser.parseExpr(scope)

                val currentInfo = scope.getVariable(name)!!

                if (!currentInfo.type.isAssignableFrom(expr.type)) {
                    throw SyntaxException("Required type: ${currentInfo.type} | Found type: ${expr.type}", expr.position)
                }

                scope.setAssumption(name, VariableInfo(currentInfo.mutable, expr.type))

                return AssignExpr(name, expr, token.position)
            }

            return IdentExpr(name, effectiveType.type, token.position)
        } else {
            throw SyntaxException("Unresolved reference $name", token.position)
        }
    }
}
