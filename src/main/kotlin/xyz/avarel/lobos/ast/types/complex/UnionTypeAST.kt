package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class UnionTypeAST(val left: AbstractTypeAST, val right: AbstractTypeAST, position: Section) : AbstractTypeAST("$left | $right", position) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}