package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.GenericParameterAST
import xyz.avarel.lobos.lexer.Section

class TypeAliasExpr(
        val name: String,
        val generics: List<GenericParameterAST>,
        val type: AbstractTypeAST,
        position: Section
) : AbstractExpr(position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}