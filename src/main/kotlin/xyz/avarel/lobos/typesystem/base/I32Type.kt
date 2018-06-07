package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.generics.ExcludedType
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType

object I32Type: AbstractType("i32") {
    override val associatedTypes = hashMapOf<String, Type>().also {
        val opFn = FunctionType(true, listOf(this, this), this)
        it["plus"] = opFn
        it["minus"] = opFn
        it["times"] = opFn
        it["div"] = opFn
    }

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