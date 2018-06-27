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
}