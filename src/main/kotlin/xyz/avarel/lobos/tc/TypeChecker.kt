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
import xyz.avarel.lobos.ast.expr.ops.BinaryOperation
import xyz.avarel.lobos.ast.expr.ops.BinaryOperationType
import xyz.avarel.lobos.ast.expr.ops.UnaryOperation
import xyz.avarel.lobos.ast.expr.ops.UnaryOperationType
import xyz.avarel.lobos.ast.expr.variables.AssignExpr
import xyz.avarel.lobos.ast.types.TypeAST
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.parser.TypeException
import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.tc.base.*
import xyz.avarel.lobos.tc.complex.FunctionType
import xyz.avarel.lobos.tc.complex.TupleType
import xyz.avarel.lobos.tc.generics.GenericParameter
import xyz.avarel.lobos.tc.generics.GenericType
import xyz.avarel.lobos.tc.scope.ScopeContext
import xyz.avarel.lobos.tc.scope.StmtContext

class TypeChecker(
        val scope: ScopeContext,
        val stmt: StmtContext?,
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
            declarations.modules.forEach { it.accept(subScope, deferBody = true) }
            // defer functions //
            declarations.functions.forEach { it.accept(subScope, deferBody = true) }

            if (!deferBody) {
                // check lets //
                declarations.variables.forEach { it.accept(subScope) }
                // check modules modules //
                declarations.modules.forEach { it.accept(subScope) }
                // check function bodies //
                declarations.functions.forEach { it.accept(subScope) }
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
            val resultType = expr.body.accept(bodyScope, StmtContext(), true)
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

        val exprType = expr.value.accept(scope, StmtContext(), true)

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
            return InvalidType
        }

        val exprType = expr.accept(scope, StmtContext(), true)
        if (!catchError { typeCheck(type, exprType, expr.value.position) }) {
            scope.putAssumption(expr.name, exprType)
        }

        return InvalidType
    }

    override fun visit(expr: IdentExpr): Type {
        val key = expr.name
        return stmt?.getAssumption(key) ?: scope.getAssumption(key) ?: let {
            errorHandler(TypeException("Reference $key does not exist in this scope", expr.position))
            return InvalidType
        }
    }

    override fun visit(expr: TupleExpr): Type {
        return when {
            expr.list.isEmpty() -> UnitType
            else -> TupleType(expr.list.map { it.accept(scope, StmtContext(), true) })
        }
    }

    override fun visit(expr: InvokeExpr): Type {
        return checkInvocation(expr.target, expr.arguments, expr.position)
    }

    override fun visit(expr: UnaryOperation): Type {
        val target = expr.target.accept(scope, StmtContext(), true)

        when (expr.operator) {
            UnaryOperationType.NOT -> TODO()
            else -> when (target) {
                I32Type,
                I64Type,
                F64Type -> return target
            }
        }

        errorHandler(TypeException("$target is incompatible", expr.position))
        return InvalidType
    }

    override fun visit(expr: BinaryOperation): Type {
        val stmt = stmt ?: StmtContext() // locally, b/c chains matter
        val left = expr.left.accept(scope, stmt, true)

        when (expr.operator) {
            BinaryOperationType.EQUALS, BinaryOperationType.NOT_EQUALS -> {
                val right = expr.right.accept(scope, StmtContext(), true)
                if (!left.isAssignableFrom(right) && !right.isAssignableFrom(left)) {
                    errorHandler(TypeException("$left and $right are incompatible", expr.position))
                } else {
                    inferTypeAssertion(true, expr.left, left, right, Type::filter, Type::exclude) { key, assumption, reciprocal ->
                        stmt.putAssumption(key, assumption)
                        stmt.putReciprocal(key, reciprocal)
                    }
                    inferTypeAssertion(true, expr.right, right, left, Type::filter, Type::exclude) { key, assumption, reciprocal ->
                        stmt.putAssumption(key, assumption)
                        stmt.putReciprocal(key, reciprocal)
                    }
                    if (expr.operator == BinaryOperationType.NOT_EQUALS) {
                        val tmp = stmt.assumptions
                        stmt.assumptions = stmt.reciprocals
                        stmt.reciprocals = tmp
                    }
                }

                return BoolType
            }
            BinaryOperationType.AND -> {
                val rightCtx = StmtContext().also {
                    it.assumptions.putAll(stmt.assumptions)
                }
                val right = expr.right.accept(scope, rightCtx, true)
                catchError { typeCheck(BoolType, left, expr.left.position) }
                catchError { typeCheck(BoolType, right, expr.right.position) }

                stmt.assumptions.mergeAll(rightCtx.assumptions, Type::commonAssignableFromType)

                if ((stmt.reciprocals.keys + rightCtx.reciprocals.keys).size == 1) {
                    stmt.reciprocals.mergeAll(rightCtx.reciprocals, Type::commonAssignableToType)
                } else {
                    // cant trust any assumptions about outside if depends on multiple variables
                    stmt.reciprocals.clear()
                }
                return BoolType
            }
            BinaryOperationType.OR -> {
                val rightCtx = StmtContext().also {
                    it.assumptions.putAll(stmt.reciprocals)
                }
                val right = expr.right.accept(scope, rightCtx, true)
                catchError { typeCheck(BoolType, left, expr.left.position) }
                catchError { typeCheck(BoolType, right, expr.right.position) }

                stmt.reciprocals.mergeAll(rightCtx.reciprocals, Type::commonAssignableFromType)

                if ((stmt.assumptions.keys + rightCtx.assumptions.keys).size == 1) {
                    stmt.reciprocals.mergeAll(rightCtx.assumptions, Type::commonAssignableToType)
                } else {
                    // cant trust any reciprocals if depends on multiple variables
                    stmt.assumptions.clear()
                }
                return BoolType
            }
            else -> {
                val right = expr.right.accept(scope, stmt)
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
                        F64Type -> return F64Type
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
        }
    }

    override fun visit(expr: ReturnExpr): Type {
        val expectedReturnType = scope.expectedReturnType
        if (expectedReturnType == null) {
            errorHandler(TypeException("return is not valid in this context", expr.position))
        } else {
            catchError { typeCheck(expectedReturnType, expr.accept(scope, StmtContext(), true), expr.position) }
        }
        scope.terminates = true
        return NeverType
    }

    override fun visit(expr: IfExpr): Type {
        TODO("not implemented")
    }

    override fun visit(expr: IndexAccessExpr): Type {
        TODO("not implemented")
    }

    override fun visit(expr: PropertyAccessExpr): Type {
        val target = expr.target.accept(scope, StmtContext(), true)
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
        val type = expr.target.accept(scope, StmtContext(), true)

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
            expr.list[i].accept(scope, null, deferBody)
        }
        return expr.list.last().accept(scope, stmt?.let { StmtContext() }, deferBody)
    }

    private fun Expr.accept(scope: ScopeContext, stmt: StmtContext? = null, deferBody: Boolean = false): Type {
        return accept(TypeChecker(scope, stmt, deferBody, errorHandler))
    }

    private fun TypeAST.resolve(scope: ScopeContext): Type {
        return accept(TypeResolver(scope, errorHandler))
    }

    /**
     * Throws an error if [foundType] can not be assigned to [expectedType].
     */
    private fun typeCheck(expectedType: Type, foundType: Type, position: Section) {
        if (!expectedType.isAssignableFrom(foundType)) {
            throw TypeException("Expected $expectedType but found $foundType", position)
        }
    }

    /**
     * Check that [target] is invokable by [arguments].
     * @return [target] return type.
     * @throws TypeException if [target] is not a function.
     */
    private fun checkInvocation(target: Expr, arguments: List<Expr>, position: Section): Type {
        val targetType = target.accept(scope, StmtContext())
        if (targetType !is FunctionType) {
            errorHandler(TypeException("$targetType can not be invoked", target.position))
            return InvalidType
        }

        val targetArgumentTypes = targetType.argumentTypes
        val argumentTypes = arguments.map { it.accept(scope, StmtContext()) }

        if (targetArgumentTypes.size != argumentTypes.size) {
            errorHandler(TypeException("Expected ${targetArgumentTypes.size} arguments, but found ${argumentTypes.size} arguments", position))
            return InvalidType
        }

        for (i in targetArgumentTypes.indices) {
            catchError { typeCheck(targetArgumentTypes[i], argumentTypes[i], arguments[i].position) }
        }

        return targetType.returnType.also {
            if (it === NeverType) scope.terminates = true
        }
    }

    private inline fun inferTypeAssertion(
            unitOnly: Boolean,
            target: Expr,
            targetType: Type,
            subjectType: Type,
            function: (Type, Type) -> Type,
            inverse: (Type, Type) -> Type,
            success: (key: String, assumption: Type, reciprocal: Type) -> Unit
    ) {
        if (target !is IdentExpr) return
        if (unitOnly && !subjectType.isUnitType) return

        val assumption = function(targetType, subjectType)
        val reciprocal = inverse(targetType, subjectType)

        success(target.name, assumption, reciprocal)
    }

    private fun requireNotExpr(position: Section) {
        if (stmt != null) throw TypeException("Not a valid expression", position)
    }

    private fun requireNotGeneric(type: Type, position: Section) {
        if (type is TypeTemplate && type.genericParameters.isNotEmpty()) {
            throw TypeException("Missing generic type parameters", position)
        }
    }

    private inline fun catchError(block: () -> Unit): Boolean {
        return try {
            block()
            false
        } catch (e: TypeException) {
            errorHandler(e)
            true
        }
    }
}