package xyz.avarel.lobos.ast.types

import xyz.avarel.lobos.lexer.Positional

interface TypeAST : Positional {
    fun <R> accept(visitor: TypeASTVisitor<R>): R
}