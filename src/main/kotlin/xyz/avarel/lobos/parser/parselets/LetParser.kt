package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.variables.LetExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.parseType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object LetParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token): Expr {
        if (ctx.expectedType != null) {
            throw SyntaxException("Not an expression", token.position)
        }

        val mutable = parser.match(TokenType.MUT)

        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val type: Type? = if (parser.match(TokenType.COLON)) parser.parseType(scope) else null

        parser.eat(TokenType.ASSIGN)

        val expr = parser.parseExpr(scope, StmtContext(AnyType))

        if (name in scope.variables) {
            throw SyntaxException("Variable $name has already been declared", ident.position)
        } else {
            if (type != null) {
                if (type.isAssignableFrom(expr.type)) {
                    scope.variables[name] = VariableInfo(mutable, type)
                    if (expr.type != type) { // If assumption is necessary
//                        scope.assumptions[name] = VariableInfo(mutable, expr.type)
                    }
                } else {
                    scope.variables[name] = VariableInfo(mutable, type)
                    throw SyntaxException("Expected $type but found ${expr.type}", expr.position)
                }
            } else {
                assert(expr.type.universalType.isAssignableFrom(expr.type))
                scope.variables[name] = VariableInfo(mutable, expr.type.universalType)
//                scope.assumptions[name] = VariableInfo(mutable, expr.type)
            }
            return LetExpr(name, expr, token.position)
        }
    }
}