package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

open class ParserContext(
        private val parent: ParserContext? = null,
        private val variables: MutableMap<String, VariableInfo> = hashMapOf(),
        private val assumptions: MutableMap<String, VariableInfo> = hashMapOf(),
        private val types: MutableMap<String, Type> = hashMapOf()
) {
    fun containsVariable(key: String): Boolean {
        return key in variables || parent?.containsVariable(key) ?: false
    }

    fun getVariable(key: String): VariableInfo? {
        return variables[key] ?: parent?.getVariable(key)
    }

    fun setVariable(key: String, info: VariableInfo) {
        variables[key] = info
    }

    fun containsAssumption(key: String): Boolean {
        return key in assumptions || parent?.containsAssumption(key) ?: false
    }

    fun getAssumption(key: String): VariableInfo? {
        return assumptions[key] ?: parent?.getAssumption(key)
    }

    fun setAssumption(key: String, info: VariableInfo) {
        assumptions[key] = info
    }

    fun getEffectiveType(key: String): VariableInfo? {
        return assumptions[key] ?: variables[key] ?: parent?.getEffectiveType(key)
    }

    fun containsType(key: String): Boolean {
        return key in types || parent?.containsType(key) ?: false
    }

    fun getType(key: String): Type? {
        return types[key] ?: parent?.getType(key)
    }

    fun setType(key: String, info: Type) {
        types[key] = info
    }

    fun subContext() = ParserContext(this)
}

