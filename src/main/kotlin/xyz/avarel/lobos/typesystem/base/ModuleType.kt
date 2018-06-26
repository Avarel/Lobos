package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.scope.ScopeContext

class ModuleType(name: String, val scope: ScopeContext) : AbstractType("mod $name") {
    override fun getMember(key: String) = scope.getAssumption(key)
}