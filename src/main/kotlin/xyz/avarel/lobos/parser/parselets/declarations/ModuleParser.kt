package xyz.avarel.lobos.parser.parselets.declarations

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.declarations.ModuleExpr
import xyz.avarel.lobos.lexer.Token
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Modifier
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.PrefixParser
import xyz.avarel.lobos.parser.parseDeclarations

object ModuleParser : PrefixParser {
    override fun parse(parser: Parser, modifiers: List<Modifier>, token: Token): Expr {
        val name = parser.eat(TokenType.IDENT).string

        val declarations = parser.parseDeclarations()

        return ModuleExpr(name, declarations, token.position)
        // TODO external modules?
    }
}