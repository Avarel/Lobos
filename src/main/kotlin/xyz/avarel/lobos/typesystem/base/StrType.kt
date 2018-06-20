package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.ExcludedType
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.literals.LiteralStrType

object StrType: AbstractType("str") {
    val members = hashMapOf<String, Type>().also {
        it["plus"] = FunctionType(true, listOf(this, AnyType), this)
    }

    override fun getMember(key: String) = members[key]

    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            is LiteralStrType -> this
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            is LiteralStrType -> other
            else -> super.commonAssignableFromType(other)
        }
    }

    override fun exclude(other: Type): Type {
        return when (other) {
            this -> NeverType
            is LiteralStrType -> ExcludedType(this, other)
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when (other) {
            this -> this
            is LiteralStrType -> other
            else -> NeverType
        }
    }
}