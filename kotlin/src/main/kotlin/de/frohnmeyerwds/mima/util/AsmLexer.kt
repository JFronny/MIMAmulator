package de.frohnmeyerwds.mima.util

import java.io.Reader

data class ReaderScope(
    val reader: Reader,
    val constants: MutableMap<String, U24>,
    var line: Int = 1,
)

fun ReaderScope.readWord(): String? = buildString {
    var i = reader.read()

    fun skipComment(): Boolean = if (i.toChar() != ';') false else run {
        while (i != -1 && i.toChar() != '\n') {
            i = reader.read()
        }
        if (i.toChar() == '\n') line++
        true
    }

    while (i != -1 && i.toChar().isWhitespace()) {
        if (i.toChar() == '\n') line++
        i = reader.read()
    }
    if (i == -1) return null
    if (skipComment()) return readWord()
    while (i != -1 && !i.toChar().isWhitespace()) {
        if (skipComment()) break
        append(i.toChar())
        i = reader.read()
    }
    if (i.toChar() == '\n') line++
}

fun ReaderScope.readChar(ch: Char, next: () -> Char?): Char? {
    return when (ch) {
        '\\' -> when (next()) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '0' -> '\u0000'
            null -> error("Unexpected escape sequence \"\\ \" in line $line")
            else -> error("Unknown escape sequence \"\\$ch\" in line $line")
        }
        '\"' -> null
        '\n' -> error("Unexpected end of line in line $line")
        else -> ch
    }
}

fun ReaderScope.readU24(word: String = readWord() ?: error("Unexpected end of file")): U24? {
    if (word.getOrNull(0) == '\'') {
        if (word.length !in 3..4 || word.last() != '\'') error("Invalid character: $word")
        var i = 1
        val ch = readChar(word[i++]) { word.getOrNull(i++) }?.code ?: error("Invalid character: $word")
        if (i != word.length - 1) error("Invalid character: $word")
        return U24(ch)
    }
    return U24.tryParse(word) ?: constants[word]
}

fun ReaderScope.readStringOrU24(pos: U24?, word: String = readWord() ?: error("Unexpected end of file")): Either<U24, String>? {
    return if (word.getOrNull(0) == '\"') {
        buildString {
            var i = 1
            while (i < word.length) {
                append(readChar(word[i++]) { word.getOrNull(i++) } ?: return@buildString)
            }
            append(' ')
            i = reader.read()
            while (i != -1 && i.toChar() != '\"') {
                append(readChar(i.toChar()) { i = reader.read(); if (i == -1) null else i.toChar() } ?: return@buildString)
                i = reader.read()
            }
        }.eitherRight()
    } else {
        readU24(word)?.eitherLeft()
    }
}
