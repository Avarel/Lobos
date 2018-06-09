package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.literals.ExistentialType

class UnionType(
        override val genericParameters: List<GenericParameter>,
        val valueTypes: List<Type>
): ExistentialType, TypeTemplate {
    constructor(valueTypes: List<Type>): this(valueTypes.findGenericParameters(), valueTypes)

    init {
        if (valueTypes.isEmpty()) throw IllegalStateException("empty union")
    }

    override val universalType: Type by lazy {
        assert(valueTypes.size > 1)
        valueTypes.reduce(Type::commonSuperTypeWith)
    }

    override val associatedTypes: Map<String, Type> by lazy {
        val names = valueTypes.map { it.allAssociatedTypes.keys }.reduce { a, b -> a intersect b }
        names.associate { name -> name to valueTypes.mapNotNull { it.getAssociatedType(name) }.reduce(Type::commonAssignableToType) }
    }

    fun flatten(): Type {
        val types = mutableListOf<Type>()

        for (type in valueTypes) {
            if (type is UnionType) {
                val flattened = type.flatten()
                if (flattened is UnionType) {
                    types += flattened.valueTypes
                } else {
                    types += flattened
                }
            } else {
                types += type
            }
        }

        return types.toType(false)
    }

    override fun getAssociatedType(key: String) = associatedTypes[key]

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return valueTypes.map {
            transposeTypes(it, genericParameters, types)
        }.toType()
    }

    override fun isAssignableFrom(other: Type) = when (other) {
        NeverType -> true
        is UnionType -> other.valueTypes.all { o -> valueTypes.any { it.isAssignableFrom(o) } }
        else -> valueTypes.any { it.isAssignableFrom(other) }
    }

    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            NeverType -> this
            is UnionType -> {
                valueTypes.flatMap {
                    other.valueTypes
                            //.filter { it.isAssignableFrom(other) && other.isAssignableFrom(it) }
                            .map { other -> it.commonAssignableToType(other) }
                            .flatMap { (it as? UnionType)?.valueTypes ?: listOf(it) }
                }.toType()
            }
            else -> {
                valueTypes/*.filter { it.isAssignableFrom(other) && other.isAssignableFrom(it) }*/.flatMap {
                    it.commonAssignableToType(other).let { (it as? UnionType)?.valueTypes ?: listOf(it) }
                }.toType()
            }
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            is UnionType -> {
                valueTypes.flatMap {
                    other.valueTypes
                            .map { other -> it.commonAssignableFromType(other) }
                            .flatMap { (it as? UnionType)?.valueTypes ?: listOf(it) }
                }.filter { it != NeverType }.toType()
            }
            else -> {
                valueTypes.flatMap {
                    it.commonAssignableFromType(other).let { (it as? UnionType)?.valueTypes ?: listOf(it) }
                }.filter { it != NeverType }.toType()
            }
        }
    }

    override fun exclude(other: Type): Type {
        if (other == this) return NeverType
        val values = valueTypes.map { it.exclude(other) }.filter { it !== NeverType }
        return values.toType()
    }

    override fun filter(other: Type): Type {
        if (other == this) return this
        val values = valueTypes.map { it.filter(other) }.filter { it !== NeverType }
        return values.toType()
    }

    override fun toString() = buildString {
        append(valueTypes[0].toNestedString())
        for (i in 1 until valueTypes.size) {
            append(" | ")
            append(valueTypes[i])
        }
    }

    override fun toNestedString() = "($this)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnionType) return false

        if (genericParameters != other.genericParameters) return false
        if (valueTypes != other.valueTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genericParameters.hashCode()
        result = 31 * result + valueTypes.hashCode()
        return result
    }
}

fun List<Type>.toType(flatten: Boolean = true): Type {
    val optimizedList = distinct()
    return when {
        optimizedList.isEmpty() -> NeverType
        optimizedList.size == 1 -> optimizedList[0]
        else -> UnionType(optimizedList).let { if (flatten) it.flatten() else it }
    }
}