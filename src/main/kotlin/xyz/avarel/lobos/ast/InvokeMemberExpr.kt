package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.Type

class InvokeMemberExpr(type: Type, val target: Expr, val name: String, val arguments: List<Expr>, position: Section) : AbstractExpr(type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}