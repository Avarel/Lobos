package xyz.avarel.lobos.ast.expr.nodes

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class ReturnExpr(val expr: Expr, position: Section) : AbstractExpr(position.span(expr.position)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}