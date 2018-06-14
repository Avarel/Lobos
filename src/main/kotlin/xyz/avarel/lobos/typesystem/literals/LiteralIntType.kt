package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.I32Type
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.generics.UnionType
import xyz.avarel.lobos.typesystem.generics.toType

class LiteralIntType(val value: Int): ExistentialType {
    override val isUnitType: Boolean get() = true
    override val universalType get() = I32Type
    override val parentType get() = I32Type

    override fun isAssignableFrom(other: Type): Boolean {
        return when (other) {
            this -> true
            NeverType -> true
            !is LiteralIntType -> false
            else -> other.value == value
        }
    }

    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            I32Type -> other
            is LiteralIntType -> if (value == other.value) this else UnionType(listOf(this, other))
            else -> listOf(this, other).toType()
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when {
            other === I32Type -> this
            other is LiteralIntType -> if (value == other.value) this else NeverType
            else -> NeverType
        }
    }

    override fun exclude(other: Type): Type {
        return when {
            this == other -> NeverType
            other is LiteralIntType && other.value == value -> NeverType
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when {
            this == other -> this
            else -> NeverType
        }
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralIntType -> false
            else -> value == other.value
        }
    }

    override fun hashCode(): Int {
        return value
    }
}