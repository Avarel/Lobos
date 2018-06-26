package xyz.avarel.lobos.lexer

import java.io.BufferedReader
import java.io.Reader

class Tokenizer(val fileName: String = "_", reader: Reader) {
    constructor(string: String) : this(reader = string.reader())

    private val reader: Reader = if (reader.markSupported()) reader else BufferedReader(reader)

    private var lineNumber: Long = 1
    private var lineIndex: Long = 0

    fun parse() = mutableListOf<Token>().also(::parseTo)

    private fun parseTo(list: MutableList<Token>) {
        while (hasNext()) {
            parseCharTo(list, next())
        }
    }

    private fun parseCharTo(list: MutableList<Token>, c: Char) {
        when (c) {
            '{' -> list.add(makeToken(TokenType.L_BRACE))
            '}' -> list.add(makeToken(TokenType.R_BRACE))
            '(' -> list.add(makeToken(TokenType.L_PAREN))
            ')' -> list.add(makeToken(TokenType.R_PAREN))
            '[' -> list.add(makeToken(TokenType.L_BRACKET))
            ']' -> list.add(makeToken(TokenType.R_BRACKET))
            '.' -> when {
                match('.') -> when {
                    match('=') -> list.add(makeToken(TokenType.RANGE_IN, 3))
                    match('<') -> list.add(makeToken(TokenType.RANGE_EX, 3))
                }
                else -> list.add(makeToken(TokenType.DOT))
            }
            ',' -> list.add(makeToken(TokenType.COMMA))
            ':' -> list.add(makeToken(TokenType.COLON))
            ';' -> list.add(makeToken(TokenType.SEMICOLON))
            '\n' -> list.add(makeToken(TokenType.NL))
            '+' -> list.add(makeToken(TokenType.PLUS))
            '-' -> when {
                match('>') -> list.add(makeToken(TokenType.ARROW, 2))
                else -> list.add(makeToken(TokenType.MINUS))
            }
            '*' -> list.add(makeToken(TokenType.ASTERISK))
            '/' -> list.add(makeToken(TokenType.F_SLASH))
            '\\' -> list.add(makeToken(TokenType.B_SLASH))
            '!' -> when {
                match('=') -> list.add(makeToken(TokenType.NEQ, 2))
                else -> list.add(makeToken(TokenType.BANG))
            }
            '?' -> list.add(makeToken(TokenType.QUESTION))
            '=' -> when {
                match('=') -> list.add(makeToken(TokenType.EQ, 2))
                else -> list.add(makeToken(TokenType.ASSIGN))
            }
            '|' -> when {
                match('|') -> list.add(makeToken(TokenType.OR, 2))
                match('>') -> list.add(makeToken(TokenType.PIPE_FORWARD, 2))
                else -> list.add(makeToken(TokenType.PIPE))
            }
            '&' -> when {
                match('&') -> list.add(makeToken(TokenType.AND, 2))
                else -> list.add(makeToken(TokenType.AMP))
            }
            '<' -> when {
                match('=') -> list.add(makeToken(TokenType.LTE, 2))
                else -> list.add(makeToken(TokenType.LT))
            }
            '>' -> when {
                match('=') -> list.add(makeToken(TokenType.GTE, 2))
                else -> list.add(makeToken(TokenType.GT))
            }
            '"' -> parseStringTo(list, '"', true)
            '\'' -> parseStringTo(list, '\'', false)
            ' ' -> Unit
            else -> when {
                c.isDigit() -> parseNumberTo(list, c)
                c.isLetter() -> parseIdentTo(list, c)
                else -> list.add(makeToken(TokenType.INVALID, c.toString()))
            }
        }
    }

    private fun makeToken(tokenType: TokenType, offset: Int = 1) = makeToken(tokenType, "", offset)
    private fun makeToken(tokenType: TokenType, string: String, offset: Int = 0) = Token(
            tokenType,
            string,
            Section(fileName, lineNumber, lineIndex - string.length - offset, string.length + offset)
    )

    private fun parseIdentTo(list: MutableList<Token>, c: Char) {
        val buf = StringBuilder()
        buf.append(c)

        while (hasNext()) {
            val cc = peek()
            if (cc.isLetterOrDigit() || cc == '_') {
                buf.append(cc)
                next()
            } else {
                break
            }
        }

        val str = buf.toString()
        when (str) {
            "true" -> list.add(makeToken(TokenType.TRUE, 4))
            "false" -> list.add(makeToken(TokenType.FALSE, 4))
            "let" -> list.add(makeToken(TokenType.LET, 3))
            "mut" -> list.add(makeToken(TokenType.MUT, 3))
            "return" -> list.add(makeToken(TokenType.RETURN, 6))
            "mod" -> list.add(makeToken(TokenType.MOD, 3))
            "if" -> list.add(makeToken(TokenType.IF, 2))
            "else" -> list.add(makeToken(TokenType.ELSE, 4))
            "null" -> list.add(makeToken(TokenType.NULL, 4))
            "type" -> list.add(makeToken(TokenType.TYPE, 4))
            "def" -> list.add(makeToken(TokenType.DEF, 3))
            "external" -> list.add(makeToken(TokenType.EXTERNAL, 8))
            else -> list.add(makeToken(TokenType.IDENT, str))
        }
    }

    private fun parseStringTo(list: MutableList<Token>, delim: Char, template: Boolean) {
        var buf = StringBuilder()
        var eol = false

        while (hasNext()) {
            val c = peek()
            if (c == '$' && template) {
                next()

                if (peek() == '{') {
                    next()

                    list.add(makeToken(TokenType.STRING, buf.toString()))
                    list.add(makeToken(TokenType.PLUS))
                    buf = StringBuilder()

                    var braces = 0

                    list.add(makeToken(TokenType.L_PAREN))

                    while (hasNext()) {
                        val cc = next()
                        if (cc == '}') {
                            if (braces == 0) {
                                break
                            } else {
                                braces--
                                parseCharTo(list, cc)
                            }
                        } else if (cc == '{') {
                            braces++
                            parseCharTo(list, cc)
                        } else parseCharTo(list, cc)
                    }

                    list.add(makeToken(TokenType.R_PAREN))
                    list.add(makeToken(TokenType.PLUS))
                } else if (peek().isLetter()) {
                    list.add(makeToken(TokenType.STRING, buf.toString()))
                    list.add(makeToken(TokenType.PLUS))
                    buf = StringBuilder()

                    buf.append(next())

                    while (hasNext() && peek().isLetterOrDigit()) {
                        buf.append(next())
                    }

                    list.add(makeToken(TokenType.PLUS))
                } else {
                    buf.append(next())
                }
            } else if (c == delim) {
                next()
                eol = true
                break
            } else {
                next()
                buf.append(c)
            }
        }

        if (!eol) {
            throw IllegalStateException("Unterminated string")
        }

        list.add(makeToken(TokenType.STRING, buf.toString(), 2))
    }

    private fun parseNumberTo(list: MutableList<Token>, c: Char) {
        val buf = StringBuilder()

        if (c == '0') {
            when {
                match('x') -> {
                    fillBufferNumbers(buf, true)

                    val numberStr = buf.toString().toIntOrNull(16)?.toString()

                    if (numberStr == null) {
                        list.add(makeToken(TokenType.INVALID, buf.toString()))
                        return
                    }

                    list.add(makeToken(TokenType.INT, numberStr))
                    return
                }
                match('b') -> {
                    fillBufferNumbers(buf, false)

                    val numberStr = buf.toString().toIntOrNull(2)?.toString()

                    if (numberStr == null) {
                        list.add(makeToken(TokenType.INVALID, buf.toString()))
                        return
                    }

                    list.add(makeToken(TokenType.INT, numberStr))
                    return
                }
                else -> {
                    buf.append('0')
                }
            }
        } else {
            buf.append(c)
        }

        fillBufferNumbers(buf, false)

        when {
            peek() == '.' && peek(1).isDigit() -> {
                next()
                buf.append('.')
                fillBufferNumbers(buf, false)
                list.add(makeToken(TokenType.DECIMAL, buf.toString()))
            }
            else -> {
                list.add(makeToken(TokenType.INT, buf.toString()))
            }
        }
    }

    private fun fillBufferNumbers(buf: StringBuilder, allowHex: Boolean) {
        while (hasNext()) {
            val c = peek()
            if (c.isDigit() || (allowHex && c in 'A'..'F')) {
                buf.append(next())
            } else {
                break
            }
        }
    }

    private fun peek(): Char {
        reader.mark(1)
        val c = reader.read().toChar()
        reader.reset()
        return c
    }

    private fun peek(distance: Int): Char {
        reader.mark(distance + 1)
        val array = CharArray(distance + 1)
        val result = when {
            reader.read(array) < distance + 1 -> (-1).toChar()
            else -> array[distance]
        }
        reader.reset()
        return result
    }

    private fun peekString(length: Int): String {
        val array = CharArray(length)
        reader.mark(length)
        val len = reader.read(array)
        reader.reset()
        return when {
            len == -1 -> ""
            len < length -> String(array.copyOf(len))
            else -> String(array)
        }
    }

    private fun match(c: Char): Boolean {
        return if (peek() == c) {
            next()
            true
        } else {
            false
        }
    }

    private fun hasNext(): Boolean {
        reader.mark(1)
        val i = reader.read()
        reader.reset()
        return i > 0
    }

    private fun next(): Char {
        val c = reader.read().toChar()

        when (c) {
            '\n' -> {
                lineNumber++
                lineIndex = 0
            }
            else -> {
                lineIndex++
            }
        }

        return c
    }

    private fun nextString(length: Int): String {
        val buf = StringBuilder(length)
        var i = 0
        while (hasNext() && i++ < length) {
            buf.append(next())
        }
        return buf.toString()
    }
}

