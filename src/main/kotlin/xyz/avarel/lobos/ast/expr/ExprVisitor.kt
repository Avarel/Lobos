package xyz.avarel.lobos.ast.expr

import xyz.avarel.lobos.ast.expr.access.IndexAccessExpr
import xyz.avarel.lobos.ast.expr.access.PropertyAccessExpr
import xyz.avarel.lobos.ast.expr.access.TupleIndexAccessExpr
import xyz.avarel.lobos.ast.expr.declarations.LetExpr
import xyz.avarel.lobos.ast.expr.declarations.ModuleExpr
import xyz.avarel.lobos.ast.expr.declarations.NamedFunctionExpr
import xyz.avarel.lobos.ast.expr.declarations.TypeAliasExpr
import xyz.avarel.lobos.ast.expr.external.ExternalLetExpr
import xyz.avarel.lobos.ast.expr.external.ExternalNamedFunctionExpr
import xyz.avarel.lobos.ast.expr.invoke.InvokeExpr
import xyz.avarel.lobos.ast.expr.invoke.InvokeMemberExpr
import xyz.avarel.lobos.ast.expr.misc.IfExpr
import xyz.avarel.lobos.ast.expr.misc.InvalidExpr
import xyz.avarel.lobos.ast.expr.misc.MultiExpr
import xyz.avarel.lobos.ast.expr.nodes.*
import xyz.avarel.lobos.ast.expr.ops.BinaryOperation
import xyz.avarel.lobos.ast.expr.ops.UnaryOperation
import xyz.avarel.lobos.ast.expr.variables.AssignExpr

interface ExprVisitor<R> {
    fun visit(expr: I32Expr): R
    fun visit(expr: I64Expr): R
    fun visit(expr: F64Expr): R
    fun visit(expr: NullExpr): R

    fun visit(expr: InvalidExpr): R
    fun visit(expr: StringExpr): R
    fun visit(expr: BooleanExpr): R

    fun visit(expr: ModuleExpr): R
    fun visit(expr: NamedFunctionExpr): R
    fun visit(expr: TypeAliasExpr): R
    fun visit(expr: LetExpr): R
    fun visit(expr: AssignExpr): R

    fun visit(expr: IdentExpr): R
    fun visit(expr: TupleExpr): R

    fun visit(expr: InvokeExpr): R
    fun visit(expr: UnaryOperation): R
    fun visit(expr: BinaryOperation): R

    fun visit(expr: ReturnExpr): R
    fun visit(expr: IfExpr): R

    fun visit(expr: IndexAccessExpr): R
    fun visit(expr: PropertyAccessExpr): R
    fun visit(expr: InvokeMemberExpr): R
    fun visit(expr: TupleIndexAccessExpr): R

    fun visit(expr: ExternalLetExpr): R
    fun visit(expr: ExternalNamedFunctionExpr): R

    fun visit(expr: MultiExpr): R
}