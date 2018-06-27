package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeVisitor
import xyz.avarel.lobos.lexer.Section

class FunctionTypeAST(val types: List<AbstractTypeAST>, val returnType: AbstractTypeAST, position: Section) : AbstractTypeAST(buildString {
    append('(')
    types.joinTo(this)
    append(") -> ")
    append(returnType)
}, position) {
    override fun <R> accept(visitor: TypeVisitor<R>) = visitor.visit(this)
}