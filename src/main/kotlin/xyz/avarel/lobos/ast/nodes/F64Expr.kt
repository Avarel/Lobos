package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.F64Type
import xyz.avarel.lobos.typesystem.base.I64Type

class F64Expr(val value: Double, position: Position): AbstractExpr(F64Type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}