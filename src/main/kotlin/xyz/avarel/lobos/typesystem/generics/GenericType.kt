package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType

class GenericType(val genericParameter: GenericParameter, parentType: Type = AnyType): AbstractType(genericParameter.name, parentType) {
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is GenericType -> false
            else -> name == other.name && parentType == other.parentType
        }
    }
}