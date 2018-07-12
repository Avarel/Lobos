package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.lexer.Section

class DeclareLetExpr(
        val mutable: Boolean,
        val name: String,
        val type: AbstractTypeAST?,
        val value: Expr,
        section: Section
) : AbstractExpr(section), LetExpr {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}