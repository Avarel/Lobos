package xyz.avarel.lobos.tc.scope

import xyz.avarel.lobos.tc.Type

open class ScopeContext(
        val parent: ScopeContext? = null,
        val allowMutableParentAssumptions: Boolean = true,
        val variables: MutableMap<String, VariableInfo> = hashMapOf(),
        val types: MutableMap<String, Type> = hashMapOf()
) {
    val assumptions: MutableMap<String, Type> = hashMapOf()

    var expectedReturnType: Type? = null
    var terminates: Boolean = false

    fun getDeclaration(key: String): VariableInfo? {
        return variables[key] ?: parent?.getDeclaration(key)
    }

    fun declare(key: String, type: Type, mutable: Boolean) {
        variables[key] = VariableInfo(type, mutable)
    }

    fun getAssumption(key: String): Type? {
        return assumptions[key] ?: variables[key]?.type ?: parent?.getAssumption(key)?.let { parentAssumption ->
            when {
                allowMutableParentAssumptions -> parentAssumption
                else -> parent.getDeclaration(key)!!.let { (type, mutable) ->
                    if (mutable) return type
                    return parentAssumption
                }
            }
        }
    }

    fun assume(key: String, type: Type) {
        assumptions[key] = type
    }

    fun getType(key: String): Type? {
        return types[key] ?: parent?.getType(key)
    }

    fun putType(key: String, type: Type) {
        types[key] = type
    }

    fun subContext(allowParentAssumptions: Boolean = true) = ScopeContext(this, allowParentAssumptions).also {
        it.expectedReturnType = expectedReturnType
    }
}

