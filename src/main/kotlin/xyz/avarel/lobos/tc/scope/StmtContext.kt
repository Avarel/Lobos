package xyz.avarel.lobos.tc.scope

import xyz.avarel.lobos.tc.Type

class StmtContext(val expectedType: Type? = null) {
    var assumptions: MutableMap<String, Type> = hashMapOf()
    var inverseAssumptions: MutableMap<String, Type> = hashMapOf()
}