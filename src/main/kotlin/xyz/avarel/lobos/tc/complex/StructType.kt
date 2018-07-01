package xyz.avarel.lobos.tc.complex

import xyz.avarel.lobos.tc.Type
import xyz.avarel.lobos.tc.TypeTemplate
import xyz.avarel.lobos.tc.findGenericParameters
import xyz.avarel.lobos.tc.generics.GenericParameter

class StructType(val name: String, val members: Map<String, Type>) : TypeTemplate {
    override var genericParameters: List<GenericParameter> = members.values.findGenericParameters()

    override fun getMember(key: String): Type? {
        return members[key]
    }

    override fun template(types: Map<GenericParameter, Type>): Type {
        require(types.keys == genericParameters.toSet())
        return TemplatedStructType(this, types)
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return this === other
    }

    override fun toString() = buildString {
        append(name)

        if (genericParameters.isNotEmpty()) {
            genericParameters.joinTo(this, prefix = "<", postfix = ">")
        }
    }
}

