package xyz.avarel.lobos.typesystem.base

import xyz.avarel.lobos.typesystem.Type
import xyz.avarel.lobos.typesystem.complex.TupleType

object UnitType : TupleType(emptyList()) {
    override val universalType: Type get() = this
    override fun toString() = "()"
}