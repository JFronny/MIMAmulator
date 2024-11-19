package de.frohnmeyer_wds

fun interpret(memory: DyBuf, start: U24) {
    val mima = Mima(memory, start)
    while (mima.executeSingle()) { }
}

class Mima(val memory: DyBuf, start: U24) {
    private var iar = start
    private var ir: U24 = U24(0)
    private var akku = U24(0)
    private var io = IO()

    fun executeSingle(): Boolean {
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
                    return false
                }
                0x1 -> akku = akku.inv()
                0x2 -> akku = akku shr 1
                0x3 -> akku = io.read(barg(ir))
                0x4 -> io.write(barg(ir), akku)
                else -> {
                    println("Error: Unknown command: $ir\nLast state was:")
                    disassemble(memory, System.out.writer())
                    return false
                }
            }
            else -> {
                println("Error: Unknown command: $ir\nLast state was:")
                disassemble(memory, System.out.writer())
                return false
            }
        }
        return true
    }
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}

private fun barg(command: U24): U24 {
    return (command.value and 0xFFFF).toU24()
}