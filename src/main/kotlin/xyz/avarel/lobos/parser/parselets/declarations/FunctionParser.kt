package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.declarations.NamedFunctionExpr
import xyz.avarel.lobos.ast.expr.external.ExternalNamedFunctionExpr
import xyz.avarel.lobos.ast.types.ArgumentParameterAST
import xyz.avarel.lobos.ast.types.complex.TupleTypeAST
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.*

object FunctionParser: PrefixParser {
    override fun parse(parser: Parser, modifiers: List<Modifier>, token: Token): Expr {
        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val generics = parser.parseGenericParameters()
        val arguments = mutableMapOf<String, ArgumentParameterAST>()

        parser.eat(TokenType.L_PAREN)
        if (!parser.match(TokenType.R_PAREN)) {
            do {
//                val isMutable = parser.match(TokenType.MUT)

                val paramIdent = parser.eat(TokenType.IDENT)
                val paramName = paramIdent.string

                parser.eat(TokenType.COLON)
                val type = parser.parseTypeAST()

                if (paramName in arguments) {
                    parser.errors += SyntaxException("Parameter $paramName has already been declared", paramIdent.position)
                }

                arguments[paramName] = ArgumentParameterAST(paramName, type)
            } while (parser.match(TokenType.COMMA))

            parser.eat(TokenType.R_PAREN)
        }

        val returnType = if (parser.match(TokenType.ARROW)) {
            parser.parseTypeAST()
        } else {
            TupleTypeAST(parser.last.position)
        }

        if (Modifier.EXTERNAL in modifiers) {
            return ExternalNamedFunctionExpr(name, generics, arguments.values.toList(), returnType, token.position)
        }

        val body = parser.parseBlock()

        return NamedFunctionExpr(name, generics, arguments.values.toList(), returnType, body, token.position)
    }
}