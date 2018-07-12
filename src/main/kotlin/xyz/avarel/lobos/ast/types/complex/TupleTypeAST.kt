package xyz.avarel.lobos.ast.types.complex

import xyz.avarel.lobos.ast.types.AbstractTypeAST
import xyz.avarel.lobos.ast.types.TypeASTVisitor
import xyz.avarel.lobos.lexer.Section

class TupleTypeAST(val valueTypes: List<AbstractTypeAST>, section: Section) : AbstractTypeAST(valueTypes.joinToString(prefix = "(", postfix = ")"), section) {
    constructor(section: Section) : this(emptyList(), section)

    override fun <R> accept(visitor: TypeASTVisitor<R>) = visitor.visit(this)
}

