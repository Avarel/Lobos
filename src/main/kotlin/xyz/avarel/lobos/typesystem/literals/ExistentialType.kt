package xyz.avarel.lobos.typesystem.literals

import xyz.avarel.lobos.typesystem.Type

interface ExistentialType: Type {
    override val universalType: Type

    override val implNamespace: String get() = universalType.implNamespace
}

