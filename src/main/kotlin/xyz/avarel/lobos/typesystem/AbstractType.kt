package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.generics.UnionType

abstract class AbstractType(val name: String, override val parentType: Type = AnyType): Type {
    override fun isAssignableFrom(other: Type): Boolean {
        return when (other) {
            this -> true
            is UnionType -> other.valueTypes.all(this::isAssignableFrom)
            else -> {
                var currentType = other
                while (currentType != this) {
                    currentType = currentType.parentType
                    if (currentType == AnyType) {
                        return false
                    }
                }
                true
            }
        }
    }

    override fun toString() = name
}