package xyz.avarel.lobos.ast.expr.nodes

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class TupleExpr(val list: List<Expr>, position: Section) : AbstractExpr(position) {
    constructor(position: Section) : this(emptyList(), position)
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}