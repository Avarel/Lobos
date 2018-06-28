package xyz.avarel.lobos.typesystem.complex

import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.typesystem.*
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.generics.GenericParameter

open class TupleType(val valueTypes: List<Type>) : TypeTemplate {

    override var genericParameters = valueTypes.findGenericParameters()

    init {
        require(valueTypes.isNotEmpty())
    }

    override val universalType: Type by lazy { TupleType(valueTypes.map(Type::universalType)) }

    override fun template(types: Map<GenericParameter, Type>): Type {
        return TupleType(valueTypes.map { it.template(types) })
    }

    override fun extract(type: Type): Map<GenericParameter, Type> {
        if (type !is TupleType) throw IllegalArgumentException("Expected tuple, found $type")

        if (type.valueTypes.size < valueTypes.size) return emptyMap()

        val map = mutableMapOf<GenericParameter, Type>()

        valueTypes.zip(type.valueTypes).forEach { (a, b) ->
            map.mergeAll(a.extract(b), Type::commonAssignableToType)
        }

        return map
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other === NeverType -> true
            other is UnionType -> isAssignableFrom(other.left) && isAssignableFrom(other.right)
            other !is TupleType -> false
            else -> valueTypes.size == other.valueTypes.size
                    && valueTypes.zip(other.valueTypes).all { (a, b) -> a.isAssignableFrom(b) }
        }
    }

    override fun commonAssignableToType(other: Type): Type {
        if (other is TupleType && other.valueTypes.size == valueTypes.size) {
            return TupleType(valueTypes.zip(other.valueTypes, Type::commonAssignableToType))
        }
        return super.commonAssignableToType(other)
    }

    override fun commonAssignableFromType(other: Type): Type {
        if (other is TupleType && other.valueTypes.size == valueTypes.size) {
            return TupleType(valueTypes.zip(other.valueTypes, Type::commonAssignableFromType))
        }
        return super.commonAssignableFromType(other)
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