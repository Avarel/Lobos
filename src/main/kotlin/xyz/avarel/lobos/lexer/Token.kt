package xyz.avarel.lobos.lexer

data class Token(val type: TokenType, val string: String, override val position: Section) : Positional {
    override fun toString() = "$type[$string] $position"
}