package xyz.avarel.lobos.tc.base

import xyz.avarel.lobos.tc.AbstractType
import xyz.avarel.lobos.tc.scope.VariableInfo

class ModuleType(name: String) : AbstractType("mod $name") {
    var members: MutableMap<String, VariableInfo> = hashMapOf()

    override fun getMember(key: String) = members[key]
}