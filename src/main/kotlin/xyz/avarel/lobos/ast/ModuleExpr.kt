package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.InvalidType

class ModuleExpr(val name: String, val body: Expr, position: Section) : AbstractExpr(InvalidType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
