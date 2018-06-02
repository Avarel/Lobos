package xyz.avarel.lobos.lexer

data class Token(val type: TokenType, val string: String? = null, val position: Position) {
    override fun toString() = buildString {
        append(type)
        if (string != null) {
            append('[')
            append(string)
            append(']')
        }
        append(position)
    }
}