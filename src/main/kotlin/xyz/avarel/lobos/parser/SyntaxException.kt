package xyz.avarel.lobos.parser

import xyz.avarel.lobos.lexer.Position

class SyntaxException(message: String, position: Position): RuntimeException("$message at $position")