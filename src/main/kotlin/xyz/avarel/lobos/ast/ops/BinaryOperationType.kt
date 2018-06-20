package xyz.avarel.lobos.ast.ops

enum class BinaryOperationType(val functionName: String) {
    ADD("plus"),
    SUBTRACT("minus"),
    MULTIPLY("times"),
    DIVIDE("div")
}