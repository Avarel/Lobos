package xyz.avarel.lobos.ast.expr.access

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class IndexAssignExpr(
        val target: Expr,
        val index: Expr,
        val value: Expr,
        position: Section
) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}