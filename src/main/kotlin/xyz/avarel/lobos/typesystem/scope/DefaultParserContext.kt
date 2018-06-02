package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.base.*

object DefaultParserContext: ParserContext() {
    init {
        setType("i32", I32Type)
        setType("i64", I64Type)
        setType("str", StrType)
        setType("null", NullType)
        setType("any", AnyType)
        setType("bool", BoolType)
    }
}