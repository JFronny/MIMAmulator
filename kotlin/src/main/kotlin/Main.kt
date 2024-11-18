package de.frohnmeyer_wds

import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.reader
import kotlin.io.path.writeBytes

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: Assembler <file>")
        println("       Assembler assemble <source> [target]")
        println("       Assembler disassemble <file>")
        println("       Assembler interpret <file> [start]")
        return
    }
    if (args.size == 1) {
        main(arrayOf("assemble", args[0]))
        return
    }
    when (args[0]) {
        "assemble" -> {
            if (args.size !in 2..3) {
                println("Usage: Assembler assemble <source> [target]")
                println("       target: target file (default: source with .mbf extension)")
                return
            }
            if (args.size == 2) {
                main(arrayOf(args[0], args[1], args[1] + ".mbf"))
                return
            }
            Path(args[2]).writeBytes(Path(args[1]).reader().use { assemble(it) }.toByteArray())
        }
        "disassemble" -> {
            if (args.size != 2) {
                println("Usage: Assembler disassemble <file>")
                return
            }
            val dyBuf = DyBuf()
            dyBuf += Path(args[1]).readBytes()
            disassemble(dyBuf, System.out.writer())
        }
        "interpret" -> {
            if (args.size !in 2..3) {
                println("Usage: Assembler interpret <file> [start]")
                println("       start: start address in hex (default: 0)")
                return
            }
            if (args.size == 2) {
                main(arrayOf(args[0], args[1], "0"))
                return
            }
            val dyBuf = DyBuf()
            dyBuf += Path(args[1]).readBytes()
            interpret(dyBuf, U24.parse(args[2]))
        }
        else -> println("Unknown command: ${args[0]}")
    }
}