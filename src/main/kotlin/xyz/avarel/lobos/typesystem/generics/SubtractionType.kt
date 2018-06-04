package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.ExistentialType
import xyz.avarel.lobos.typesystem.base.InvalidType

class SubtractionType(
        override val genericParameters: List<GenericParameter>,
        val targetType: Type,
        val subtractedType: Type
): ExistentialType, TypeTemplate {
    constructor(targetType: Type, subtractedType: Type): this(
            listOf(targetType, subtractedType).findGenericParameters(),
            targetType,
            subtractedType
    )

    override val universalType: Type get() {
        return if (targetType === InvalidType || subtractedType === InvalidType) {
            InvalidType
        } else targetType
    }

    override fun getAssociatedType(key: String) = universalType.getAssociatedType(key)

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return SubtractionType(
                emptyList(),
                transposeTypes(targetType, genericParameters, types),
                transposeTypes(subtractedType, genericParameters, types)
        )
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return targetType.isAssignableFrom(other) && !subtractedType.isAssignableFrom(other)
    }

    override fun toString() = "$targetType ! $subtractedType"
}