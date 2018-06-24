package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType

class GenericBodyType(val genericParameter: GenericParameter): AbstractType(genericParameter.name, genericParameter.parentType ?: AnyType) {
    override fun isAssignableFrom(other: Type): Boolean {
        return other == this || genericParameter.parentType?.isAssignableFrom(other) ?: false
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is GenericType -> genericParameter == other.genericParameter
            other is GenericBodyType -> genericParameter == other.genericParameter
            else -> false
        }
    }

    override fun toString() = genericParameter.name

    override fun hashCode(): Int {
        return genericParameter.hashCode()
    }
}