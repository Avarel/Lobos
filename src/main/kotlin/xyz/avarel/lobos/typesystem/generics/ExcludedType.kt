package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.literals.ExistentialType

class ExcludedType(
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

    override val parentType: Type get() = targetType

    override fun getAssociatedType(key: String) = universalType.getAssociatedType(key)

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return ExcludedType(
                emptyList(),
                transposeTypes(targetType, genericParameters, types),
                transposeTypes(subtractedType, genericParameters, types)
        )
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return targetType.isAssignableFrom(other) && !other.isAssignableFrom(subtractedType)
    }

    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            is ExcludedType -> if (other.targetType != targetType) UnionType(listOf(this, other)) else {
                ExcludedType(targetType, subtractedType.commonAssignableFromType(other.subtractedType))
            }
            is UnionType -> {
                val subtract = subtractedType.exclude(other)
                if (subtract === NeverType) targetType else ExcludedType(targetType, subtractedType)
            }
            targetType -> other
            subtractedType -> targetType
            else -> super.commonAssignableToType(other)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            is ExcludedType -> if (other.targetType != targetType) NeverType else {
                ExcludedType(targetType, subtractedType.commonAssignableToType(other.subtractedType))
            }
            is UnionType -> {
                val value = other.exclude(subtractedType)
                if (value is UnionType) {
                    val values = value.valueTypes.filter { it.isAssignableFrom(this) }
                    if (values.isEmpty()) NeverType else if (values.size == 1) values[0] else UnionType(values)
                } else if (this.isAssignableFrom(value)) {
                    ExcludedType(targetType, value)
                } else {
                    NeverType
                }
            }
            targetType -> this
            subtractedType -> NeverType
            else -> super.commonAssignableFromType(other)
        }
    }

    override fun exclude(other: Type): Type {
        return when {
            this == other -> NeverType
            targetType.isAssignableFrom(other) -> ExcludedType(targetType, subtractedType.commonAssignableToType(other))
            else -> this
        }
    }

    override fun filter(other: Type): Type {
        return when {
            this == other -> this
            other.isAssignableFrom(subtractedType) -> NeverType
            else -> targetType.filter(other)
        }
    }

    override fun toString() = buildString {
        append(targetType)
        append('!')
        append(subtractedType.toNestedString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExcludedType) return false

        if (genericParameters != other.genericParameters) return false
        if (targetType != other.targetType) return false
        if (subtractedType != other.subtractedType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genericParameters.hashCode()
        result = 31 * result + targetType.hashCode()
        result = 31 * result + subtractedType.hashCode()
        return result
    }
}