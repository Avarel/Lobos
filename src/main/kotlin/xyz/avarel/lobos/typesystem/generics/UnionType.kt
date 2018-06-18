package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.literals.ExistentialType

class UnionType(val left: Type, val right: Type): ExistentialType, TypeTemplate {
    override val genericParameters = listOf(left, right).findGenericParameters()

    init {
        assert(left != right)
    }

    override val universalType: Type by lazy {
        left.commonSuperTypeWith(right)
    }

    override val associatedTypes: Map<String, Type> get() = TODO()

    override fun getAssociatedType(key: String) = associatedTypes[key]

    override fun template(types: List<Type>): Type {
        return listOf(
                transposeTypes(left, genericParameters, types),
                transposeTypes(right, genericParameters, types)
        ).toType()
    }

    override fun isAssignableFrom(other: Type): Boolean = when (other) {
        NeverType -> true
        is UnionType -> this.isAssignableFrom(other.left) && isAssignableFrom(other.right)
        else -> left.isAssignableFrom(other) || right.isAssignableFrom(other)
    }

    override fun commonAssignableToType(other: Type): Type {
        return when (other) {
            NeverType -> this
            is UnionType -> listOf(left, right, other.left, other.right).reduce(Type::commonAssignableToType)
            else -> listOf(left, right, other).reduce(Type::commonAssignableToType)
        }
    }

    override fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            NeverType -> NeverType
            is UnionType -> listOf(left, right, other.left, other.right).reduce(Type::commonAssignableFromType)
            else -> listOf(left, right, other).reduce(Type::commonAssignableFromType)
        }
    }

    override fun exclude(other: Type): Type {
        if (other == this) return NeverType
        return listOf(left, right).map { it.exclude(other) }.filter { it != NeverType }.toType()
    }

    override fun filter(other: Type): Type {
        if (other == this) return this
        return listOf(left, right).map { it.exclude(other) }.filter { it != NeverType }.toType()
    }

    override fun toString() = "$left | $right"

    override fun toNestedString() = "($this)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnionType) return false

        if (genericParameters != other.genericParameters) return false
        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + genericParameters.hashCode()
        return result
    }
}

fun List<Type>.toType(): Type {
    val optimizedList = distinct()
    return when {
        optimizedList.isEmpty() -> NeverType
        optimizedList.size == 1 -> optimizedList[0]
        else -> optimizedList.reduce(::UnionType)
    }
}