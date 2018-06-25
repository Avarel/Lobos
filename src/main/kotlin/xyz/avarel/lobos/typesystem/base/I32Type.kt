package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.ExcludedType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.complex.UnionType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType
import xyz.avarel.lobos.typesystem.scope.VariableInfo

object I32Impl: Namespace("i32") {
    init {
        val gp = GenericParameter("T", UnionType(I32Type, I64Type))
        val gt = GenericType(gp)
        val biOp = VariableInfo(false, FunctionType(true, listOf(I32Type, gt), gt))
        scope.variables["plus"] = biOp
        scope.variables["minus"] = biOp
        scope.variables["times"] = biOp
        scope.variables["div"] = biOp

        val unOp = VariableInfo(false, FunctionType(true, listOf(I32Type), I32Type))
        scope.variables["unary_plus"] = unOp
        scope.variables["unary_minus"] = unOp

        scope.variables["to_i32"] = VariableInfo(false, FunctionType(true, listOf(I32Type), I32Type))
        scope.variables["to_i64"] = VariableInfo(false, FunctionType(true, listOf(I32Type), I64Type))
    }
}

object I32Type: AbstractType("i32") {
    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            is LiteralIntType -> this
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            is LiteralIntType -> other
            else -> super.commonAssignableFromType(other)
        }
    }

    override fun exclude(other: Type): Type {
        return when (other) {
            is LiteralIntType -> ExcludedType(this, other)
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when (other) {
            is LiteralIntType -> other
            else -> NeverType
        }
    }
}