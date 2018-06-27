package xyz.avarel.lobos.ast.types.basic

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeVisitor
import xyz.avarel.lobos.lexer.Section

class NullTypeAST(position: Section) : AbstractTypeAST("null", position) {
    override fun <R> accept(visitor: TypeVisitor<R>) = visitor.visit(this)
}

