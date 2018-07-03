package xyz.avarel.lobos.tc.complex

import xyz.avarel.lobos.tc.*
import xyz.avarel.lobos.tc.base.NeverType
import xyz.avarel.lobos.tc.generics.GenericParameter
import xyz.avarel.lobos.tc.scope.VariableInfo

class UnionType(val left: Type, val right: Type) : TypeTemplate {
    override var genericParameters = listOf(left, right).findGenericParameters()

    override val universalType: Type by lazy {
        left.universalType commonSuperTypeWith right.universalType
    }

    override fun getMember(key: String): VariableInfo? {
        val left = left.getMember(key) ?: return null
        val right = right.getMember(key) ?: return null
        return VariableInfo(left.type union right.type, left.mutable && right.mutable)
    }

    override fun template(types: Map<GenericParameter, Type>): Type {
        return listOf(left.template(types), right.template(types)).toType()
    }

    override fun isAssignableFrom(other: Type): Boolean = when (other) {
        NeverType -> true
        is UnionType -> this isAssignableFrom other.left && isAssignableFrom(other.right)
        else -> left isAssignableFrom other || right isAssignableFrom other
    }

//    override fun union(other: Type): Type {
//        return when (other) {
//            NeverType -> this
//            is UnionType -> (toList() + other.toList()).reduce(Type::union)
//            else -> (toList() + other).toType()
//        }
//    }
//
//    override fun intersect(other: Type): Type {
//        return when (other) {
//            NeverType -> NeverType
//            is UnionType -> listOf(left, right, other.left, other.right).reduce(Type::intersect)
//            else -> listOf(left, right, other).reduce(Type::intersect)
//        }
//    }

//    override fun replace(target: Type, value: Type): Type {
//        if (target is UnionType) {
//            val thisTypes = toList()
//            val targetTypes = target.toList()
//            if (thisTypes.containsAll(targetTypes)) {
//                return thisTypes.toMutableList().also {
//                    it.removeAll(targetTypes)
//                    it.add(value)
//                }.toType()
//            }
//        } else {
//            val thisTypes = toList()
//            val index = thisTypes.indexOf(target)
//            if (index != -1) {
//                 return thisTypes.toMutableList().also {
//                    it[index] = value
//                }.toType()
//            }
//        }
//        return this
//    }

    override fun toString() = "$left | $right"

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