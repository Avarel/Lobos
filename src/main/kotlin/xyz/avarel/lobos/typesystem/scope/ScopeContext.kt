package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

open class ScopeContext(
        val parent: ScopeContext? = null,
        val variables: MutableMap<String, VariableInfo> = hashMapOf(),
        val types: MutableMap<String, Type> = hashMapOf()
) {
    val assumptions: MutableMap<String, VariableInfo> = hashMapOf()

    var expectedReturnType: Type? = null
    var terminates: Boolean = false

    fun containsVariable(key: String): Boolean {
        return key in variables || parent?.containsVariable(key) ?: false
    }
    fun getVariable(key: String): VariableInfo? {
        return variables[key] ?: parent?.getVariable(key)
    }

    fun containsAssumption(key: String): Boolean {
        return key in assumptions || parent?.containsAssumption(key) ?: false
    }

    fun getAssumption(key: String): VariableInfo? {
        return assumptions[key] ?: parent?.getAssumption(key)
    }

    fun getEffectiveType(key: String): VariableInfo? {
        return assumptions[key] ?: variables[key] ?: parent?.getEffectiveType(key)
    }

    fun getType(key: String): Type? {
        return types[key] ?: parent?.getType(key)
    }

    fun subContext() = ScopeContext(this).also { it.expectedReturnType = expectedReturnType }
}

