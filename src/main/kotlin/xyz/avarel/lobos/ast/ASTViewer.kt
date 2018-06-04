package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.*
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.UnaryOperation
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.ast.variables.LetExpr

fun Expr.ast(buf: StringBuilder, indent: String = "", isTail: Boolean) = accept(ASTViewer(buf, indent, isTail))

class ASTViewer(val buf: StringBuilder, val indent: String = "", val isTail: Boolean): ExprVisitor<Unit> {
    private fun defaultAst(string: String) {
        buf.append(indent).append(if (isTail) "└── " else "├── ").append(string)
    }

    override fun visit(expr: NullExpr) = defaultAst("null")

    override fun visit(expr: InvalidExpr) = defaultAst("[Invalid expression.]")

    override fun visit(expr: IntExpr) = defaultAst(expr.value.toString())

    override fun visit(expr: StringExpr) = defaultAst("\"${expr.value}\"")

    override fun visit(expr: BooleanExpr) = defaultAst(expr.value.toString())

    override fun visit(expr: UnitExpr) = defaultAst("()")

    override fun visit(expr: MultiExpr) {
        for (i in 0 until expr.list.size - 1) {
            expr.list[i].ast(buf, indent, false)
            buf.append('\n')
        }
        if (expr.list.isNotEmpty()) {
            expr.list.last().ast(buf, indent, true)
        }
    }

    override fun visit(expr: LetExpr) {
        defaultAst("let ${expr.name}")
        buf.append('\n')
        expr.expr.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: AssignExpr) {
        defaultAst("assign ${expr.name}")
        buf.append('\n')
        expr.expr.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: IdentExpr) = defaultAst("variable ${expr.name}")

    override fun visit(expr: TupleExpr) {
        defaultAst("tuple")

        buf.append('\n')
        for (i in 0 until expr.list.size - 1) {
            expr.list[i].ast(buf, indent, false)
            buf.append('\n')
        }
        if (expr.list.isNotEmpty()) {
            expr.list.last().ast(buf, indent, true)
        }
    }

    override fun visit(expr: UnaryOperation) {
        defaultAst("binary ${expr.operator}")

        buf.append('\n')
        expr.expr.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: BinaryOperation) {
        defaultAst("binary ${expr.operator}")

        buf.append('\n')
        expr.left.ast(buf, indent + if (isTail) "    " else "│   ", false)

        buf.append('\n')
        expr.right.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: ReturnExpr) {
        defaultAst("return")

        buf.append('\n')
        expr.expr.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: IfExpr) {
        defaultAst("if")

        buf.append('\n').append(indent).append("│   ").append("then")

        buf.append('\n')
        expr.condition.ast(buf, indent + if (isTail) "    " else "│   ", false)

        buf.append('\n')
        expr.thenBranch.ast(buf, indent + if (isTail) "    " else "│   ", expr.elseBranch == null)

        if (expr.elseBranch != null) {
            buf.append('\n').append(indent).append("│   ").append("else")
            buf.append('\n')
            expr.elseBranch.ast(buf, indent + if (isTail) "    " else "│   ", true)
        }
    }
}