package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.base.AnyType
import xyz.avarel.lobos.typesystem.base.InvalidType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.base.NullType

// GO WITH EXPLICIT TYPES FOR NOW, INFERENCE TOO HARD

interface Type {
    /**
     * @returns The parent type that this type extends.
     */
    val parentType: Type get() = AnyType

    /**
     * For existential types such as literal types, this will return
     * their universal version. In instance, literal integer types will
     * return the base int type, and literal string type will return the
     * base string type. This is mainly used when the parser has to
     * completely infer the type of the expression.
     *
     * @see xyz.avarel.lobos.typesystem.base.ExistentialType
     */
    val universalType: Type get() = this

    /**
     * Existential types only exist at compile time and not during
     * runtime.
     *
     * @returns `true` if the type is existential.
     */
    val isExistential get() = this != universalType

    /**
     * @returns `true` if [other] is assignable to this type.
     */
    fun isAssignableFrom(other: Type): Boolean

    val associatedTypes: Map<String, Type> get() = emptyMap()

    /**
     * Includes inherited associated types.
     */
    val allAssociatedTypes: Map<String, Type> get() = parentType.allAssociatedTypes + associatedTypes

    /**
     * Specialized for [xyz.avarel.lobos.typesystem.generics.TupleType].
     */
    fun getAssociatedType(key: Int): Type? = null

    /**
     * Get an associated type of this type.
     */
    fun getAssociatedType(key: String): Type? = associatedTypes[key] ?: parentType.getAssociatedType(key)

    fun commonSuperTypeWith(other: Type): Type {
        when {
            this == other -> return this
            other == NeverType || this == NeverType -> return NeverType
            other == NullType || this == NullType -> return NeverType
            other == InvalidType || this == InvalidType -> return InvalidType
            else -> {
                var thisWalk = this
                var otherWalk = other

                while (true) {
                    if (thisWalk == otherWalk) return thisWalk
                    while (otherWalk != AnyType) {
                        otherWalk = otherWalk.parentType
                        if (thisWalk == otherWalk) return thisWalk
                    }
                    otherWalk = other
                    thisWalk = thisWalk.parentType
                }
            }
        }
    }
}