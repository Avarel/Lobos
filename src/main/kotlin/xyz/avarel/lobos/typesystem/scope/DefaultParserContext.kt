package xyz.avarel.lobos.typesystem.scope

import xyz.avarel.lobos.typesystem.base.*

object DefaultParserContext: ScopeContext() {
    init {
        this.types["i32"] = I32Type
        this.types["i64"] = I64Type
        this.types["f64"] = F64Type
        this.types["str"] = StrType
        this.types["null"] = NullType
        this.types["any"] = AnyType
        this.types["bool"] = BoolType
    }
}