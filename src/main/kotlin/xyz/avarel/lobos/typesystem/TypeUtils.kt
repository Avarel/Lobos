package xyz.avarel.lobos.typesystem

import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.complex.UnionType
import xyz.avarel.lobos.typesystem.generics.GenericBodyType
import xyz.avarel.lobos.typesystem.generics.GenericParameter
import xyz.avarel.lobos.typesystem.generics.GenericType

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
    val optimizedList = distinct().filter { it != NeverType }
    return when {
        optimizedList.isEmpty() -> NeverType
        optimizedList.size == 1 -> optimizedList[0]
        else -> optimizedList.reduce(::UnionType)
    }
}

fun Type.toList(): List<Type> {
    val list = mutableListOf<Type>()

    when {
        this is UnionType -> {
            list += this.left.toList()
            list += this.right.toList()
        }
        else -> list += this
    }

    return list
}