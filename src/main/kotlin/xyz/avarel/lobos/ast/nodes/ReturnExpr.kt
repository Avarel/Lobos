package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.NeverType

class ReturnExpr(val expr: Expr, position: Section) : AbstractExpr(NeverType, position.span(expr.position)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}