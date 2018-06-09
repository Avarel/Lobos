package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.AnyType

class GenericParameter(
        val name: String,
        val parentType: Type = AnyType
) {
    override fun toString() = buildString {
        append(name)
        if (parentType != AnyType) {
            append(": ")
            append(parentType)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is GenericParameter -> false
            else -> name == other.name && parentType == other.parentType
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parentType.hashCode()
        return result
    }
}

fun List<Type>.findGenericParameters(): List<GenericParameter> {
    val list = mutableListOf<GenericParameter>()
    for (type in this) {
        when (type) {
            is GenericType -> list.add(type.genericParameter)
            is TypeTemplate -> list.addAll(type.genericParameters)
        }
    }
    return list.distinctBy { it.name }
}

fun transposeTypes(originalType: Type, typeParameters: List<GenericParameter>, imposingTypes: List<Type>): Type {
    return when (originalType) {
        is GenericType -> imposingTypes[typeParameters.indexOfFirst { it.name == originalType.name }]
        is TypeTemplate -> originalType.template(originalType.genericParameters.map { inner -> typeParameters.indexOfFirst { it.name == inner.name } }.map { imposingTypes[it] })
        else -> originalType
    }
}