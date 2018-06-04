package xyz.avarel.lobos.parser

import xyz.avarel.lobos.lexer.Position

class SyntaxException(message: String, val position: Position): RuntimeException("$message at $position")