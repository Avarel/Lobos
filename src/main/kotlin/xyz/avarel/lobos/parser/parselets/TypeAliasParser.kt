package xyz.avarel.lobos.parser.parselets

import xyz.avarel.lobos.ast.DummyExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.parseType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object TypeAliasParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, ctx: StmtContext, token: Token): Expr {
        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val type = if (parser.match(TokenType.LT)) {
            val subScope = scope.subContext()

            do {
                val genericToken = parser.eat(TokenType.IDENT)
                val gname = genericToken.string

                if (gname in subScope.types) {
                    parser.errors += SyntaxException("Generic parameter $gname has already been declared", genericToken.position)
                }

                if (parser.match(TokenType.COLON)) {
                    val parentType = parser.parseType(subScope)
                    println(parentType)
                    subScope.types[gname] = GenericType(GenericParameter(gname, parentType))
                } else {
                    subScope.types[gname] = GenericType(GenericParameter(gname))
                }
            } while (parser.match(TokenType.COMMA))

            parser.eat(TokenType.GT)

            parser.eat(TokenType.ASSIGN)
            parser.parseType(subScope)
        } else {
            parser.eat(TokenType.ASSIGN)
            parser.parseType(scope)
        }
        scope.types[name] = type

        return DummyExpr
    }
}