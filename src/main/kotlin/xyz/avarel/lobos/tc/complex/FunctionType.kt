package xyz.avarel.lobos.tc.complex

import xyz.avarel.lobos.parser.mergeAll
import xyz.avarel.lobos.tc.*
import xyz.avarel.lobos.tc.base.NeverType
import xyz.avarel.lobos.tc.generics.GenericParameter

class FunctionType(
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
        return FunctionType(argumentTypes.map { it.template(types) }, returnType.template(types))
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

    override fun toString() = buildString {
        argumentTypes.joinTo(this, prefix = "(", postfix = ")")

        append(" -> ")
        append(returnType)
    }
}