package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

class StmtContext(val expectedType: Type? = null) {
    var assumptions: MutableMap<String, VariableInfo> = hashMapOf()
    var inverseAssumptions: MutableMap<String, VariableInfo> = hashMapOf()
}