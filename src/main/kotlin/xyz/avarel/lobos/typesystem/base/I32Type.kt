package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.ExcludedType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.complex.UnionType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType

object I32Type: AbstractType("i32") {
    val members = hashMapOf<String, Type>().also {
        val gp = GenericParameter("T", UnionType(I32Type, I64Type))
        val gt = GenericType(gp)
        val biOp = FunctionType(true, listOf(this, gt), gt)
        it["plus"] = biOp
        it["minus"] = biOp
        it["times"] = biOp
        it["div"] = biOp

        val unOp = FunctionType(true, listOf(this), this)
        it["unary_plus"] = unOp
        it["unary_minus"] = unOp


        it["to_i32"] = FunctionType(true, listOf(this), this)
        it["to_i64"] = FunctionType(true, listOf(this), I64Type)
    }

    override fun getMember(key: String): Type? = members[key]

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