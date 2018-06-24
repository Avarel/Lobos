package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.Type

class Namespace(name: String): AbstractType("namespace $name") {
    val members: MutableMap<String, Type> = hashMapOf()

    override fun getMember(key: String) = members[key]
}