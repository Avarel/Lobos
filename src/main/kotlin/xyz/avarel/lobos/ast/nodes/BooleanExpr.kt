package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Section
import xyz.avarel.lobos.typesystem.literals.LiteralFalseType
import xyz.avarel.lobos.typesystem.literals.LiteralTrueType

class BooleanExpr(val value: Boolean, position: Section) : AbstractExpr(if (value) LiteralTrueType else LiteralFalseType, position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}