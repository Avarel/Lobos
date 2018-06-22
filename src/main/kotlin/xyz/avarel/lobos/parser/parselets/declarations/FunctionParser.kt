package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.DummyExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.NamedFunctionExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.UnitType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.scope.Modifier
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext
import xyz.avarel.lobos.typesystem.scope.VariableInfo
import xyz.avarel.lobos.typesystem.transformToBodyType

object FunctionParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        if (name in scope.variables) {
            throw SyntaxException("Variable $name has already been declared", ident.position)
        }

        val (genericParameters, argumentScope) = parser.parseGenericArgumentsScope(scope) ?: emptyList<GenericParameter>() to scope

        val bodyScope = argumentScope.subContext()
        val parameters = mutableMapOf<String, Type>()

        parser.eat(TokenType.L_PAREN)
        if (!parser.match(TokenType.R_PAREN)) {
            do {
                val mutable = parser.match(TokenType.MUT)

                val paramIdent = parser.eat(TokenType.IDENT)
                val paramName = paramIdent.string

                parser.eat(TokenType.COLON)
                val type = parser.parseType(argumentScope)

                if (paramName in bodyScope.variables) {
                    parser.errors += SyntaxException("Parameter $paramName has already been declared", paramIdent.position)
                } else {
                    bodyScope.variables[paramName] = VariableInfo(mutable, type.transformToBodyType())
                    parameters[paramName] = type
                }
            } while (parser.match(TokenType.COMMA))

            parser.eat(TokenType.R_PAREN)
        }

        val returnType = if (parser.match(TokenType.ARROW)) {
            parser.parseType(argumentScope)
        } else {
            UnitType
        }

        if (Modifier.EXTERN in stmt.modifiers) {
            val type = FunctionType(false, parameters.values.toList(), returnType)

            type.genericParameters = genericParameters

            scope.variables[name] = VariableInfo(false, type)

            return DummyExpr
        }

        bodyScope.expectedReturnType = returnType

        val body = parser.parseStatements(bodyScope, TokenType.L_BRACE to TokenType.R_BRACE)

        if (!bodyScope.terminates) {
            parser.continuableTypeCheck(returnType, body.type, (body as? MultiExpr)?.list?.last()?.position ?: body.position)
        }

        val type = FunctionType(false, parameters.values.toList(), returnType)

        type.genericParameters = genericParameters

        scope.variables[name] = VariableInfo(false, type)
        return NamedFunctionExpr(name, parameters, returnType, body, token.position)
    }
}