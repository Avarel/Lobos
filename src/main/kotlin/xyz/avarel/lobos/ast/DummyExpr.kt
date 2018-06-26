package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.base.NeverType

object DummyExpr : AbstractExpr(NeverType, Section("dummy", 0, 0, 0)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = throw IllegalStateException()
}