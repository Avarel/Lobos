package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.NeverType

object LiteralTrueType: ExistentialType {
    override val universalType: Type get() = BoolType
    override val parentType: Type get() = BoolType

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other || other === NeverType
    }

    override fun commonAssignableToType(other: Type): Type {
        return when {
            other === BoolType -> other
            other === LiteralFalseType -> BoolType
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when {
            other === BoolType -> this
            other === LiteralFalseType -> NeverType
            else -> super.commonAssignableToType(other)
        }
    }

    override fun toString() = "true"
}