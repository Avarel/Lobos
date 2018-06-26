package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.InvalidType

class NamedFunctionExpr(
        val name: String,
        val parameters: Map<String, Type>,
        val returnType: Type,
        val body: Expr,
        position: Section
): AbstractExpr(InvalidType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}