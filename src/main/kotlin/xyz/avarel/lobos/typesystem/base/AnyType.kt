package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.Type

/**
 * Represents the top of the inheritable type hierarchy.
 */
object AnyType: Type {
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other !== NullType && other !== InvalidType
    override fun getMember(key: String): Type? = null
    override fun toString() = "any"
}

