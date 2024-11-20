package de.frohnmeyer_wds

import java.io.Reader

fun assemble(reader: Reader): DyBuf {
    val dyBuf = DyBuf()

    fun vcmd(opcode: UByte): (U24?, U24) -> Unit = { a, pos ->
        dyBuf[pos] = (opcode.toU24() shl 20) or (a ?: error("Missing argument"))
    }

    fun bcmd(opcode: UByte): (U24) -> Unit = { pos ->
        dyBuf[pos] = (opcode.toU24() shl 16)
    }

    fun bacmd(opcode: UByte): (U24?, U24) -> Unit = { a, pos ->
        dyBuf[pos] = (opcode.toU24() shl 16) or (a ?: error("Missing argument"))
    }

    val codes = mapOf(
        "LDC" to vcmd(0x0.toUByte()),
        "LDV" to vcmd(0x1.toUByte()),
        "STV" to vcmd(0x2.toUByte()),
        "ADD" to vcmd(0x3.toUByte()),
        "AND" to vcmd(0x4.toUByte()),
        "OR"  to vcmd(0x5.toUByte()),
        "XOR" to vcmd(0x6.toUByte()),
        "EQL" to vcmd(0x7.toUByte()),
        "JMP" to vcmd(0x8.toUByte()),
        "JMN" to vcmd(0x9.toUByte()),

        "IN"  to bacmd(0xF3.toUByte()),
        "OUT" to bacmd(0xF4.toUByte()),
    )
    val codes2 = mapOf(
        "HALT" to bcmd(0xF0.toUByte()),
        "NOT"  to bcmd(0xF1.toUByte()),
        "RAR"  to bcmd(0xF2.toUByte()),
    )

    val constants = mutableMapOf<String, U24>()
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

    fun readU24(pos: U24?, word: String = readWord() ?: error("Unexpected end of file")): U24 {
        return U24.tryParse(word) ?: constants[word] ?: run {
            orConstants.computeIfAbsent(word) { mutableSetOf() }.add(pos ?: error("Invalid constant: $word"))
            U24(0)
        }
    }

    var pos = U24(0)
    while (true) {
        var word = readWord() ?: break
        val labels = mutableListOf<String>()
        while (word.endsWith(':')) {
            labels.add(word.dropLast(1))
            word = readWord() ?: error("Unexpected end of file")
        }
        if (word in codes) {
            val code = codes[word]!!
            val next = readWord()
            code(next?.let { readU24(pos, it) }, pos)
            labels.forEach { constants[it] = pos }
            pos++
            continue
        }
        if (word in codes2) {
            val code = codes2[word]!!
            code(pos)
            labels.forEach { constants[it] = pos }
            pos++
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