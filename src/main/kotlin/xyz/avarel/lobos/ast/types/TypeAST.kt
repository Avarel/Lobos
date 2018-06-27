package xyz.avarel.lobos.ast.types

import xyz.avarel.lobos.ast.Positional

interface TypeAST : Positional {
    fun <R> accept(visitor: TypeVisitor<R>): R
}