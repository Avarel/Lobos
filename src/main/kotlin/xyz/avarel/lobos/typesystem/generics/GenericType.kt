package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.AnyType
import java.util.*

class GenericType(val genericParameter: GenericParameter): AbstractType(genericParameter.name, genericParameter.parentType ?: AnyType), TypeTemplate {
    override var genericParameters = listOf(genericParameter)

    override fun isAssignableFrom(other: Type): Boolean {
        return other == this || genericParameter.parentType?.isAssignableFrom(other) ?: false
    }

    override fun template(types: Map<GenericParameter, Type>): Type {
        val type = types[genericParameter] ?: throw IllegalArgumentException("Internal error")

        if (!parentType.isAssignableFrom(type)) {
            throw IllegalArgumentException("$type does not satisfy type bound $this")
        }

        return type
    }

    override fun extract(type: Type): Map<GenericParameter, Type> {
        if (genericParameter.parentType != null && !genericParameter.parentType.isAssignableFrom(type)) {
            throw IllegalArgumentException("$type does not satisfy type bound $this")
        }

        return Collections.singletonMap(genericParameter, type)
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