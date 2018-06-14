package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.base.NeverType

class GenericType(val genericParameter: GenericParameter): AbstractType(genericParameter.name, genericParameter.parentType ?: AnyType), TypeTemplate {
    override val genericParameters get() = listOf(genericParameter)

    override fun isAssignableFrom(other: Type): Boolean {
        return other == this || genericParameter.parentType?.isAssignableFrom(other) ?: false
    }

    override fun template(types: List<Type>): Type {
        return types[0]
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other === NeverType -> true
            other !is GenericType -> false
            else -> name == other.name && parentType == other.parentType
        }
    }

    override fun hashCode(): Int {
        return genericParameter.hashCode()
    }
}