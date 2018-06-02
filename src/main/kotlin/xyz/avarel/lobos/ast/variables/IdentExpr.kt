package xyz.avarel.lobos.ast.variables

import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.Type

class IdentExpr(val name: String, override val type: Type, override val position: Position): Expr {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}