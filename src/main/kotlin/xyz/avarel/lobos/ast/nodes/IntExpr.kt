package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.LiteralIntType

class IntExpr(val value: Int, position: Position): AbstractExpr(LiteralIntType(value), position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}