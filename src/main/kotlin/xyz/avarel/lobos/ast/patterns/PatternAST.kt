package xyz.avarel.lobos.ast.patterns

import xyz.avarel.lobos.lexer.Sectional
import xyz.avarel.lobos.lexer.TokenType
import xyz.avarel.lobos.parser.Parser
import xyz.avarel.lobos.parser.SyntaxException
import xyz.avarel.lobos.parser.parseTypeAST

interface PatternAST : Sectional {
    fun <R> accept(visitor: PatternVisitor<R>): R
}
