package xyz.avarel.lobos.ast.variables

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.InvalidType

class LetExpr(val name: String, val expr: Expr, position: Section) : AbstractExpr(InvalidType, position.span(expr.position)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}