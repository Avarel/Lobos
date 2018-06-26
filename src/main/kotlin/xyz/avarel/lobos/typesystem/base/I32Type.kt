package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.ExcludedType
import xyz.avarel.lobos.typesystem.literals.LiteralIntType

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