package xyz.avarel.lobos.ast.misc

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor

class MultiExpr(val list: List<Expr>): AbstractExpr(list.last().type, list.first().position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
