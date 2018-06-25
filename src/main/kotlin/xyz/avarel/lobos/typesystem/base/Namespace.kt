package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.AbstractType
import xyz.avarel.lobos.typesystem.scope.ScopeContext

open class Namespace(name: String): AbstractType("namespace $name") {
    override val implNamespace: String get() = "namespace"
    val scope: ScopeContext = ScopeContext()

    override fun getMember(key: String) = scope.getAssumption(key)?.type
}