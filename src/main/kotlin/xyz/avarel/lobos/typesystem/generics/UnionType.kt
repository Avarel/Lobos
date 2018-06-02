package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate

class UnionType(
        override val genericParameters: List<GenericParameter>,
        val valueTypes: List<Type>
): Type, TypeTemplate {
    constructor(types: List<Type>): this(types.findGenericParameters(), types)

    // TODO handle effective type for this

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return UnionType(emptyList(), valueTypes.map {
            transposeTypes(it, genericParameters, types)
        })
    }

    override fun isAssignableFrom(other: Type) = valueTypes.any { it.isAssignableFrom(other) }

    override fun toString() = valueTypes.joinToString(separator = " | ")
}