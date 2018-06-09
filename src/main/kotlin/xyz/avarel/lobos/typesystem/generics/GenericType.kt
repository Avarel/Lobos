package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.NeverType

class GenericType(val genericParameter: GenericParameter): AbstractType(genericParameter.name, genericParameter.parentType), TypeTemplate {
    override val genericParameters: List<GenericParameter> get() = listOf(genericParameter)

    override fun template(types: List<Type>): Type {
        require(types.size == 1)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
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