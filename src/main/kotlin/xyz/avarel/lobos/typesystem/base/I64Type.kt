package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.FunctionType
import xyz.avarel.lobos.typesystem.complex.UnionType

object I64Type: AbstractType("i64") {
    val members = hashMapOf<String, Type>().also {
        val opFn = FunctionType(true, listOf(this, UnionType(I32Type, I64Type)), this)
        it["plus"] = opFn
        it["minus"] = opFn
        it["times"] = opFn
        it["div"] = opFn

        val unOp = FunctionType(true, listOf(this), this)
        it["unary_plus"] = unOp
        it["unary_minus"] = unOp

        it["to_i32"] = FunctionType(true, listOf(this), I32Type)
        it["to_i64"] = FunctionType(true, listOf(this), this)
    }

    override fun getMember(key: String): Type? = members[key]
}