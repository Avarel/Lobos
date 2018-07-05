package xyz.avarel.lobos.ast.expr.external

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.lexer.Section

class ExternalLetExpr(
        val mutable: Boolean,
        val name: String,
        val type: AbstractTypeAST,
        position: Section
) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}