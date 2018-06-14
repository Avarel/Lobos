package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.*
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.ast.ops.LogicalOrOperation
import xyz.avarel.lobos.ast.ops.UnaryOperation
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.ast.variables.LetExpr

interface ExprVisitor<R> {
    fun visit(expr: InvalidExpr): R
    fun visit(expr: IntExpr): R
    fun visit(expr: StringExpr): R
    fun visit(expr: BooleanExpr): R
    fun visit(expr: UnitExpr): R

    fun visit(expr: MultiExpr): R

    fun visit(expr: NamedFunctionExpr): R
    fun visit(expr: LetExpr): R
    fun visit(expr: AssignExpr): R

    fun visit(expr: IdentExpr): R
    fun visit(expr: TupleExpr): R

    fun visit(expr: UnaryOperation): R
    fun visit(expr: BinaryOperation): R

    fun visit(expr: ReturnExpr): R
    fun visit(expr: IfExpr): R
    fun visit(expr: NullExpr): R

    fun visit(expr: LogicalOrOperation): R
    fun visit(expr: LogicalAndOperation): R
}