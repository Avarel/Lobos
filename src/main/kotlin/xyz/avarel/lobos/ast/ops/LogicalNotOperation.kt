package xyz.avarel.lobos.ast.ops

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.BoolType

class LogicalNotOperation(val target: Expr, position: Section) : AbstractExpr(BoolType, position.span(target.position)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}