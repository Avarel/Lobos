package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

class StmtContext(val modifiers: Array<Modifier> = emptyArray(), val expectedType: Type? = null) {
    constructor(expectedType: Type? = null): this(emptyArray(), expectedType)

    var assumptions: MutableMap<String, VariableInfo> = hashMapOf()
    var inverseAssumptions: MutableMap<String, VariableInfo> = hashMapOf()
}