package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.generics.UnionType

/**
 * Represents the top of the inheritable type hierarchy.
 */
object AnyType: Type {
    override val allAssociatedTypes: Map<String, Type> get() = associatedTypes
    override val associatedTypes = hashMapOf<String, Type>().also {
        val anyOrNull = UnionType(this, NullType)
        it["equals"] = FunctionType(true, listOf(anyOrNull, anyOrNull), BoolType)
    }

    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other !== NullType && other !== InvalidType
    override fun getAssociatedType(key: String): Type? = associatedTypes[key]
    override fun toString() = "any"
}

