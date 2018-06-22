package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.FunctionType

object F64Type: AbstractType("f64") {
    val members = hashMapOf<String, Type>().also {
        val opFn = FunctionType(true, listOf(this, this), this)
        it["plus"] = opFn
        it["minus"] = opFn
        it["times"] = opFn
        it["div"] = opFn
    }

    override fun getMember(key: String): Type? = members[key]
}