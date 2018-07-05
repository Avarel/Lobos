package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class FunctionTypeAST(val arguments: List<AbstractTypeAST>, val returnType: AbstractTypeAST, position: Section) : AbstractTypeAST(buildString {
    append('(')
    arguments.joinTo(this)
    append(") -> ")
    append(returnType)
}, position) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}