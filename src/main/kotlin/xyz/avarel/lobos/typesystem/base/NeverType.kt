package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

/**
 * This type can be assigned to anything, because technically, it will never return.
 * However, no other type can be assigned to it.
 */
object NeverType: AbstractType("!") {
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other === this
    override fun getMember(key: String): Type? = null

    override fun commonAssignableToType(other: Type) = other
    override fun commonAssignableFromType(other: Type) = this
}