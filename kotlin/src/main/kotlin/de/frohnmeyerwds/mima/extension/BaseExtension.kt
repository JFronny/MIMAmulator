package de.frohnmeyerwds.mima.extension

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO

object BaseExtension : Extension {
    override val name: String get() = "base"
    override val instructions0: Map<String, Instruction0> get() = mapOf(
        "HALT" to BroadInstruction0(0xF0.toUByte()) {
            println("HALT at address ${iar - 1}")
            false
        },
        "NOT" to BroadInstruction0(0xF1.toUByte()) {
            akku = akku.inv()
            true
        },
        "RAR" to BroadInstruction0(0xF2.toUByte()) {
            akku = (akku shr 1) or (akku shl 23)
            true
        },
    )
    override val instructions1: Map<String, Instruction1> get() = mapOf(
        "LDC" to SlimInstruction1(0x0.toUByte(), true) {
            akku = arg(ir)
            true
        },
        "LDV" to SlimInstruction1(0x1.toUByte()) {
            akku = memory[arg(ir)]
            true
        },
        "STV" to SlimInstruction1(0x2.toUByte()) {
            memory[arg(ir)] = akku
            true
        },
        "ADD" to SlimInstruction1(0x3.toUByte()) {
            akku += memory[arg(ir)]
            true
        },
        "AND" to SlimInstruction1(0x4.toUByte()) {
            akku = akku and memory[arg(ir)]
            true
        },
        "OR" to SlimInstruction1(0x5.toUByte()) {
            akku = akku or memory[arg(ir)]
            true
        },
        "XOR" to SlimInstruction1(0x6.toUByte()) {
            akku = akku xor memory[arg(ir)]
            true
        },
        "EQL" to SlimInstruction1(0x7.toUByte()) {
            akku = if (akku == memory[arg(ir)]) U24(-1) else ZERO
            true
        },
        "JMP" to SlimInstruction1(0x8.toUByte()) {
            iar = arg(ir)
            true
        },
        "JMN" to SlimInstruction1(0x9.toUByte()) {
            if (akku < ZERO) iar = arg(ir)
            true
        },
    )
}
