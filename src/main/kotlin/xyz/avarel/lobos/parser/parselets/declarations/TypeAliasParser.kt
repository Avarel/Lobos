package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.DummyExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.parseGenericArgumentsScope
import xyz.avarel.lobos.parser.parseType
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

object TypeAliasParser: PrefixParser {
    override fun parse(parser: Parser, scope: ScopeContext, stmt: StmtContext, token: Token): Expr {
        val ident = parser.eat(TokenType.IDENT)
        val name = ident.string

        val (genericParameters, typeScope) = parser.parseGenericArgumentsScope(scope) ?: emptyList<GenericParameter>() to scope

        parser.eat(TokenType.ASSIGN)

        val type = parser.parseType(typeScope)

        if (type is TypeTemplate) {
            type.genericParameters += genericParameters
        }

        scope.types[name] = type

        return DummyExpr
    }
}