package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.complex.UnionType

abstract class AbstractType(val name: String, override val parentType: Type = AnyType): Type {
    override val implNamespace get() = name

    override fun isAssignableFrom(other: Type): Boolean {
        return when (other) {
            this, NeverType -> true
            is UnionType -> this.isAssignableFrom(other.right) && this.isAssignableFrom(other.left)
            else -> {
                var currentType = other
                while (currentType != this) {
                    currentType = currentType.parentType
                    if (currentType.parentType == currentType) {
                        // Top-most types such as NullType, InvalidType, and AnyType
                        return false
                    }
                }
                true
            }
        }
    }

    override fun getMember(key: String) = parentType.getMember(key)

    override fun toString() = name
}