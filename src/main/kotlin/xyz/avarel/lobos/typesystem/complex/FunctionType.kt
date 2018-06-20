package xyz.avarel.lobos.typesystem.complex

import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.typesystem.*
import xyz.avarel.lobos.typesystem.base.NeverType
import xyz.avarel.lobos.typesystem.generics.GenericParameter

class FunctionType(
        val selfArgument: Boolean,
        val argumentTypes: List<Type>,
        val returnType: Type
): TypeTemplate {
    override var genericParameters = (argumentTypes + returnType).findGenericParameters()

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other === NeverType -> true
            other !is FunctionType -> false
            else -> argumentTypes.size == other.argumentTypes.size
                    && other.argumentTypes.zip(argumentTypes).all { (a, b) -> a.isAssignableFrom(b) }
                    && returnType.isAssignableFrom(other.returnType)
        }
    }

    override fun template(types: Map<GenericParameter, Type>): FunctionType {
        return FunctionType(selfArgument, argumentTypes.map { it.template(types) }, returnType.template(types))
    }

    override fun extract(type: Type): Map<GenericParameter, Type> {
        if (type !is FunctionType)
            throw IllegalArgumentException("Expected function, found $type")
        if (type.argumentTypes.size < argumentTypes.size)
            throw IllegalArgumentException("Expected ${argumentTypes.size} arguments, found ${type.argumentTypes.size}")

        val map = mutableMapOf<GenericParameter, Type>()

        argumentTypes.zip(type.argumentTypes) { a, b ->
            map.mergeAll(a.extract(b), Type::commonAssignableToType)
        }

        map.mergeAll(returnType.extract(type.returnType)) { v1, _ -> v1 }

        return map
    }

    // fn equals(a: i32, b: i32) <- union 1
    // fn equals(a: null, b: null) <- union 2
    // fn equals(a: i32 | null, b: i32 | null) <- what i want
    // fn equals(a: never, b: never) <- what happens when trying to access the member of a union
//
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