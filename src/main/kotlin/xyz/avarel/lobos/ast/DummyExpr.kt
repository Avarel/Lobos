package xyz.avarel.lobos.ast

import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.base.NeverType

object DummyExpr: AbstractExpr(NeverType, Position("dummy", 0, 0)) {
    override fun <R> accept(visitor: ExprVisitor<R>) = throw IllegalStateException()
}