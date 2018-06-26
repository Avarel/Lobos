package xyz.avarel.lobos.lexer

data class Token(val type: TokenType, val string: String, val position: Section) {
    override fun toString() = "$type[$string] $position"
}