package xyz.avarel.lobos.lexer

import java.io.File
import java.io.Reader

class Source(val path: String?, val name: String, reader: Reader) {
    constructor(string: String) : this(null, "?", string.reader())
    constructor(file: File) : this(file.path, file.name, file.reader())

    val content = reader.readText()
    val lines = content.lines()

    fun reader(): Reader {
        return content.reader()
    }
}