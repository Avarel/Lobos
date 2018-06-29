package xyz.avarel.lobos.tc

import xyz.avarel.lobos.tc.base.AnyType
import xyz.avarel.lobos.tc.base.InvalidType
import xyz.avarel.lobos.tc.base.NeverType
import xyz.avarel.lobos.tc.base.NullType
import xyz.avarel.lobos.tc.complex.UnionType

// GO WITH EXPLICIT TYPES FOR NOW, INFERENCE TOO HARD

interface Type {
    /**
     * @returns true if this type can only ever have 1 single value. ie. null
     */
    val isUnitType: Boolean get() = false

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
     * TODO rewrite this
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
    infix fun isAssignableFrom(other: Type): Boolean

    /**
     * Get an associated type of this type.
     */
    fun getMember(key: String): Type? = parentType.getMember(key)

    infix fun commonSuperTypeWith(other: Type): Type {
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

    infix fun commonAssignableToType(other: Type): Type {
        return when (other) {
            this -> this
            else -> UnionType(this, other)
        }
    }

    infix fun commonAssignableFromType(other: Type): Type {
        return when (other) {
            this -> this
            else -> NeverType
        }
    }

    fun filter(other: Type): Type {
        return when {
            this == other -> this
            this isAssignableFrom other -> other
            else -> NeverType
        }
    }

    fun exclude(other: Type): Type {
        return when {
            this == other -> NeverType
        // this isAssignableFrom other -> NeverType
        // potential spot for the return of exclusion types?
            else -> this
        }
    }
}