package xyz.avarel.lobos.lexer

interface Positional {
    val position: Section
}

fun Positional.span(other: Positional) = position.span(other.position)