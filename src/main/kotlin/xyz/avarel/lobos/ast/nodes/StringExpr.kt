package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.literals.LiteralStrType

class StringExpr(val value: String, position: Section) : AbstractExpr(LiteralStrType(value), position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}