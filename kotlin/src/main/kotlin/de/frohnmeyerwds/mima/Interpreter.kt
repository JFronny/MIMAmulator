package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import de.frohnmeyerwds.mima.util.toU24

class Mima(val memory: DyBuf, start: U24) {
    private var iar = start
    private var ir: U24 = ZERO
    private var akku = ZERO

    fun executeSingle(): Boolean {
        ir = memory[iar++]
        when (ir.value and 0xF00000 shr 20) {
            0x0 -> akku = arg(ir)
            0x1 -> akku = memory[arg(ir)]
            0x2 -> memory[arg(ir)] = akku
            0x3 -> akku += memory[arg(ir)]
            0x4 -> akku = akku and memory[arg(ir)]
            0x5 -> akku = akku or memory[arg(ir)]
            0x6 -> akku = akku xor memory[arg(ir)]
            0x7 -> akku = if (akku == memory[arg(ir)]) U24(-1) else ZERO
            0x8 -> iar = arg(ir)
            0x9 -> if (akku < ZERO) iar = arg(ir)
            0xF -> when (ir.value and 0xF0000 shr 16) {
                0x0 -> {
                    println("HALT at address ${iar - 1}")
                    return false
                }
                0x1 -> akku = akku.inv()
                0x2 -> akku = (akku shr 1) or (akku shl 23)
                else -> {
                    println("Error: Unknown command: $ir (address ${iar - 1})")
                    return false
                }
            }
            else -> {
                println("Error: Unknown command: $ir (address ${iar - 1})")
                return false
            }
        }
        return true
    }

    fun interpret() {
        while (executeSingle()) { }
    }

    override fun toString(): String = buildString {
        memory.forEachIndexed { i, value ->
            if (i != 0) append(' ')
            if (i == iar.value) append('<')
            append(value.toStringFlat())
            if (i == iar.value) {
                append('|')
                append(akku.toStringFlat())
                append(">")
            }
        }
    }
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}
