package xyz.avarel.lobos

import org.junit.Test
import xyz.avarel.lobos.tc.base.I32Type
import xyz.avarel.lobos.tc.complex.StructType

class TestTypes {
    @Test
    fun `mutable test`() {
        val what = StructType("Box", mapOf("value" to I32Type))
        println(what)
    }
}