package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.Type

data class VariableInfo(
        val mutable: Boolean,
        val type: Type
)