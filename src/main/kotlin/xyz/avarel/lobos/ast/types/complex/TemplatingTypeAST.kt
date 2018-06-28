package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class TemplatingTypeAST(val target: AbstractTypeAST, val arguments: List<AbstractTypeAST>, position: Section) : AbstractTypeAST(buildString {
    append(target)
    append('<')
    arguments.joinTo(this)
    append('>')
}, position) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}

