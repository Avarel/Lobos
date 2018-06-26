package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type

class PropertyAccessExpr(
        type: Type,
        val target: Expr,
        val name: String,
        position: Section
): AbstractExpr(type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}