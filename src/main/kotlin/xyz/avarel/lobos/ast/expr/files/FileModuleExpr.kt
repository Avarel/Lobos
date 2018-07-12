package xyz.avarel.lobos.ast.expr.files

import xyz.avarel.lobos.ast.DeclarationsAST
import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class FileModuleExpr(
        val name: String,
        val declarationsAST: DeclarationsAST,
        section: Section
) : AbstractExpr(section) {
    override fun <R> accept(visitor: ExprVisitor<R>) = TODO()// visitor.visit(this)
}