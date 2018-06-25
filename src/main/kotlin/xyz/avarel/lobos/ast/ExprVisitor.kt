package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.*
import xyz.avarel.lobos.ast.ops.*
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.ast.variables.LetExpr

interface ExprVisitor<R> {
    fun visit(expr: I32Expr): R
    fun visit(expr: I64Expr): R
    fun visit(expr: F64Expr): R

    fun visit(expr: InvalidExpr): R
    fun visit(expr: StringExpr): R
    fun visit(expr: BooleanExpr): R
    fun visit(expr: UnitExpr): R

    fun visit(expr: MultiExpr): R

    fun visit(expr: NamedFunctionExpr): R
    fun visit(expr: LetExpr): R
    fun visit(expr: AssignExpr): R

    fun visit(expr: IdentExpr): R
    fun visit(expr: TupleExpr): R

    fun visit(expr: InvokeExpr): R
    fun visit(expr: UnaryOperation): R
    fun visit(expr: BinaryOperation): R

    fun visit(expr: ReturnExpr): R
    fun visit(expr: IfExpr): R
    fun visit(expr: NullExpr): R

    fun visit(expr: LogicalOrOperation): R
    fun visit(expr: LogicalAndOperation): R
    fun visit(expr: LogicalNotOperation): R
    fun visit(expr: EqualsOperation): R

    fun visit(expr: IndexAccessExpr): R
    fun visit(expr: PropertyAccessExpr): R
    fun visit(expr: InvokeMemberExpr): R
}