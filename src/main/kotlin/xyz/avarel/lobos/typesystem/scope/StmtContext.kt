package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

class StmtContext(val expectedType: Type? = null) {
    val assumptions: MutableMap<String, VariableInfo> = hashMapOf()
    val inverseAssumptions: MutableMap<String, VariableInfo> = hashMapOf()
}