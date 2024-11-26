package de.frohnmeyer_wds

import java.io.Reader
import java.io.StringReader

fun assemble(reader: Reader, start: U24 = U24(0), knownConstants: Map<String, U24> = mapOf()): DyBuf {
    val dyBuf = DyBuf()

    fun vcmd(opcode: UByte): (U24?, U24) -> U24 = { a, pos ->
        dyBuf[pos] = (opcode.toU24() shl 20) or (a ?: error("Missing argument"))
        pos + 1
    }

    fun bcmd(opcode: UByte): (U24) -> U24 = { pos ->
        dyBuf[pos] = (opcode.toU24() shl 16)
        pos + 1
    }

    fun bacmd(opcode: UByte): (U24?, U24) -> U24 = { a, pos ->
        dyBuf[pos] = (opcode.toU24() shl 16) or (a ?: error("Missing argument"))
        pos + 1
    }

    fun pacmd(code: String): (U24?, U24) -> U24 = { a, pos ->
        // Warning: If a pseudo-instruction uses a different pseudo-instruction in its implementation,
        //          the parameters MUST be available BEFORE that pseudo-instruction is called.
        //          Make sure you use no down-jumps or data stores defined later in the code!
        val buf = assemble(StringReader(code), pos, mapOf(
            "a1" to (a ?: error("Missing argument"))
        ))
        dyBuf[pos] = buf.copyOfRange(pos.value, buf.size)
        pos + (buf.size - pos.value)
    }

    val codes0 = mapOf(
        "HALT" to bcmd(0xF0.toUByte()),
        "NOT"  to bcmd(0xF1.toUByte()),
        "RAR"  to bcmd(0xF2.toUByte()),
    )
    val codes1 = mapOf(
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

        "LDI" to pacmd("""
            LDV il
            ADD a1
            STV cm
            JMP cm
        il: LDV 0
        cm: LDV 0
        """.trimIndent()),

        "OUTS" to pacmd("""
            JMP prepare
            
        arg DS 0
        end DS 0
        pos DS 0
        
        prepare:
            STV arg
            LDC 1
            ADD arg
            STV pos
            LDI arg
            ADD pos
            STV end
        
        print:
            LDV pos
            EQL end
            JMN _end
            LDI pos
            OUT a1
            LDC 1
            ADD pos
            STV pos
            JMP print
        _end:
            LDV arg
        """.trimIndent()),
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
        if (word.getOrNull(0) == '\'') {
            if (word.length !in 3..4 || word.last() != '\'') error("Invalid character: $word")
            var i = 1
            val ch = readChar(word[i++]) { word.getOrNull(i++) }?.code ?: error("Invalid character: $word")
            if (i != word.length - 1) error("Invalid character: $word")
            return U24(ch)
        }
        return U24.tryParse(word) ?: constants[word] ?: run {
            orConstants.computeIfAbsent(word) { mutableSetOf() }.add(pos ?: error("Invalid constant: $word"))
            U24(0)
        }
    }

    fun readStringOrU24(pos: U24?): Either<U24, String> {
        val word = readWord() ?: error("Unexpected end of file")
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
            readU24(pos, word).eitherLeft()
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
            pos = codes1[word]!!(next?.let { readU24(pos, it) }, pos)
            continue
        }
        val next = readWord()
        when (next) {
            "DS" -> {
                constants[word] = pos
                labels.forEach { constants[it] = pos }
                readStringOrU24(pos).fold(
                    { number ->
                        dyBuf[pos] = number
                        pos++
                    },
                    { string ->
                        dyBuf[pos++] = U24(string.length)
                        string.forEach {
                            dyBuf[pos++] = U24(it.code)
                        }
                    }
                )
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