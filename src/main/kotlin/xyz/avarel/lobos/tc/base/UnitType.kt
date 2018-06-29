package xyz.avarel.lobos.tc.base

import xyz.avarel.lobos.tc.Type
import xyz.avarel.lobos.tc.complex.TupleType

object UnitType : TupleType(emptyList()) {
    override val universalType: Type get() = this
    override val isUnitType: Boolean get() = true
    override fun toString() = "()"
}