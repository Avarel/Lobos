package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.generics.toType

object LiteralTrueType: ExistentialType {
    override val isUnitType: Boolean get() = true
    override val universalType: Type get() = BoolType
    override val parentType: Type get() = BoolType

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other || other === NeverType
    }

    override fun commonAssignableToType(other: Type): Type {
        return when {
            other === BoolType -> other
            other === LiteralFalseType -> BoolType
            else -> listOf(this, other).toType()
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when {
            other === BoolType -> this
            other === LiteralFalseType -> NeverType
            else -> NeverType
        }
    }

    override fun toString() = "true"
}