package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.ArgumentParameterAST
import xyz.avarel.lobos.ast.types.GenericParameterAST
import xyz.avarel.lobos.lexer.Section

class NamedFunctionExpr(
        val name: String,
        val generics: List<GenericParameterAST>,
        val arguments: List<ArgumentParameterAST>,
        val returnType: AbstractTypeAST,
        val body: Expr,
        position: Section
) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}