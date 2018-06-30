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
import xyz.avarel.lobos.ast.expr.misc.TemplateExpr
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
) : ExprVisitor<Type?> {
    override fun visit(expr: NullExpr) = NullType
    override fun visit(expr: I32Expr) = I32Type
    override fun visit(expr: I64Expr) = I64Type
    override fun visit(expr: F64Expr) = F64Type
    override fun visit(expr: InvalidExpr) = InvalidType
    override fun visit(expr: StringExpr) = StrType
    override fun visit(expr: BooleanExpr) = BoolType

    override fun visit(expr: ModuleExpr): Type? {
        val subScope = scope.subContext()

        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a module is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Module ${expr.name} has already been declared", expr.position))
                return null
            }
        }

        val type = ModuleType(expr.name).also { it.members = subScope.variables }

        expr.declarationsAST.let { declarations ->
            // defer modules //
            declarations.modules.forEach { it.visitValue(subScope, deferBody = true) }
            // defer functions //
            declarations.functions.forEach { it.visitValue(subScope, deferBody = true) }

            if (!deferBody) {
                // check lets //
                declarations.variables.forEach { it.visitValue(subScope) }
                // check modules modules //
                declarations.modules.forEach { it.visitValue(subScope) }
                // check function bodies //
                declarations.functions.forEach { it.visitValue(subScope) }
            }
        }

        this.scope.putVariable(expr.name, type, deferBody)

        return null
    }

    override fun visit(expr: NamedFunctionExpr): Type? {
        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a function is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Function ${expr.name} has already been declared", expr.position))
                return null
            }
        }

        val genericParameters = expr.generics.map { GenericParameter(it.name, it.parentType?.resolve(scope)) }
        val argumentScope = scope.subContext()
        val bodyScope = scope.subContext()

        genericParameters.forEach {
            argumentScope.putType(it.name, GenericType(it))
        }

        val arguments = expr.arguments.map {
            it.type.resolve(argumentScope).also { type ->
                bodyScope.putVariable(it.name, type.transformToBodyType(), false)
            }
        }

        val returnType = expr.returnType.resolve(argumentScope)

        val type = FunctionType(arguments.toList(), returnType)
        type.genericParameters = genericParameters

        if (!deferBody) {
            bodyScope.expectedReturnType = returnType
            val resultType = expr.body.visitValue(bodyScope, StmtContext(), true)
            if (!bodyScope.terminates) {
                checkType(
                        returnType,
                        resultType,
                        (expr.body as? MultiExpr)?.list?.lastOrNull()?.position ?: expr.body.position
                )
            }
        }

        scope.putVariable(expr.name, type, deferBody)
        return null
    }

    override fun visit(expr: TypeAliasExpr): Type? {
        val typeAliased = if (expr.generics.isNotEmpty()) {
            val genericParameters = expr.generics.map {
                GenericParameter(it.name, it.parentType?.resolve(scope))
            }

            val scope = scope.subContext()

            genericParameters.forEach {
                scope.putType(it.name, GenericType(it))
            }

            expr.type.resolve(scope).also { typeAliased ->
                if (typeAliased is TypeTemplate) {
                    typeAliased.genericParameters = genericParameters
                } else {
                    errorHandler(TypeException("${expr.type} is not a generic type", expr.type.position))
                    return null
                }
            }
        } else {
            expr.type.resolve(scope)
        }

        scope.putType(expr.name, typeAliased)

        return null
    }

    override fun visit(expr: LetExpr): Type? {
        if (expr.name in scope.variables) {
            errorHandler(TypeException("Variable ${expr.name} has already been declared", expr.position))
        }

        val exprType = expr.value.visitValue(scope, StmtContext(), true)

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

        return null
    }

    override fun visit(expr: AssignExpr): Type? {
        val (type, mutable) = scope.getDeclaration(expr.name) ?: let {
            errorHandler(TypeException("Reference ${expr.name} does not exist in this scope", expr.position))
            return null
        }

        if (!mutable) {
            errorHandler(TypeException("Reference ${expr.name} is not mutable", expr.position))
            return null
        }

        val exprType = expr.visitValue(scope, StmtContext(), true)
        if (checkType(type, exprType, expr.value.position)) {
            scope.putAssumption(expr.name, exprType)
        }

        return null
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
            else -> TupleType(expr.list.map { it.visitValue(scope, StmtContext(), true) })
        }
    }

    override fun visit(expr: TemplateExpr): Type {
        val target = expr.target.visitValue(scope, StmtContext(), checkNotGeneric = false)

        if (target !is TypeTemplate) {
            errorHandler(TypeException("$target is not a generic template", expr.target.position))
            return target
        }

        if (target.genericParameters.size != expr.typeArguments.size) {
            errorHandler(TypeException("Expected ${target.genericParameters.size} type arguments, found ${expr.typeArguments.size} type arguments", expr.target.position))
            return InvalidType
        }

        var error = false
        val typeArguments = target.genericParameters.zip(expr.typeArguments) { param, arg ->
            val type = arg.resolve(scope)

            if (param.parentType != null) {
                if (!param.parentType.isAssignableFrom(type)) {
                    errorHandler(TypeException("$type does not satisfy type bound ${param.parentType}", arg.position))
                    error = true
                }
            }

            param to type
        }.toMap()
        if (error) return InvalidType

        return target.template(typeArguments)
    }

    override fun visit(expr: InvokeExpr): Type {
        return checkInvocation(expr.target, expr.arguments, expr.position)
    }

    override fun visit(expr: UnaryOperation): Type {
        val target = expr.target.visitValue(scope, StmtContext(), true)

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
        val left = expr.left.visitValue(scope, stmt, true)

        when (expr.operator) {
            BinaryOperationType.EQUALS, BinaryOperationType.NOT_EQUALS -> {
                val right = expr.right.visitValue(scope, StmtContext(), true)
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
                val right = expr.right.visitValue(scope, rightCtx, true)
                checkType(BoolType, left, expr.left.position)
                checkType(BoolType, right, expr.right.position)

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
                val right = expr.right.visitValue(scope, rightCtx, true)
                checkType(BoolType, left, expr.left.position)
                checkType(BoolType, right, expr.right.position)

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
                val right = expr.right.visitValue(scope, stmt)
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
            checkType(expectedReturnType, expr.visitValue(scope, StmtContext(), true), expr.position)
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
        val target = expr.target.visitValue(scope, StmtContext(), true)
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
        val type = expr.target.visitValue(scope, StmtContext(), true)

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

    override fun visit(expr: ExternalLetExpr): Type? {
        if (expr.name in scope.variables) {
            errorHandler(TypeException("Variable ${expr.name} has already been declared", expr.position))
        }

        val exprType = expr.type.resolve(scope)

        scope.putVariable(expr.name, exprType, expr.mutable)

        return null
    }

    override fun visit(expr: ExternalNamedFunctionExpr): Type? {
        if (expr.name in scope.variables) {
            if (!scope.getDeclaration(expr.name)!!.second) {
                // if a function is mutable in this scope, it's body's typecheck is being deferred
                errorHandler(TypeException("Function ${expr.name} has already been declared", expr.position))
                return null
            }
        }

        val genericParameters = expr.generics.map { GenericParameter(it.name, it.parentType?.resolve(scope)) }
        val argumentScope = scope.subContext()
        val bodyScope = scope.subContext()

        genericParameters.forEach {
            argumentScope.putType(it.name, GenericType(it))
        }

        val arguments = expr.arguments.map {
            it.type.resolve(argumentScope).also { type ->
                bodyScope.putVariable(it.name, type.transformToBodyType(), false)
            }
        }

        val returnType = expr.returnType.resolve(argumentScope)

        val type = FunctionType(arguments.toList(), returnType)
        type.genericParameters = genericParameters

        scope.putVariable(expr.name, type, deferBody)
        return null
    }

    override fun visit(expr: MultiExpr): Type? {
        for (i in 0 until expr.list.lastIndex) {
            expr.list[i].visitStmt(scope, null, deferBody)
        }
        return if (stmt != null) {
            expr.list.last().visitValue(scope, StmtContext(), deferBody)
        } else {
            expr.list.last().visitStmt(scope, null, deferBody)
            null
        }
    }

    private fun Expr.visitStmt(
            scope: ScopeContext,
            stmt: StmtContext? = null,
            deferBody: Boolean = false,
            expectNotGeneric: Boolean = true,
            expectExpr: Boolean = false
    ): Type? {
        val type = accept(TypeChecker(scope, stmt, deferBody, errorHandler))

        if (expectExpr && type == null) {
            errorHandler(TypeException("Not a valid expression", position))
            return InvalidType
        }

        if (expectNotGeneric && type is TypeTemplate && type.genericParameters.isNotEmpty()) {
            errorHandler(TypeException("Missing generic type parameters", position))
            return InvalidType
        }

        return type
    }

    private fun Expr.visitValue(
            scope: ScopeContext,
            stmt: StmtContext? = null,
            deferBody: Boolean = false,
            checkNotGeneric: Boolean = true
    ): Type {
        return visitStmt(scope, stmt, deferBody, checkNotGeneric, true)!!
    }

    private fun TypeAST.resolve(scope: ScopeContext): Type {
        return accept(TypeResolver(scope, errorHandler))
    }

    /**
     * Throws an error if [foundType] can not be assigned to [expectedType].
     */
    private fun checkType(expectedType: Type, foundType: Type, position: Section): Boolean {
        return if (!expectedType.isAssignableFrom(foundType)) {
            errorHandler(TypeException("Expected $expectedType but found $foundType", position))
            false
        } else {
            true
        }
    }

    /**
     * Check that [target] is invokable by [arguments].
     * @return [target] return type.
     * @throws TypeException if [target] is not a function.
     */
    private fun checkInvocation(target: Expr, arguments: List<Expr>, position: Section): Type {
        val targetType = target.visitValue(scope, StmtContext())

        if (targetType !is FunctionType) {
            errorHandler(TypeException("$targetType can not be invoked", target.position))
            return InvalidType
        }

        val targetArgumentTypes = targetType.argumentTypes
        val argumentTypes = arguments.map { it.visitValue(scope, StmtContext()) }

        if (targetArgumentTypes.size != argumentTypes.size) {
            errorHandler(TypeException("Expected ${targetArgumentTypes.size} arguments, but found ${argumentTypes.size} arguments", position))
            return InvalidType
        }

        for (i in targetArgumentTypes.indices) {
            checkType(targetArgumentTypes[i], argumentTypes[i], arguments[i].position)
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
}