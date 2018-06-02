package xyz.avarel.lobos.lexer

data class Position(val fileName: String, val lineNumber: Long, val lineIndex: Long) {
    override fun toString() = "($fileName:$lineNumber:$lineIndex)"
}