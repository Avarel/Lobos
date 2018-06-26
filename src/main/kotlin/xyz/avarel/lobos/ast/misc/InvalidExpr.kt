package xyz.avarel.lobos.ast.misc

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.InvalidType

class InvalidExpr(position: Section) : AbstractExpr(InvalidType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}