package xyz.avarel.lobos.typesystem.complex

import xyz.avarel.lobos.typesystem.*
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.literals.ExistentialType

class UnionType(val left: Type, val right: Type): ExistentialType, TypeTemplate {
    override var genericParameters = listOf(left, right).findGenericParameters()

    override val universalType: Type by lazy {
        left.universalType.commonSuperTypeWith(right.universalType)
    }

    override fun getMember(key: String): Type? {
        val left = left.getMember(key) ?: return null
        val right = right.getMember(key) ?: return null
        return left.commonAssignableToType(right)
    }

    override fun template(types: Map<GenericParameter, Type>): Type {
        return listOf(left.template(types), right.template(types)).toType()
    }

    override fun extract(type: Type): Map<GenericParameter, Type> {
        val list = toList()
        val generics = list.filterIsInstance<GenericType>()
        val types = list.filter { it !is GenericType }
        val other = type.toList().toMutableList().apply { removeAll(types) }


        return when {
            other.size < generics.size -> {
                println("what " + toList())
                println(generics)
                println(type)
                println(other)
                throw IllegalArgumentException("Insufficient types in union to infer generics")
            }
            generics.isNotEmpty() -> {
                val map = mutableMapOf<GenericParameter, Type>()

                for (i in 0 until generics.lastIndex) {
                    map[generics[i].genericParameter] = other[i]
                }

                map[generics[generics.lastIndex].genericParameter] = other.subList(generics.lastIndex, other.size).toType()
                map
            }
            else -> emptyMap()
        }
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
        return when {
            this === other -> true
            other !is UnionType -> false
            genericParameters != other.genericParameters -> false
            left != other.left -> false
            right != other.right -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + genericParameters.hashCode()
        return result
    }
}