package xyz.avarel.lobos.ast.expr.declarations

import xyz.avarel.lobos.ast.DeclarationsAST
import xyz.avarel.lobos.ast.expr.AbstractExpr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.lexer.Section

class DeclareModuleExpr(
        val name: String,
        val declarationsAST: DeclarationsAST,
        section: Section
) : AbstractExpr(section), ModuleExpr {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}