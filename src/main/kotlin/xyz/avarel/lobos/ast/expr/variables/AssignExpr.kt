package xyz.avarel.lobos.ast.expr.variables

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class AssignExpr(val name: String, val value: Expr, position: Section) : AbstractExpr(position.span(value.position)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}