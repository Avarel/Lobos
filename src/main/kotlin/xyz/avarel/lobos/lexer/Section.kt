package xyz.avarel.lobos.lexer

import kotlin.math.max
import kotlin.math.min

data class Section(val source: Source, val lineNumber: Long, val lineIndex: Long, val length: Int) {
    fun span(other: Section): Section {
        return if (source == other.source && lineNumber == other.lineNumber) {
            val min = min(lineIndex, other.lineIndex)
            val max = max(lineIndex + length, other.lineIndex + other.length)
            Section(source, lineNumber, min, (max - min).toInt())
        } else {
            this
        }
    }

    override fun toString() = "(${source.name}:$lineNumber:$lineIndex)"
}