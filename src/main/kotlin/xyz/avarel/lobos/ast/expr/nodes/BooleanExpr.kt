package xyz.avarel.lobos.ast.expr.nodes

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class BooleanExpr(val value: Boolean, position: Section) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}