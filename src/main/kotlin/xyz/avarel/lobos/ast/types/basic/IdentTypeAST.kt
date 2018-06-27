package xyz.avarel.lobos.ast.types.basic

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeVisitor
import xyz.avarel.lobos.lexer.Section

class IdentTypeAST(name: String, position: Section) : AbstractTypeAST(name, position) {
    override fun <R> accept(visitor: TypeVisitor<R>) = visitor.visit(this)
}

