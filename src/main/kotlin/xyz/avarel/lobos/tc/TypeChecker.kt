package xyz.avarel.lobos.tc

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
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.parser.TypeException
import xyz.avarel.lobos.tc.base.*
import xyz.avarel.lobos.tc.complex.FunctionType
import xyz.avarel.lobos.tc.complex.TupleType
import xyz.avarel.lobos.tc.generics.GenericParameter
import xyz.avarel.lobos.tc.generics.GenericType
import xyz.avarel.lobos.tc.scope.ScopeContext
import xyz.avarel.lobos.tc.scope.StmtContext

class TypeChecker(
        val scope: ScopeContext,
        val stmt: StmtContext,
        val deferBody: Boolean,
        val errorHandler: (TypeException) -> Unit
) : ExprVisitor<Type> {
    override fun visit(expr: NullExpr) = NullType
    override fun visit(expr: I32Expr) = I32Type
    override fun visit(expr: I64Expr) = I64Type
    override fun visit(expr: F64Expr) = F64Type
    override fun visit(expr: InvalidExpr) = InvalidType
    override fun visit(expr: StringExpr) = StrType
    override fun visit(expr: BooleanExpr) = BoolType

    override fun visit(expr: ModuleExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        val subScope = scope.subContext()

        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a module is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Module ${expr.name} has already been declared", expr.position))
                return InvalidType
            }
        }

        val type = ModuleType(expr.name).also { it.members = subScope.variables }

        expr.declarationsAST.let { declarations ->
            // defer modules //
            declarations.modules.forEach { it.accept(subScope, StmtContext(), true) }
            // defer functions //
            declarations.functions.forEach { it.accept(subScope, StmtContext(), true) }

            if (!deferBody) {
                // check lets //
                declarations.variables.forEach { it.accept(subScope, StmtContext()) }
                // check modules modules //
                declarations.modules.forEach { it.accept(subScope, StmtContext()) }
                // check function bodies //
                declarations.functions.forEach { it.accept(subScope, StmtContext()) }
            }
        }


        this.scope.putVariable(expr.name, type, deferBody)

        return InvalidType
    }

    override fun visit(expr: NamedFunctionExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a function is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Function ${expr.name} has already been declared", expr.position))
                return InvalidType
            }
        }

        val argumentScope = scope.subContext()
        val bodyScope = scope.subContext()

        expr.generics.forEach {
            val gp = GenericParameter(it.name, it.parentType?.resolve(scope))
            argumentScope.putVariable(it.name, GenericType(gp), false)
        }

        val arguments = expr.arguments.map {
            it.type.resolve(argumentScope).also { type ->
                bodyScope.putVariable(it.name, type.transformToBodyType(), false)
            }
        }

        val returnType = expr.returnType.resolve(argumentScope)

        val type = FunctionType(arguments.toList(), returnType)

        if (!deferBody) {
            bodyScope.expectedReturnType = returnType
            val resultType = expr.body.accept(bodyScope, StmtContext())
            if (!bodyScope.terminates) {
                catchError {
                    typeCheck(
                            returnType,
                            resultType,
                            (expr.body as? MultiExpr)?.list?.lastOrNull()?.position ?: expr.body.position
                    )
                }
            }
        }

        scope.putVariable(expr.name, type, deferBody)
        return InvalidType
    }

    override fun visit(expr: TypeAliasExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        val typeAliased = expr.type.resolve(scope)

        if (expr.generics.isNotEmpty()) {
            if (typeAliased is TypeTemplate) {
                typeAliased.genericParameters = expr.generics.map {
                    GenericParameter(it.name, it.parentType?.resolve(scope))
                }
            } else {
                errorHandler(TypeException("${expr.type} is not a generic type", expr.type.position))
                return InvalidType
            }
        }

        scope.types[expr.name] = typeAliased

        return InvalidType
    }

    override fun visit(expr: LetExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        if (expr.name in scope.variables) {
            errorHandler(TypeException("Variable ${expr.name} has already been declared", expr.position))
        }

        val exprType = expr.value.accept(scope, stmtExpectExpr())

        if (catchError { requireNotGeneric(exprType, expr.value.position) }) return InvalidType

        if (expr.type == null) {
            scope.putVariable(expr.name, exprType.universalType, expr.mutable)
        } else {
            val type = expr.type.resolve(scope)

            scope.putVariable(expr.name, type, expr.mutable)

            if (type.isAssignableFrom(exprType)) {
                scope.putAssumption(expr.name, exprType)
            } else {
                errorHandler(TypeException("Expected $type but found $exprType", expr.value.position))
            }
        }

        return InvalidType
    }

    override fun visit(expr: AssignExpr): Type {
        val (type, mutable) = scope.getDeclaration(expr.name) ?: let {
            errorHandler(TypeException("Reference ${expr.name} does not exist in this scope", expr.position))
            return InvalidType
        }

        if (!mutable) {
            errorHandler(TypeException("Reference ${expr.name} is not mutable", expr.position))
        }

        val exprType = expr.accept(scope, stmtExpectExpr())
        if (!catchError { typeCheck(type, exprType, expr.value.position) }) {
            scope.putAssumption(expr.name, exprType)
        }

        return InvalidType
    }

    override fun visit(expr: IdentExpr): Type {
        return scope.getAssumption(expr.name) ?: let {
            errorHandler(TypeException("Reference ${expr.name} does not exist in this scope", expr.position))
            return InvalidType
        }
    }

    override fun visit(expr: TupleExpr): Type {
        return when {
            expr.list.isEmpty() -> UnitType
            else -> TupleType(expr.list.map { it.accept(scope, stmtExpectExpr()) })
        }
    }

    override fun visit(expr: InvokeExpr): Type {
        return checkInvocation(expr.target, expr.arguments, expr.position)
    }

    override fun visit(expr: UnaryOperation): Type {
        val target = expr.target.accept(scope, stmtExpectExpr())
        when (target) {
            I32Type,
            I64Type,
            F64Type -> return target
        }
        errorHandler(TypeException("$target is incompatible", expr.position))
        return InvalidType
    }

    override fun visit(expr: BinaryOperation): Type {
        val left = expr.left.accept(scope, stmtExpectExpr())
        val right = expr.right.accept(scope, stmtExpectExpr())
        when (left) {
            StrType -> if (expr.operator == BinaryOperationType.ADD) return StrType
            I32Type -> when (right) {
                I32Type -> return I32Type
                I64Type,
                F64Type -> return right
            }
            I64Type -> when (right) {
                I32Type,
                I64Type -> return I64Type
                F64Type -> F64Type
            }
            F64Type -> when (right) {
                I32Type,
                I64Type,
                F64Type -> return F64Type
            }
        }
        errorHandler(TypeException("$left is incompatible with $right", expr.position))
        return InvalidType
    }

    override fun visit(expr: ReturnExpr): Type {
        TODO("not implemented")
//        catchError { typeCheck(scope.expectedReturnType, expr.accept(scope, stmtExpectExpr()), expr.position) }
//        return InvalidType
    }

    override fun visit(expr: IfExpr): Type {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalOrOperation): Type {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalAndOperation): Type {
        TODO("not implemented")
    }

    override fun visit(expr: LogicalNotOperation): Type {
        TODO("not implemented")
    }

    override fun visit(expr: EqualsOperation): Type {
        TODO("not implemented")
    }

    override fun visit(expr: IndexAccessExpr): Type {
        TODO("not implemented")
    }

    override fun visit(expr: PropertyAccessExpr): Type {
        val target = expr.target.accept(scope, stmtExpectExpr())
        val type = target.getMember(expr.name)

        if (type == null) {
            errorHandler(TypeException("$target does not have member ${expr.name}", expr.position))
        }

        return type ?: InvalidType
    }

    override fun visit(expr: InvokeMemberExpr): Type {
        return checkInvocation(PropertyAccessExpr(expr.target, expr.name, expr.target.position), expr.arguments, expr.position)
    }

    override fun visit(expr: TupleIndexAccessExpr): Type {
        val type = expr.target.accept(scope, stmtExpectExpr())

        if (type !is TupleType) {
            errorHandler(TypeException("$type is not a tuple type", expr.target.position))
            return InvalidType
        }

        if (expr.index !in type.valueTypes.indices) {
            errorHandler(TypeException("$type indices only include 0..${type.valueTypes.size - 1}, tried to access ${expr.index}", expr.position))
            return InvalidType
        }

        return type.valueTypes[expr.index]
    }

    override fun visit(expr: ExternalLetExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        if (expr.name in scope.variables) {
            errorHandler(TypeException("Variable ${expr.name} has already been declared", expr.position))
        }

        val exprType = expr.type.resolve(scope)

        if (catchError { requireNotGeneric(exprType, expr.type.position) }) return InvalidType

        scope.putVariable(expr.name, exprType, expr.mutable)

        return InvalidType
    }

    override fun visit(expr: ExternalNamedFunctionExpr): Type {
        if (catchError { requireNotExpr(expr.position) }) return InvalidType

        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a function is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Function ${expr.name} has already been declared", expr.position))
                return InvalidType
            }
        }

        val argumentScope = scope.subContext()

        expr.generics.forEach {
            val gp = GenericParameter(it.name, it.parentType?.resolve(scope))
            argumentScope.putVariable(it.name, GenericType(gp), false)
        }
        val arguments = expr.arguments.map { it.type.resolve(argumentScope) }

        val returnType = expr.returnType.resolve(argumentScope)

        val type = FunctionType(arguments.toList(), returnType)

        scope.putVariable(expr.name, type, deferBody)
        return InvalidType
    }

    override fun visit(expr: MultiExpr): Type {
        for (i in 0 until expr.list.lastIndex) {
            expr.list[i].accept(scope, StmtContext(), deferBody)
        }
        return expr.list.last().accept(scope, StmtContext(), deferBody)
    }

    fun Expr.accept(scope: ScopeContext, stmt: StmtContext, deferFunctionBody: Boolean = false): Type {
        return accept(TypeChecker(scope, stmt, deferFunctionBody, errorHandler))
    }

    fun TypeAST.resolve(scope: ScopeContext): Type {
        return accept(TypeResolver(scope, errorHandler))
    }

    /**
     * Throws an error if [foundType] can not be assigned to [expectedType].
     */
    fun typeCheck(expectedType: Type, foundType: Type, position: Section) {
        if (!expectedType.isAssignableFrom(foundType)) {
            throw TypeException("Expected $expectedType but found $foundType", position)
        }
    }

    /**
     * Check that [target] is invokable by [arguments].
     * @return [target] return type.
     * @throws TypeException if [target] is not a function.
     */
    fun checkInvocation(target: Expr, arguments: List<Expr>, position: Section): Type {
        val targetType = target.accept(scope, stmtExpectExpr())
        if (targetType !is FunctionType) {
            errorHandler(TypeException("$targetType can not be invoked", target.position))
            return InvalidType
        }

        val targetArgumentTypes = targetType.argumentTypes
        val argumentTypes = arguments.map { it.accept(scope, stmtExpectExpr()) }

        if (targetArgumentTypes.size != argumentTypes.size) {
            errorHandler(TypeException("Expected ${targetArgumentTypes.size} arguments, but found ${argumentTypes.size} arguments", position))
            return InvalidType
        }

        for (i in targetArgumentTypes.indices) {
            catchError { typeCheck(targetArgumentTypes[i], argumentTypes[i], arguments[i].position) }
        }

        return targetType.returnType
    }

    //fun inferAssumptionExpr(
    //        removeUnitOnly: Boolean,
    //        scope: ScopeContext,
    //        ctx: StmtContext,
    //        target: Expr,
    //        other: Expr,
    //        function: Pair<(Type, Type) -> Type, (Type, Type) -> Type> // forward and inverse
    //): Triple<String, Type, Type>? {
    //    if (target !is IdentExpr) return null
    //
    //    val key = target.name
    //    val effectiveType = ctx.assumptions[key] ?: scope.getAssumption(key)!!
    //    val otherType = other.type
    //
    //    if (removeUnitOnly && !otherType.isUnitType) {
    //        return null
    //    }
    //
    //    val assumption = function.first(effectiveType, otherType)
    //    val inverse = function.second(effectiveType, otherType)
    //
    //    return Triple(key, assumption, inverse)
    //}

    fun requireNotExpr(position: Section) {
        if (stmt.expectedType != null) {
            throw TypeException("Not a valid expression", position)
        }
    }

    fun stmtExpectExpr() = StmtContext(AnyType)

    fun requireNotGeneric(type: Type, position: Section) {
        if (type is TypeTemplate && type.genericParameters.isNotEmpty()) {
            throw TypeException("Missing generic type parameters", position)
        }
    }

    inline fun catchError(block: () -> Unit): Boolean {
        return try {
            block()
            false
        } catch (e: TypeException) {
            errorHandler(e)
            true
        }
    }
}