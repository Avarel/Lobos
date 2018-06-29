package xyz.avarel.lobos.tc

import xyz.avarel.lobos.tc.generics.GenericParameter

interface TypeTemplate: Type {
    /**
     * Type parameters for this type template.
     */
    var genericParameters: List<GenericParameter>

    /**
     * Apply type parameters to the type template.
     */
    fun template(types: Map<GenericParameter, Type>): Type

    fun template(types: List<Type>) = template(genericParameters.zip(types).toMap())

    /**
     * Extract generic parameters.
     */
    fun extract(type: Type): Map<GenericParameter, Type>

}

fun Type.template(types: Map<GenericParameter, Type>): Type {
    return (this as? TypeTemplate)?.template(types) ?: this
}

fun Type.template(types: List<Type>): Type {
    return (this as? TypeTemplate)?.template(types) ?: this
}

fun Type.extract(type: Type): Map<GenericParameter, Type> {
    return (this as? TypeTemplate)?.extract(type) ?: emptyMap()
}