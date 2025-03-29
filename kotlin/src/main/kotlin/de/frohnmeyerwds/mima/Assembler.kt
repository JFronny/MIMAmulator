package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.extension.Extension
import de.frohnmeyerwds.mima.extension.InsertContext
import de.frohnmeyerwds.mima.util.*
import java.io.Reader

private fun interface Code0 {
    operator fun invoke(pos: U24): U24
}

private fun interface Code1 {
    operator fun invoke(arg: U24, pos: U24): U24
}

class DSCode1(private val dyBuf: DyBuf) : Code1 {
    override fun invoke(arg: U24, pos: U24): U24 {
        dyBuf[pos] = arg
        return ONE
    }
}

private data class Rewrite1(
    val at: U24,
    val size: U24,
    val line: Int,
    val arg: String,
    val insn: Code1,
)

fun assemble(reader: Reader, extensions: Set<Extension>, start: U24 = ZERO, knownConstants: Map<String, U24> = mapOf()): DyBuf = ReaderScope(reader, knownConstants.toMutableMap()).run {
    val dyBuf = DyBuf()

    val codes0 = extensions.flatMap { e ->
        val deps = e.dependencies.map { n -> extensions.first { it.name == n } }.toSet() + e
        e.instructions0.map { (n, i) -> i.run { n to Code0 { pos -> InsertContext(dyBuf, pos, deps).insert() } } }
    }.toMap()
    val codes1 = extensions.flatMap { e ->
        val deps = e.dependencies.map { n -> extensions.first { it.name == n } }.toSet() + e
        e.instructions1.map { (n, i) -> i.run { n to Code1 { arg, pos -> InsertContext(dyBuf, pos, deps).insert(arg) } } }
    }.toMap()

    val rewrite1 = mutableSetOf<Rewrite1>()

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
            val next = readWord()!!
            labels.forEach { constants[it] = pos }
            val code = codes1[word]!!
            val arg = readU24(next)
            val size = code(arg ?: ZERO, pos)
            if (arg == null) rewrite1.add(Rewrite1(pos, size, line, next, code))
            pos += size
            continue
        }
        val next = readWord()
        when (next) {
            "DS" -> {
                constants[word] = pos
                labels.forEach { constants[it] = pos }
                val w = readWord() ?: error("Unexpected end of file")
                val datum = readStringOrU24(pos, w)
                if (datum == null) {
                    // must be a number
                    rewrite1.add(Rewrite1(pos, ONE, line, w, DSCode1(dyBuf)))
                    pos++
                } else {
                    datum.fold(
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
            }
            "=" -> {
                if (labels.isNotEmpty()) error("Unexpected '=' in line $line")
                val datum = readU24() ?: error("Constant not yet defined for $word in line $line (constant definitions must not depend on later definitions)")
                if (word == "*") pos = datum
                else constants[word] = datum
            }
            else -> error("Unknown opcode: $word in line $line")
        }
    }

    for (r in rewrite1) {
        val arg = readU24(r.arg) ?: error("Unknown constant: ${r.arg} in line ${r.line}")
        if (r.insn(arg, r.at) != r.size) error("Instruction width changed with rewrite caused by ${r.arg} in line ${r.line}")
    }

    return dyBuf
}
