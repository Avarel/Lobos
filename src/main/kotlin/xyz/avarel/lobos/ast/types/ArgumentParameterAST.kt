package xyz.avarel.lobos.ast.types

class ArgumentParameterAST(val mutable: Boolean, val name: String, val type: AbstractTypeAST) {
    override fun toString() = buildString {
        if (mutable) {
            append("mut ")
        }
        append(name)
        append(": ")
        append(type)
    }
}
