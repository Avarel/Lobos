package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

/**
 * This represents the null type. Nothing can be assigned to it except for itself.
 */
object NullType: AbstractType("null") {
    override val implNamespace: String get() = "null"
    override val isUnitType: Boolean get() = true
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other === this || other === NeverType
    override fun getMember(key: String): Type? = null
}