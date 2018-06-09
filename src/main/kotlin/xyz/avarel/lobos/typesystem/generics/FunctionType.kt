package xyz.avarel.lobos.typesystem.generics

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.TypeTemplate
import xyz.avarel.lobos.typesystem.base.NeverType

class FunctionType(
        override val genericParameters: List<GenericParameter>,
        val selfArgument: Boolean,
        val argumentTypes: List<Type>,
        val returnType: Type
): Type, TypeTemplate {
    constructor(selfArgument: Boolean, argumentTypes: List<Type>, returnType: Type): this(
            argumentTypes.findGenericParameters(),
            selfArgument,
            argumentTypes,
            returnType
    )

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other === NeverType -> true
            other !is FunctionType -> false
            else -> argumentTypes.size == other.argumentTypes.size
                    && argumentTypes.zip(other.argumentTypes).all { (a, b) -> a.isAssignableFrom(b) }
                    && returnType.isAssignableFrom(other.returnType)
        }
    }

    override fun template(types: List<Type>): Type {
        require(types.size == genericParameters.size)
        require(types.zip(genericParameters).all { (type, param) -> param.parentType.isAssignableFrom(type) })
        return FunctionType(selfArgument, argumentTypes.map {
            transposeTypes(it, genericParameters, types)
        }, transposeTypes(returnType, genericParameters, types))
    }

//    override fun commonSuperTypeWith(other: Type): Type {
//        return when {
//            other !is FunctionType || other.argumentTypes.size != argumentTypes.size -> super.commonSuperTypeWith(other)
//            else -> {
//                val arguments = argumentTypes.zip(other.argumentTypes).fold(mutableListOf<Type>()) { list, (a, b) ->
//                    list.add(a.commonSuperTypeWith(b))
//                    list
//                }
//                val returns = returnType.commonSuperTypeWith(other.returnType)
//                return FunctionType(selfArgument && other.selfArgument, arguments, returns)
//            }
//        }
//    }

    override fun toString() = buildString {
        if (genericParameters.isNotEmpty()) {
            genericParameters.joinTo(this, prefix = "<", postfix = ">")
        }

        argumentTypes.joinTo(this, prefix = "(", postfix = ")")

        append(" -> ")
        append(returnType)
    }
}