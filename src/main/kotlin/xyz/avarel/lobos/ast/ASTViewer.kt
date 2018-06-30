package xyz.avarel.lobos.ast

import xyz.avarel.lobos.ast.expr.Expr
import xyz.avarel.lobos.ast.expr.ExprVisitor
import xyz.avarel.lobos.ast.expr.access.IndexAccessExpr
import xyz.avarel.lobos.ast.expr.access.PropertyAccessExpr
import xyz.avarel.lobos.ast.expr.access.TupleIndexAccessExpr
import xyz.avarel.lobos.ast.expr.declarations.LetExpr
import xyz.avarel.lobos.ast.expr.declarations.ModuleExpr
import xyz.avarel.lobos.ast.expr.declarations.NamedFunctionExpr
import xyz.avarel.lobos.ast.expr.declarations.TypeAliasExpr
import xyz.avarel.lobos.ast.expr.external.ExternalLetExpr
import xyz.avarel.lobos.ast.expr.external.ExternalNamedFunctionExpr
import xyz.avarel.lobos.ast.expr.invoke.InvokeExpr
import xyz.avarel.lobos.ast.expr.invoke.InvokeMemberExpr
import xyz.avarel.lobos.ast.expr.misc.IfExpr
import xyz.avarel.lobos.ast.expr.misc.InvalidExpr
import xyz.avarel.lobos.ast.expr.misc.MultiExpr
import xyz.avarel.lobos.ast.expr.misc.TemplateExpr
import xyz.avarel.lobos.ast.expr.nodes.*
import xyz.avarel.lobos.ast.expr.ops.BinaryOperation
import xyz.avarel.lobos.ast.expr.ops.UnaryOperation
import xyz.avarel.lobos.ast.expr.variables.AssignExpr

class ASTViewer(val buf: StringBuilder, val indent: String = "", val isTail: Boolean): ExprVisitor<Unit> {
    fun Expr.ast(indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) = accept(ASTViewer(buf, indent, tail))

    fun Expr.astLabel(label: String, indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) {
        listOf(this).astGroup(label, indent, tail)
    }

    fun List<Expr>.astGroup(label: String, indent: String = this@ASTViewer.indent + if (isTail) "    " else "│   ", tail: Boolean) {
        if (isEmpty()) return

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

        expr.declarationsAST.let { declarations ->
            declarations.modules.astGroup("submodules", tail = false)
            declarations.functions.astGroup("functions", tail = false)
            declarations.variables.astGroup("variables", tail = true)
        }
    }

    override fun visit(expr: NamedFunctionExpr) {
        defaultAst("function")
        label("name: ${expr.name}", false)

        if (expr.generics.isNotEmpty()) {
            label(expr.generics.joinToString(prefix = "generics: <", postfix = ">"), false)
        }

        label(expr.arguments.joinToString(prefix = "arguments: (", postfix = ")"), false)

        label("return: ${expr.returnType}", false)

        expr.body.astLabel("body", tail = true)
    }

    override fun visit(expr: LetExpr) {
        defaultAst("let")
        label("name: ${expr.name}", false)

        label("type: ${expr.type}", false)

        expr.value.ast(tail = true)
    }

    override fun visit(expr: TypeAliasExpr) {
        defaultAst("typealias")

        label("name: ${expr.name}", false)

        if (expr.generics.isNotEmpty()) {
            label(expr.generics.joinToString(prefix = "generics: <", postfix = ">"), false)
        }

        label("type: ${expr.type}", true)
    }

    override fun visit(expr: ExternalLetExpr) {
        defaultAst("external let")
        label("name: ${expr.name}", true)
    }

    override fun visit(expr: ExternalNamedFunctionExpr) {
        defaultAst("external function")
        label("name: ${expr.name}", true)

        if (expr.generics.isNotEmpty()) {
            label(expr.generics.joinToString(prefix = "generics: <", postfix = ">"), false)
        }

        label(expr.arguments.joinToString(prefix = "arguments: (", postfix = ")"), false)

        label("return: ${expr.returnType}", true)
    }

    override fun visit(expr: AssignExpr) {
        defaultAst("assign ${expr.name}")

        expr.value.ast(tail = true)
    }

    override fun visit(expr: IdentExpr) = defaultAst("variable ${expr.name}")

    override fun visit(expr: TupleExpr) {
        defaultAst("tuple")

        for (i in 0 until expr.list.size - 1) {
            expr.list[i].ast(tail = false)
        }
        if (expr.list.isNotEmpty()) {
            expr.list.last().ast(tail = true)
        }
    }

    override fun visit(expr: TemplateExpr) {
        defaultAst("template")

        expr.target.astLabel("target", tail = false)

        label(expr.typeArguments.joinToString(prefix = "type arguments: <", postfix = ">"), true)
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

    override fun visit(expr: TupleIndexAccessExpr) {
        defaultAst("tuple index access")

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