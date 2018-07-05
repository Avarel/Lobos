package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.expr.Expr

@Suppress("UNCHECKED_CAST")
class DeclarationsAST(
        val modules: MutableList<Expr> = mutableListOf(),
        val functions: MutableList<Expr> = mutableListOf(),
        val variables: MutableList<Expr> = mutableListOf()
// TODO typealiases
)