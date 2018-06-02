package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate

open class TupleType(
        override val genericParameters: List<GenericParameter>,
        val valueTypes: List<Type>
): Type, TypeTemplate {
    constructor(valueTypes: List<Type>): this(valueTypes.findGenericParameters(), valueTypes)

    override val universalType: Type by lazy { TupleType(valueTypes.map(Type::universalType)) }

    object Unit: TupleType(emptyList(), emptyList()) {
        override fun toString() = "()"
    }

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return TupleType(emptyList(), valueTypes.map {
            transposeTypes(it, genericParameters, types)
        })
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other !is TupleType -> false
            other is UnionType -> other.valueTypes.all(this::isAssignableFrom)
            else -> valueTypes.size == other.valueTypes.size
                    && valueTypes.zip(other.valueTypes).all { (a, b) -> a.isAssignableFrom(b) }
        }
    }

    override fun toString() = valueTypes.joinToString(prefix = "(", postfix = ")")
}