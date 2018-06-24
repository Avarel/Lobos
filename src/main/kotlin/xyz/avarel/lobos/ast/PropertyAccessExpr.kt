package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.Type

class PropertyAccessExpr(
        type: Type,
        val target: Expr,
        val name: String,
        position: Position
): AbstractExpr(type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}