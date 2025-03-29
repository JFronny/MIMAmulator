package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import de.frohnmeyerwds.mima.util.toU24
import java.io.Reader

fun interface Cmd {
    operator fun invoke(arg: U24?, pos: U24): U24
}

fun assemble(reader: Reader, start: U24 = ZERO, knownConstants: Map<String, U24> = mapOf()): DyBuf {
    val dyBuf = DyBuf()

    fun vcmd(opcode: UByte): Cmd = Cmd { a, pos ->
        dyBuf[pos] = (opcode.toU24() shl 20) or (a ?: error("Missing argument"))
        pos + 1
    }

    fun bcmd(opcode: UByte): (U24) -> U24 = { pos ->
        dyBuf[pos] = (opcode.toU24() shl 16)
        pos + 1
    }

    val codes0 = mapOf(
        "HALT" to bcmd(0xF0.toUByte()),
        "NOT"  to bcmd(0xF1.toUByte()),
        "RAR"  to bcmd(0xF2.toUByte()),
    )
    val codes1 = mapOf(
        "LDC"  to vcmd(   0.toUByte()),
        "LDV"  to vcmd(   1.toUByte()),
        "STV"  to vcmd(   2.toUByte()),
        "ADD"  to vcmd(   3.toUByte()),
        "AND"  to vcmd(   4.toUByte()),
        "OR"   to vcmd(   5.toUByte()),
        "XOR"  to vcmd(   6.toUByte()),
        "EQL"  to vcmd(   7.toUByte()),
        "JMP"  to vcmd(   8.toUByte()),
        "JMN"  to vcmd(   9.toUByte()),
    )

    val constants = knownConstants.toMutableMap()
    val orConstants = mutableMapOf<String, MutableSet<U24>>()
    var line = 1

    fun readWord(): String? = buildString {
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

    fun readChar(ch: Char, next: () -> Char?): Char? {
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

    fun readU24(pos: U24?, word: String = readWord() ?: error("Unexpected end of file")): U24 {
        return U24.tryParse(word) ?: constants[word] ?: run {
            orConstants.computeIfAbsent(word) { mutableSetOf() }.add(pos ?: error("Invalid constant: $word"))
            ZERO
        }
    }

    var pos = start
    while (true) {
        var word = readWord() ?: break
        val labels = mutableListOf<String>()
        while (word.endsWith(':')) {
            labels.add(word.dropLast(1))
            word = readWord() ?: error("Unexpected end of file")
        }
        if (word in codes0) {
            labels.forEach { constants[it] = pos }
            pos = codes0[word]!!(pos)
            continue
        }
        if (word in codes1) {
            val next = readWord()
            labels.forEach { constants[it] = pos }
            val code = codes1[word]!!
            pos = code(next?.let { readU24(pos, it) }, pos)
            continue
        }
        val next = readWord()
        when (next) {
            "DS" -> {
                dyBuf[pos] = readU24(pos)
                constants[word] = pos
                labels.forEach { constants[it] = pos }
                pos++
            }
            "=" -> {
                if (labels.isNotEmpty()) error("Unexpected '=' in line $line")
                if (word == "*") {
                    pos = readU24(null)
                } else {
                    constants[word] = readU24(null)
                }
            }
            else -> error("Unknown opcode: $word in line $line")
        }
    }

    orConstants.forEach { (k, set) -> set.forEach {
        dyBuf[it] = dyBuf[it] or (constants[k] ?: error("Unknown constant: $k"))
    } }

    return dyBuf
}
