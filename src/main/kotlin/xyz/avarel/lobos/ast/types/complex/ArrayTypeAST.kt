package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class ArrayTypeAST(val valueType: AbstractTypeAST, position: Section) : AbstractTypeAST("[$valueType]", position) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}