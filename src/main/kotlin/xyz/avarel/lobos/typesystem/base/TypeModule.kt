package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

class TypeModule(val type: Type): AbstractType("module $type") {
    override fun getMember(key: String) = type.getMember(key)
}

