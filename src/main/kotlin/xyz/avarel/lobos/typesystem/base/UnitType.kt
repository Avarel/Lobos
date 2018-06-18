package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.Type

object UnitType: Type {
    override fun isAssignableFrom(other: Type) = other === this

    override fun toString() = "()"
}