package xyz.avarel.lobos.tc.complex

import xyz.avarel.lobos.tc.Type
import xyz.avarel.lobos.tc.TypeTemplate
import xyz.avarel.lobos.tc.findGenericParameters
import xyz.avarel.lobos.tc.generics.GenericParameter
import xyz.avarel.lobos.tc.template

class TemplatedStructType(val base: StructType, val typeArguments: Map<GenericParameter, Type>) : TypeTemplate {
    val members: Map<String, Type> = base.members.mapValues { it.value.template(typeArguments) }
    override var genericParameters: List<GenericParameter> = members.values.findGenericParameters()

    override fun getMember(key: String): Type? {
        return members[key]
    }

    override fun template(types: Map<GenericParameter, Type>): Type {
        require(types.keys == genericParameters.toSet())
        return TemplatedStructType(base, typeArguments + types)
    }

    override fun isAssignableFrom(other: Type): Boolean {
        if (this == other) return true
        if (other !is TemplatedStructType) return false
        if (base !== other.base) return false

        return members.all { (key, value) ->
            other.members[key]?.let {
                value.isAssignableFrom(it)
            } ?: false
        }
    }

    override fun toString() = buildString {
        append(base.name)

        if (typeArguments.isNotEmpty()) {
            typeArguments.values.joinTo(this, prefix = "<", postfix = ">")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemplatedStructType) return false

        if (base != other.base) return false
        if (typeArguments != other.typeArguments) return false
        if (members != other.members) return false
        if (genericParameters != other.genericParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + typeArguments.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + genericParameters.hashCode()
        return result
    }
}