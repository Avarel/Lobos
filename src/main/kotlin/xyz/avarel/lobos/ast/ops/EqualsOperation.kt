package xyz.avarel.lobos.ast.ops

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.BoolType

class EqualsOperation(
        val left: Expr,
        val right: Expr,
        position: Position
): AbstractExpr(BoolType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
