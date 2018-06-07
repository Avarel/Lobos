package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.literals.LiteralFalseType
import xyz.avarel.lobos.typesystem.literals.LiteralTrueType

object BoolType: AbstractType("bool") {
    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            LiteralTrueType, LiteralFalseType -> this
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            LiteralTrueType, LiteralFalseType -> other
            else -> super.commonAssignableFromType(other)
        }
    }

    override fun exclude(other: Type): Type {
        return when (other) {
            this -> this
            LiteralTrueType -> LiteralFalseType
            LiteralFalseType -> LiteralTrueType
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when (other) {
            this -> this
            LiteralTrueType, LiteralFalseType -> other
            else -> NeverType
        }
    }
}