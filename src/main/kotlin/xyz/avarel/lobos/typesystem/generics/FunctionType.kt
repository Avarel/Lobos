package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate

class FunctionType(
        override val genericParameters: List<GenericParameter>,
        val argumentTypes: List<Type>,
        val returnType: Type
): Type, TypeTemplate {
    constructor(argumentTypes: List<Type>, returnType: Type): this(argumentTypes.findGenericParameters(), argumentTypes, returnType)

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other !is FunctionType -> false
            else -> argumentTypes.size == other.argumentTypes.size
                    && argumentTypes.zip(other.argumentTypes).all { (a, b) -> a.isAssignableFrom(b) }
                    && returnType.isAssignableFrom(other.returnType)
        }

    }

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return FunctionType(emptyList(), argumentTypes.map {
            transposeTypes(it, genericParameters, types)
        }, transposeTypes(returnType, genericParameters, types))
    }

    override fun toString() = buildString {
        append("def ")
        if (genericParameters.isNotEmpty()) {
            genericParameters.joinTo(this, prefix = "<", postfix = ">")
            append(' ')
        }
        argumentTypes.joinTo(this, prefix = "(", postfix = ")")
        if (returnType != TupleType.Unit) {
            append(" -> ")
            append(returnType)
        }
    }
}