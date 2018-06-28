package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
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
import xyz.avarel.lobos.ast.expr.ops.*
import xyz.avarel.lobos.ast.expr.variables.AssignExpr
import xyz.avarel.lobos.ast.types.TypeAST
import xyz.avarel.lobos.parser.TypeException
import xyz.avarel.lobos.typesystem.base.*
import xyz.avarel.lobos.typesystem.scope.ScopeContext
import xyz.avarel.lobos.typesystem.scope.StmtContext

class TypeChecker(
        val scope: ScopeContext,
        val stmt: StmtContext,
        val errorHandler: (TypeException) -> Unit
) : ExprVisitor<TypeChecker.TypedExpr> {
    override fun visit(expr: I32Expr) = expr.typed(I32Type)

    override fun visit(expr: I64Expr) = expr.typed(I64Type)

    override fun visit(expr: F64Expr) = expr.typed(F64Type)

    override fun visit(expr: InvalidExpr) = expr.typed(InvalidType)

    override fun visit(expr: StringExpr) = expr.typed(StrType)

    override fun visit(expr: BooleanExpr) = expr.typed(BoolType)

    override fun visit(expr: ModuleExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: NamedFunctionExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: TypeAliasExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: LetExpr): TypedExpr {
        val typed = expr.expr.accept(this)

        if (expr.name in scope.variables) {
            errorHandler(TypeException("Variable ${expr.name} has already been declared", expr.position))
        }

        if (expr.type == null) {
            scope.putVariable(expr.name, typed.type.universalType, expr.mutable)
        } else {
            val type = expr.type.resolve(scope)

            scope.putVariable(expr.name, type, expr.mutable)

            if (type.isAssignableFrom(typed.type)) {
                scope.putAssumption(expr.name, typed.type)
            } else {
                errorHandler(TypeException("Expected $type but found ${typed.type}", typed.position))
            }
        }

        return expr.typed(InvalidType)
    }

    override fun visit(expr: AssignExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: IdentExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: TupleExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: InvokeExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: UnaryOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: BinaryOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: ReturnExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: IfExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: NullExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalOrOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalAndOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalNotOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: EqualsOperation): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: IndexAccessExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: PropertyAccessExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: InvokeMemberExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: TupleIndexAccessExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: ExternalLetExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: ExternalNamedFunctionExpr): TypedExpr {
        TODO("not implemented")
    }

    override fun visit(expr: MultiExpr): TypedExpr {
        for (i in 0 until expr.list.lastIndex) {
            expr.list[i].accept(scope, StmtContext())
        }
        return expr.list.last().accept(scope, StmtContext())
    }

    fun Expr.accept(scope: ScopeContext, stmt: StmtContext): TypedExpr {
        return accept(TypeChecker(scope, stmt, errorHandler))
    }

    class TypedExpr(val type: Type, val expr: Expr) : AbstractExpr(expr.position) {
        override fun <R> accept(visitor: ExprVisitor<R>) = expr.accept(visitor)
    }

    fun Expr.typed(type: Type) = if (this is TypedExpr) throw IllegalStateException() else TypedExpr(type, this)

    fun TypeAST.resolve(scope: ScopeContext): Type {
        return accept(TypeResolver(scope, errorHandler))
    }
}