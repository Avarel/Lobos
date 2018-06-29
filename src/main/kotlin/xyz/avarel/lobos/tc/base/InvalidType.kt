package xyz.avarel.lobos.tc.base

import xyz.avarel.lobos.tc.AbstractType
import xyz.avarel.lobos.tc.Type

/**
 * Signifies when type inference has failed.
 * Nothing can be assigned to it, ever.
 */
object InvalidType: AbstractType("[Invalid type.]") {
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = false
    override fun getMember(key: String): Type? = null

    override fun commonAssignableToType(other: Type) = this
    override fun commonAssignableFromType(other: Type) = this
}