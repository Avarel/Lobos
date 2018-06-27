package xyz.avarel.lobos.ast.types

class ArgumentParameterAST(val name: String, val type: AbstractTypeAST) {
    override fun toString() = "$name: $type"
}
