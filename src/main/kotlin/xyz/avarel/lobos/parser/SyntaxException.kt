package xyz.avarel.lobos.parser

import xyz.avarel.lobos.lexer.Section

class SyntaxException(message: String, val position: Section) : RuntimeException("$message at $position")