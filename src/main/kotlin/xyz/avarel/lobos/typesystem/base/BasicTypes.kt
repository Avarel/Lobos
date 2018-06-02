package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

object AnyType: Type {
    override val parentType: Type = this
    override fun isAssignableFrom(other: Type) = other != NullType && other != InvalidType
    override fun toString() = "any"
}

object NullType: AbstractType("null") {
    override fun isAssignableFrom(other: Type) = other == this
}

object InvalidType: AbstractType("[Invalid Type]") {
    override fun isAssignableFrom(other: Type) = false
}

object I32Type: AbstractType("i32", I64Type)
object I64Type: AbstractType("i64")
object BoolType: AbstractType("bool")

object StrType: AbstractType("str")