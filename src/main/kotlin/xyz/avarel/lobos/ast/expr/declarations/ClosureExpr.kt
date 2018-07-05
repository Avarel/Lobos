package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.ArgumentParameterAST
import xyz.avarel.lobos.lexer.Section

class ClosureExpr(
        val arguments: List<ArgumentParameterAST>,
        val body: Expr,
        position: Section
) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}