package xyz.avarel.lobos.ast.types.basic

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class NullTypeAST(position: Section) : AbstractTypeAST("null", position) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}

