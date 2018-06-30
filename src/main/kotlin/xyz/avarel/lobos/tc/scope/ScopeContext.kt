package xyz.avarel.lobos.tc.scope

import xyz.avarel.lobos.tc.Type

open class ScopeContext(
        val parent: ScopeContext? = null,
        val variables: MutableMap<String, Type> = hashMapOf(),
        val types: MutableMap<String, Type> = hashMapOf()
) {
    val mutableVariables: MutableSet<String> = hashSetOf()
    val assumptions: MutableMap<String, Type> = hashMapOf()

    var expectedReturnType: Type? = null
    var terminates: Boolean = false

    private fun getMutability(key: String): Boolean {
        return key in mutableVariables || parent?.getMutability(key) ?: false
    }

    fun getDeclaration(key: String): Pair<Type, Boolean>? {
        return getVariable(key)?.let { it to getMutability(key) }
    }

    fun getVariable(key: String): Type? {
        return variables[key] ?: parent?.getVariable(key)
    }

    fun putVariable(key: String, type: Type, mutable: Boolean) {
        variables[key] = type
        if (mutable) mutableVariables += key
    }

    fun getAssumption(key: String): Type? {
        return assumptions[key] ?: variables[key] ?: parent?.getAssumption(key)
    }

    fun putAssumption(key: String, type: Type) {
        assumptions[key] = type
    }

    fun getType(key: String): Type? {
        return types[key] ?: parent?.getType(key)
    }

    fun putType(key: String, type: Type) {
        types[key] = type
    }

    fun subContext() = ScopeContext(this).also { it.expectedReturnType = expectedReturnType }
}

