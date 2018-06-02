package xyz.avarel.lobos.ast.misc

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.InvalidType

class InvalidExpr(position: Position): AbstractExpr(InvalidType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}