package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.base.StrType

class LiteralStrType(val value: String): ExistentialType {
    override val universalType: Type get() = StrType
    override val parentType get() = StrType

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other !is LiteralStrType -> false
            else -> other.value == value
        }
    }

    override fun commonAssignableToType(other: Type): Type {
        return when {
            other === StrType -> other
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when {
            other === StrType -> this
            else -> super.commonAssignableToType(other)
        }
    }

    override fun exclude(other: Type): Type {
        return when {
            this == other -> NeverType
            other is LiteralStrType && other.value == value -> NeverType
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when {
            this == other -> this
            else -> NeverType
        }
    }

    override fun toString() = "\"$value\""

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is LiteralStrType -> false
            else -> value == other.value
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}