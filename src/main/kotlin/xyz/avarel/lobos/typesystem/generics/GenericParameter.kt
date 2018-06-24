package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.base.AnyType

class GenericParameter(
        val name: String,
        val parentType: Type? = null
) {
    override fun toString() = buildString {
        append(name)
        if (parentType != null && parentType != AnyType) {
            append(": ")
            append(parentType)
        }
    }
//
//    override fun equals(other: Any?): Boolean {
//        return when {
//            this === other -> true
//            other !is GenericParameter -> false
//            else -> name == other.name && parentType == other.parentType
//        }
//    }
//
//    override fun hashCode(): Int {
//        var result = name.hashCode()
//        result = 31 * result + (parentType?.hashCode() ?: 0)
//        return result
//    }
}