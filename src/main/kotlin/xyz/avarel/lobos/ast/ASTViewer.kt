package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.misc.IfExpr
import xyz.avarel.lobos.ast.misc.InvalidExpr
import xyz.avarel.lobos.ast.misc.InvokeExpr
import xyz.avarel.lobos.ast.misc.MultiExpr
import xyz.avarel.lobos.ast.nodes.*
import xyz.avarel.lobos.ast.ops.*
import xyz.avarel.lobos.ast.variables.AssignExpr
import xyz.avarel.lobos.ast.variables.IdentExpr
import xyz.avarel.lobos.ast.variables.LetExpr

class ASTViewer(val buf: StringBuilder, val indent: String = "", val isTail: Boolean): ExprVisitor<Unit> {
    fun Expr.ast(indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) = accept(ASTViewer(buf, indent, tail))

    fun Expr.astLabel(label: String, indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) {
        listOf(this).astGroup(label, indent, tail)
    }

    fun List<Expr>.astGroup(label: String, indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) {
        label("$label:", tail)

        for (i in indices) {
            this[i].ast(indent = indent + if (tail) "    " else "│   ", tail = i == lastIndex)
        }
    }

    private fun defaultAst(string: String) {
        buf.apply {
            append(indent)
            append(if (isTail) "└── " else "├── ")
            append(string)
            append('\n')
        }
    }

    private fun label(string: String, tail: Boolean) {
        buf.apply {
            append(indent)
            append(if (isTail) "    " else "│   ")
            append(if (tail) "└── " else "├── ")
            append(string)
            append('\n')
        }
    }

    override fun visit(expr: NullExpr) = defaultAst("null ref")

    override fun visit(expr: InvalidExpr) = defaultAst("[Invalid expression.]")

    override fun visit(expr: I32Expr) = defaultAst("i32: ${expr.value}")

    override fun visit(expr: I64Expr) = defaultAst("i64: ${expr.value}")

    override fun visit(expr: F64Expr) = defaultAst("f64: ${expr.value}")

    override fun visit(expr: StringExpr) = defaultAst("\"${expr.value}\"")

    override fun visit(expr: BooleanExpr) = defaultAst(expr.value.toString())

    override fun visit(expr: UnitExpr) = defaultAst("()")

    override fun visit(expr: MultiExpr) {
        for (i in 0 until expr.list.size - 1) {
            expr.list[i].ast(indent, false)
        }
        if (expr.list.isNotEmpty()) {
            expr.list.last().ast(indent, true)
        }
    }

    override fun visit(expr: ModuleExpr) {
        defaultAst("module ${expr.name}")

        expr.body.ast(tail = true)
    }

    override fun visit(expr: NamedFunctionExpr) {
        defaultAst("function ${expr.name}")

        label(expr.parameters.entries.joinToString(prefix = "parameters: (", postfix = ")") { (name, type) -> "$name: $type" }, false)

        label("return: ${expr.returnType}", false)

        expr.body.astLabel("body", tail = true)
    }

    override fun visit(expr: LetExpr) {
        defaultAst("let ${expr.name}")

        expr.expr.ast(tail = true)
    }

    override fun visit(expr: AssignExpr) {
        defaultAst("assign ${expr.name}")

        expr.expr.ast(tail = true)
    }

    override fun visit(expr: IdentExpr) = defaultAst("variable ${expr.name}")

    override fun visit(expr: TupleExpr) {
        defaultAst("tuple")

        for (i in 0 until expr.list.size - 1) {
            expr.list[i].ast(tail = false)
            buf.append('\n')
        }
        if (expr.list.isNotEmpty()) {
            expr.list.last().ast(tail = true)
        }
    }
    override fun visit(expr: InvokeExpr) {
        defaultAst("invoke")

        expr.target.astLabel("target", tail = expr.arguments.isEmpty())

        expr.arguments.astGroup("arguments", tail = true)
    }

    override fun visit(expr: UnaryOperation) {
        defaultAst("binary ${expr.operator}")

        expr.target.ast(tail = true)
    }

    override fun visit(expr: BinaryOperation) {
        defaultAst("binary ${expr.operator}")

        expr.left.ast(tail = false)

        expr.right.ast(tail = true)
    }

    override fun visit(expr: LogicalAndOperation) {
        defaultAst("logical and")

        expr.left.ast(tail = false)

        expr.right.ast(tail = true)
    }

    override fun visit(expr: LogicalOrOperation) {
        defaultAst("logical or")

        expr.left.ast(tail = false)

        expr.right.ast(tail = true)
    }

    override fun visit(expr: LogicalNotOperation) {
        defaultAst("logical not")

        expr.target.ast(tail = false)
    }

    override fun visit(expr: EqualsOperation) {
        defaultAst("equals")

        expr.left.ast(tail = false)

        expr.right.ast(tail = true)
    }

    override fun visit(expr: ReturnExpr) {
        defaultAst("return")

        expr.expr.ast(tail = true)
    }

    override fun visit(expr: IfExpr) {
        defaultAst("if")

        expr.condition.astLabel("condition", tail = false)

        expr.thenBranch.astLabel("then", tail = expr.elseBranch == null)

        if (expr.elseBranch != null) {
            expr.elseBranch.astLabel("else", tail = true)
        }
    }

    override fun visit(expr: IndexAccessExpr) {
        defaultAst("index access")

        expr.target.astLabel("target", tail = false)

        label("index: ${expr.index}", true)
    }

    override fun visit(expr: PropertyAccessExpr) {
        defaultAst("property access")

        expr.target.astLabel("target", tail = false)

        label("name: ${expr.name}", true)
    }

    override fun visit(expr: InvokeMemberExpr) {
        defaultAst("invoke member function")

        expr.target.astLabel("target", tail = false)

        label("name: ${expr.name}", expr.arguments.isEmpty())

        expr.arguments.astGroup("arguments", tail = true)
    }
}