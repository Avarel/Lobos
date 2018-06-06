package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.generics.FunctionType
import xyz.avarel.lobos.typesystem.generics.UnionType

/**
 * Represents the top of the inheritable type hierarchy.
 */
object AnyType: Type {
    override val allAssociatedTypes: Map<String, Type> get() = associatedTypes
    override val associatedTypes = hashMapOf<String, Type>().also {
        val anyOrNull = UnionType(listOf(this, NullType))
        it["equals"] = FunctionType(true, listOf(anyOrNull, anyOrNull), BoolType)
    }

    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other !== NullType && other !== InvalidType
    override fun getAssociatedType(key: String): Type? = associatedTypes[key]
    override fun toString() = "any"
}

/**
 * This represents the null type. Nothing can be assigned to it except for itself.
 */
object NullType: AbstractType("null") {
    override val allAssociatedTypes: Map<String, Type> get() = AnyType.allAssociatedTypes
    override val associatedTypes get() = AnyType.associatedTypes
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other === this
    override fun getAssociatedType(key: String): Type? = associatedTypes[key]
}

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

/**
 * Signifies when type inference has failed.
 * Nothing can be assigned to it, ever.
 */
object InvalidType: AbstractType("[Invalid type.]") {
    override val allAssociatedTypes: Map<String, Type> get() = emptyMap()
    override val associatedTypes: Map<String, Type> get() = emptyMap()
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = false
    override fun getAssociatedType(key: String): Type? = null
}

object I32Type: AbstractType("i32") {
    override val associatedTypes = hashMapOf<String, Type>().also {
        val opFn = FunctionType(true, listOf(this, this), this)
        it["plus"] = opFn
        it["minus"] = opFn
        it["times"] = opFn
        it["div"] = opFn
    }
}

object I64Type: AbstractType("i64")
object BoolType: AbstractType("bool")

object StrType: AbstractType("str") {
    override val associatedTypes = hashMapOf<String, Type>().also {
        it["plus"] = FunctionType(true, listOf(this, AnyType), this)
    }
}