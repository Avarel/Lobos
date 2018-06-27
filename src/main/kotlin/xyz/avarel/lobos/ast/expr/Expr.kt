package xyz.avarel.lobos.ast.expr

import xyz.avarel.lobos.ast.Positional

interface Expr : Positional {
    fun <R> accept(visitor: ExprVisitor<R>): R
}