package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.generics.GenericParameter

interface TypeTemplate {
    val genericParameters: List<GenericParameter>
    fun template(types: List<Type>): Type
}