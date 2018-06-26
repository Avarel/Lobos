package xyz.avarel.lobos.ast.misc

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type

class InvokeExpr(
        type: Type,
        val target: Expr,
        val arguments: List<Expr>,
        position: Section
) : AbstractExpr(type, arguments.lastOrNull()?.let(Expr::position)?.let(position::span) ?: position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
