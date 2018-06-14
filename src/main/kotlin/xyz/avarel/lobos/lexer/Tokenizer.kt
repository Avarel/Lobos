package xyz.avarel.lobos.lexer

import java.io.BufferedReader
import java.io.Reader

class Tokenizer(private val fileName: String = "_", reader: Reader) {
    constructor(string: String) : this(reader = string.reader())

    private val reader: Reader = if (reader.markSupported()) reader else BufferedReader(reader)

    private var lineNumber: Long = 1
    private var lineIndex: Long = 0
    private val position get() = Position(fileName, lineNumber, lineIndex)

    fun parse() = mutableListOf<Token>().also(::parseTo)

    private fun parseTo(list: MutableList<Token>) {
        while (hasNext()) {
            parseCharTo(list, next())
        }
    }

    private fun parseCharTo(list: MutableList<Token>, c: Char) {
        when (c) { //todo ADD characters?
            '{' -> list.add(makeToken(TokenType.L_BRACE, c))
            '}' -> list.add(makeToken(TokenType.R_BRACE, c))
            '(' -> list.add(makeToken(TokenType.L_PAREN, c))
            ')' -> list.add(makeToken(TokenType.R_PAREN, c))
            '[' -> list.add(makeToken(TokenType.L_BRACKET, c))
            ']' -> list.add(makeToken(TokenType.R_BRACKET, c))
            '.' -> when {
                match('.') -> when {
                    match('=') -> list.add(makeToken(TokenType.RANGE_IN, "..="))
                    match('<') -> list.add(makeToken(TokenType.RANGE_EX, "..<"))
                }
                else -> list.add(makeToken(TokenType.DOT, "."))
            }
            ',' -> list.add(makeToken(TokenType.COMMA, c))
            ':' -> list.add(makeToken(TokenType.COLON, c))
            ';' -> list.add(makeToken(TokenType.SEMICOLON, c))
            '+' -> list.add(makeToken(TokenType.PLUS, c))
            '-' -> when {
                match('>') -> list.add(makeToken(TokenType.ARROW, "->"))
                else -> list.add(makeToken(TokenType.MINUS, c))
            }
            '*' -> list.add(makeToken(TokenType.ASTERISK, c))
            '/' -> list.add(makeToken(TokenType.F_SLASH, c))
            '\\' -> list.add(makeToken(TokenType.B_SLASH, c))
            '!' -> if (match('=')) {
                list.add(makeToken(TokenType.NEQ, "!="))
            } else {
                list.add(makeToken(TokenType.BANG, c))
            }
            '?' -> list.add(makeToken(TokenType.QUESTION, c))
            '=' -> if (match('=')) {
                list.add(makeToken(TokenType.EQ, "=="))
            } else {
                list.add(makeToken(TokenType.ASSIGN, c))
            }
            '|' -> if (match('|')) {
                list.add(makeToken(TokenType.OR, c))
            } else {
                list.add(makeToken(TokenType.PIPE, c))
            }
            '&' -> if (match('&')) {
                list.add(makeToken(TokenType.AND, c))
            } else {
                list.add(makeToken(TokenType.AMP, c))
            }
            '<' -> if (match('=')) {
                list.add(makeToken(TokenType.LTE, c))
            } else {
                list.add(makeToken(TokenType.LT, c))
            }
            '>' -> if (match('=')) {
                list.add(makeToken(TokenType.GTE, c))
            } else {
                list.add(makeToken(TokenType.GT, c))
            }
            '"' -> parseStringTo(list, '"', true)
            '\'' -> parseStringTo(list, '\'', false)
            ' ', '\n' -> {
            }
            else -> when {
                c.isDigit() -> parseNumberTo(list, c)
                c.isLetter() -> parseIdentTo(list, c)
                else -> list.add(makeToken(TokenType.INVALID, c.toString()))
            }
        }
    }

    private fun makeToken(tokenType: TokenType, char: Char) = makeToken(tokenType, char.toString())
    private fun makeToken(tokenType: TokenType, string: String) = Token(
            tokenType,
            string,
            position.let { it.copy(lineIndex = it.lineIndex - string.length) }
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
            "true" -> list.add(makeToken(TokenType.TRUE, "true"))
            "false" -> list.add(makeToken(TokenType.FALSE, "false"))
            "let" -> list.add(makeToken(TokenType.LET, "let"))
            "mut" -> list.add(makeToken(TokenType.MUT, "mut"))
            "return" -> list.add(makeToken(TokenType.RETURN, "return"))
            "if" -> list.add(makeToken(TokenType.IF, "if"))
            "else" -> list.add(makeToken(TokenType.ELSE, "else"))
            "null" -> list.add(makeToken(TokenType.NULL, "null"))
            "type" -> list.add(makeToken(TokenType.TYPE, "type"))
            "def" -> list.add(makeToken(TokenType.DEF, "def"))
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
                    list.add(makeToken(TokenType.PLUS, '+'))
                    buf = StringBuilder()

                    var braces = 0

                    list.add(makeToken(TokenType.L_PAREN, '('))

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

                    list.add(makeToken(TokenType.R_PAREN, ')'))
                    list.add(makeToken(TokenType.PLUS, '+'))
                } else if (peek().isLetter()) {
                    list.add(makeToken(TokenType.STRING, buf.toString()))
                    list.add(makeToken(TokenType.PLUS, '+'))
                    buf = StringBuilder()

                    buf.append(next())

                    while (hasNext() && peek().isLetterOrDigit()) {
                        buf.append(next())
                    }

                    list.add(makeToken(TokenType.PLUS, '+'))
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

        list.add(makeToken(TokenType.STRING, buf.toString()))
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
        list.add(makeToken(TokenType.INT, buf.toString()))
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
}

