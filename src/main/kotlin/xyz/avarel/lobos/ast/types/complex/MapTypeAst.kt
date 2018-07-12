package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class MapTypeAst(val keyType: AbstractTypeAST, val valueType: AbstractTypeAST, section: Section) : AbstractTypeAST("[$keyType: $valueType]", section) {
    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}