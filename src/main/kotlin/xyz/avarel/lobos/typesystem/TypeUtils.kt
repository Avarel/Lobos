package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.base.BoolType
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.complex.UnionType
import xyz.avarel.lobos.typesystem.generics.GenericBodyType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType
import xyz.avarel.lobos.typesystem.literals.LiteralFalseType
import xyz.avarel.lobos.typesystem.literals.LiteralTrueType

fun List<Type>.findGenericParameters(): List<GenericParameter> {
    val list = mutableListOf<GenericParameter>()
    for (type in this) {
        when (type) {
            is GenericType -> list.add(type.genericParameter)
            is TypeTemplate -> list.addAll(type.genericParameters)
        }
    }
    return list.distinct()
}

fun Type.transformToBodyType(): Type {
    return (this as? TypeTemplate)?.transformToBodyType() ?: this
}

fun TypeTemplate.transformToBodyType(): Type {
    return template(genericParameters.associateBy({ it }, { GenericBodyType(it) }))
}

fun List<Type>.toType(): Type {
    val list = flatMap(Type::toList).distinct().filter { it != NeverType }

    return when {
        list.isEmpty() -> NeverType
        list.size == 1 -> list[0]
        list.size == 2 && LiteralTrueType in list && LiteralFalseType in list -> BoolType
        else -> list.reduce { a, b ->
            when {
                a == b -> a
                a.isAssignableFrom(b) -> a
                b.isAssignableFrom(a) -> b
                else -> UnionType(a, b)
            }
        }
    }
}

fun Type.toList(): List<Type> {
    return when {
        this is UnionType -> {
            this.left.toList() + this.right.toList()
        }
        else -> listOf(this)
    }
}