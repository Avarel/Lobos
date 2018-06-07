package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.NeverType

object LiteralFalseType: ExistentialType {
    override val universalType: Type get() = BoolType
    override val parentType: Type get() = BoolType

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other || other === NeverType
    }

    override fun commonAssignableToType(other: Type): Type {
        return when {
            other === BoolType -> other
            other === LiteralTrueType -> BoolType
            else -> super.commonAssignableToType(other)
        }
    }


    override fun commonAssignableFromType(other: Type): Type {
        return when {
            other === BoolType -> this
            other === LiteralTrueType -> NeverType
            else -> super.commonAssignableToType(other)
        }
    }

    override fun toString() = "false"
}