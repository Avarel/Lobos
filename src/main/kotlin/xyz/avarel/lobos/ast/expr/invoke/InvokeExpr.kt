package xyz.avarel.lobos.ast.expr.invoke

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class InvokeExpr(
        val target: Expr,
        val arguments: List<Expr>,
        section: Section
) : AbstractExpr(section) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
