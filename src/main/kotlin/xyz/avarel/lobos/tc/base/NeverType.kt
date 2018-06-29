package xyz.avarel.lobos.tc.base

import xyz.avarel.lobos.tc.AbstractType
import xyz.avarel.lobos.tc.Type

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