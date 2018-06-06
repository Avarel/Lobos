package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.parser.commonAssignableToType
import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.ExistentialType

class UnionType(
        override val genericParameters: List<GenericParameter>,
        val valueTypes: List<Type>
): ExistentialType, TypeTemplate {
    constructor(valueTypes: List<Type>): this(valueTypes.findGenericParameters(), valueTypes)

    override val universalType: Type by lazy {
        assert(valueTypes.size > 1)
        valueTypes.reduce(Type::commonSuperTypeWith)
    }

    override val associatedTypes: Map<String, Type> by lazy {
        val names = valueTypes.map { it.allAssociatedTypes.keys }.reduce { a, b -> a intersect b }
        names.associate { name -> name to valueTypes.mapNotNull { it.getAssociatedType(name) }.reduce(Type::commonAssignableToType) }
    }

    override fun getAssociatedType(key: String) = associatedTypes[key]

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return UnionType(emptyList(), valueTypes.map {
            transposeTypes(it, genericParameters, types)
        })
    }

    override fun isAssignableFrom(other: Type) = valueTypes.any { it.isAssignableFrom(other) }

    override fun toString() = valueTypes.joinToString(separator = " | ") {
        if (it is UnionType) {
            "($it)"
        } else {
            it.toString()
        }
    }
}