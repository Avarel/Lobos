package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.I64Type

class I64Expr(val value: Long, position: Position): AbstractExpr(I64Type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}

