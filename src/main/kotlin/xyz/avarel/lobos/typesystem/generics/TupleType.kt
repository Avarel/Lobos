package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.NeverType

class TupleType(val valueTypes: List<Type>): Type, TypeTemplate {
    override val genericParameters = valueTypes.findGenericParameters()

    init {
        require(valueTypes.isNotEmpty())
    }

    override val universalType: Type by lazy { TupleType(valueTypes.map(Type::universalType)) }

    override fun template(types: List<Type>): Type {
        return TupleType(valueTypes.map {
            transposeTypes(it, genericParameters, types)
        })
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other === NeverType -> true
            other !is TupleType -> false
            other is UnionType -> other.valueTypes.all(this::isAssignableFrom)
            else -> valueTypes.size == other.valueTypes.size
                    && valueTypes.zip(other.valueTypes).all { (a, b) -> a.isAssignableFrom(b) }
        }
    }

    override fun toString() = buildString {
        append('(')
        append(valueTypes[0])

        if (valueTypes.size == 1) {
            append(',')
        } else for (i in 1 until valueTypes.size) {
            append(", ")
            append(valueTypes[i])
        }

        append(')')
    }
}