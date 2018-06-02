package xyz.avarel.lobos.ast.ops

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.Type

class BinaryOperation(
        type: Type,
        val left: Expr,
        val right: Expr,
        val operator: BinaryOperationType,
        position: Position
): AbstractExpr(type, position) {
    constructor(left: Expr, right: Expr, operator: BinaryOperationType, position: Position):
            this(left.type.commonSuperTypeWith(right.type), left, right, operator, position)

    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}
