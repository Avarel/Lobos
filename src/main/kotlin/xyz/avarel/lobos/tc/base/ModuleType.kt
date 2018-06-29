package xyz.avarel.lobos.tc.base

import xyz.avarel.lobos.tc.AbstractType
import xyz.avarel.lobos.tc.Type

class ModuleType(name: String) : AbstractType("mod $name") {
    var members: MutableMap<String, Type> = hashMapOf()

    override fun getMember(key: String) = members[key]
}