package xyz.avarel.lobos.lexer

import kotlin.math.max
import kotlin.math.min

data class Section(val fileName: String, val lineNumber: Long, val lineIndex: Long, val length: Int) {
    fun span(other: Section): Section {
        return if (fileName == other.fileName && lineNumber == other.lineNumber) {
            val min = min(lineIndex, other.lineIndex)
            val max = max(lineIndex + length, other.lineIndex + other.length)
            Section(fileName, lineNumber, min, (max - min).toInt())
        } else {
            this
        }
    }

    override fun toString() = "($fileName:$lineNumber:$lineIndex)"
}