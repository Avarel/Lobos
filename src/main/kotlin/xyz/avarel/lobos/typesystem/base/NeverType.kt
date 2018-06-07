package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

/**
 * This type can be assigned to anything, because technically, it will never return.
 * However, no other type can be assigned to it.
 */
object NeverType: AbstractType("!") {
    override val allAssociatedTypes: Map<String, Type> get() = emptyMap()
    override val associatedTypes: Map<String, Type> get() = emptyMap()
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other === this
    override fun getAssociatedType(key: String): Type? = null
}