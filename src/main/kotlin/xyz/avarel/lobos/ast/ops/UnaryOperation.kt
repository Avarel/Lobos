package xyz.avarel.lobos.ast.ops

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.Type

class UnaryOperation(
        type: Type,
        val target: Expr,
        val operator: UnaryOperationType,
        position: Position
): AbstractExpr(type, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
