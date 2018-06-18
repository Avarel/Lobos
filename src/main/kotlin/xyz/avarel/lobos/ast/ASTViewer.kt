package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.*
import xyz.avarel.lobos.ast.ops.BinaryOperation
import xyz.avarel.lobos.ast.ops.LogicalAndOperation
import xyz.avarel.lobos.ast.ops.LogicalOrOperation
import xyz.avarel.lobos.ast.ops.UnaryOperation
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.ast.variables.LetExpr

class ASTViewer(val buf: StringBuilder, val indent: String = "", val isTail: Boolean): ExprVisitor<Unit> {
    fun Expr.ast(buf: StringBuilder, indent: String = "", isTail: Boolean) = accept(ASTViewer(buf, indent, isTail))

    fun Expr.astLabel(label: String, buf: StringBuilder, indent: String, tail: Boolean) {
        buf.append(indent).append(if (tail) "└── " else "├── ").append(label).append(':')

        buf.append('\n')
        ast(buf, indent + if (tail) "    " else "│   ", true)
    }

    private fun defaultAst(string: String) {
        buf.append(indent).append(if (isTail) "└── " else "├── ").append(string)
    }


    private fun label(string: String) {
        buf.append(indent).append(if (isTail) "    " else "│   ").append("└── ").append(string)
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

    override fun visit(expr: NamedFunctionExpr) {
        defaultAst("function ${expr.name}")
        buf.append('\n')

        label(expr.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) -> "$name: $type" })
        label(expr.returnType.toString())

        expr.expr.ast(buf, indent + if (isTail) "    " else "│   ", true)
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

    override fun visit(expr: InvokeExpr) {
        defaultAst("invoke")

        buf.append('\n')
        expr.target.ast(buf, indent + if (isTail) "    " else "│   ", expr.arguments.isEmpty())

        buf.append('\n')
        for (i in 0 until expr.arguments.size - 1) {
            expr.arguments[i].ast(buf, indent, false)
            buf.append('\n')
        }
        if (expr.arguments.isNotEmpty()) {
            expr.arguments.last().ast(buf, indent, true)
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

    override fun visit(expr: LogicalAndOperation) {
        defaultAst("logical and")

        buf.append('\n')
        expr.left.ast(buf, indent + if (isTail) "    " else "│   ", false)

        buf.append('\n')
        expr.right.ast(buf, indent + if (isTail) "    " else "│   ", true)
    }

    override fun visit(expr: LogicalOrOperation) {
        defaultAst("logical or")

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

        buf.append('\n')
        expr.condition.astLabel("condition", buf, indent + if (isTail) "    " else "│   ", false)

        buf.append('\n')
        expr.thenBranch.astLabel("then", buf, indent + if (isTail) "    " else "│   ", expr.elseBranch == null)

        if (expr.elseBranch != null) {
            buf.append('\n')
            expr.elseBranch.astLabel("else", buf, indent + if (isTail) "    " else "│   ", true)
        }
    }
}