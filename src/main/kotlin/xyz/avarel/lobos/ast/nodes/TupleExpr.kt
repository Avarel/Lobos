package xyz.avarel.lobos.ast.nodes

import xyz.avarel.lobos.ast.AbstractExpr
import xyz.avarel.lobos.ast.Expr
import xyz.avarel.lobos.ast.ExprVisitor
import xyz.avarel.lobos.lexer.Position
import xyz.avarel.lobos.typesystem.generics.TupleType

class TupleExpr(val list: List<Expr>, position: Position): AbstractExpr(TupleType(list.map(Expr::type)), position) {
    override fun <R> accept(visitor: ExprVisitor<R>) = visitor.visit(this)
}