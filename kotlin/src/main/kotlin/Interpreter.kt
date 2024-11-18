package de.frohnmeyer_wds

fun interpret(memory: DyBuf, start: U24) {
    var iar = start
    var ir: U24
    var akku = U24(0)

    while (true) {
        ir = memory[iar]
        iar += 1
        when (ir.value and 0xF00000 shr 20) {
            0x0 -> akku = arg(ir)
            0x1 -> akku = memory[arg(ir)]
            0x2 -> memory[arg(ir)] = akku
            0x3 -> akku += memory[arg(ir)]
            0x4 -> akku = akku and memory[arg(ir)]
            0x5 -> akku = akku or memory[arg(ir)]
            0x6 -> akku = akku xor memory[arg(ir)]
            0x7 -> akku = if (akku == memory[arg(ir)]) U24(1) else U24(0)
            0x8 -> iar = arg(ir)
            0x9 -> if (akku < U24(0)) iar = arg(ir)
            0xF -> when (ir.value and 0xF0000 shr 16) {
                0x0 -> {
                    println("HALT\nLast state was:")
                    disassemble(memory, System.out.writer())
                    return
                }
                0x1 -> akku = akku.inv()
                0x2 -> akku = akku shr 1
                else -> {
                    println("Error: Unknown command: $ir\nLast state was:")
                    disassemble(memory, System.out.writer())
                    return
                }
            }
            else -> {
                println("Error: Unknown command: $ir\nLast state was:")
                disassemble(memory, System.out.writer())
                return
            }
        }
    }
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}
