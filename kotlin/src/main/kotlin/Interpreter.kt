package de.frohnmeyer_wds

import java.nio.file.Path
import kotlin.io.path.readBytes

fun interpret(file: Path, start: U24) {
    val dyBuf = DyBuf()

    dyBuf += file.readBytes()

    var iar = start
    var ir: U24
    var akku = U24(0)

    while (true) {
        ir = dyBuf[iar]
        iar += 1
        when (ir.value and 0xF00000 shr 20) {
            0x0 -> akku = arg(ir)
            0x1 -> akku = dyBuf[arg(ir)]
            0x2 -> dyBuf[arg(ir)] = akku
            0x3 -> akku += dyBuf[arg(ir)]
            0x4 -> akku = akku and dyBuf[arg(ir)]
            0x5 -> akku = akku or dyBuf[arg(ir)]
            0x6 -> akku = akku xor dyBuf[arg(ir)]
            0x7 -> akku = if (akku == dyBuf[arg(ir)]) U24(1) else U24(0)
            0x8 -> iar = arg(ir)
            0x9 -> if (akku < U24(0)) iar = arg(ir)
            0xF -> when (ir.value and 0xF0000 shr 16) {
                0x0 -> {
                    println("HALT\nLast state was:")
                    disassemble(dyBuf, System.out.writer())
                    return
                }
                0x1 -> akku = akku.inv()
                0x2 -> akku = akku shr 1
                else -> {
                    println("Error: Unknown command: $ir\nLast state was:")
                    disassemble(dyBuf, System.out.writer())
                    return
                }
            }
            else -> {
                println("Error: Unknown command: $ir\nLast state was:")
                disassemble(dyBuf, System.out.writer())
                return
            }
        }
    }
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}
