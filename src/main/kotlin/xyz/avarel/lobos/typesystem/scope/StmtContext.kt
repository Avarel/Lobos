package xyz.avarel.lobos.typesystem.scope

class StmtContext(val mustBeExpr: Boolean = false) {
    val assumptions: MutableMap<String, VariableInfo> = hashMapOf()
    val inverseAssumptions: MutableMap<String, VariableInfo> = hashMapOf()
}