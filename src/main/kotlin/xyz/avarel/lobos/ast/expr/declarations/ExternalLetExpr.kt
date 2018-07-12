package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.lexer.Section

class ExternalLetExpr(
        val mutable: Boolean,
        val name: String,
        val type: AbstractTypeAST,
        section: Section
) : AbstractExpr(section), LetExpr {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}