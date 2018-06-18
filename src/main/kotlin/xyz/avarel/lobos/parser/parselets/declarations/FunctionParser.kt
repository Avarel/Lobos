package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.NamedFunctionExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.UnitType
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object FunctionParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token): Expr {
        val name: String? = if (parser.match(TokenType.IDENT)) {
            val ident = parser.last
            val name = ident.string

            if (name in scope.variables) {
                throw SyntaxException("Variable $name has already been declared", ident.position)
            }

            name
        } else {
            TODO()
        }

        val typeScope = parser.parseGenericArgumentsScope(scope) ?: scope

        val bodyScope = typeScope.subContext()
        val parameters = mutableMapOf<String, Type>()

        parser.eat(TokenType.L_PAREN)

        if (!parser.match(TokenType.R_PAREN)) {
            do {
                val mutable = parser.match(TokenType.MUT)

                val paramIdent = parser.eat(TokenType.IDENT)
                val paramName = paramIdent.string

                parser.eat(TokenType.COLON)
                val type = parser.parseType(typeScope)

                if (paramName in bodyScope.variables) {
                    parser.errors += SyntaxException("Parameter $paramName has already been declared", paramIdent.position)
                } else {
                    bodyScope.variables[paramName] = VariableInfo(mutable, type)
                    parameters[paramName] = type
                }
            } while (parser.match(TokenType.COMMA))

            parser.eat(TokenType.R_PAREN)
        }

        val returnType = if (parser.match(TokenType.ARROW)) {
            parser.parseType(typeScope)
        } else {
            UnitType
        }

        bodyScope.expectedReturnType = returnType

        val body = parser.parseStatements(bodyScope, TokenType.L_BRACE to TokenType.R_BRACE)

        if (!bodyScope.terminates) {
            parser.continuableTypeCheck(returnType, body.type, (body as? MultiExpr)?.list?.last()?.position ?: body.position)
        }

        if (name != null) {
            scope.variables[name] = VariableInfo(false, FunctionType(false, parameters.values.toList(), returnType))
            return NamedFunctionExpr(name, parameters, returnType, body, token.position)
        } else {
            TODO()
        }
    }
}