package de.frohnmeyer_wds

import kotlin.io.path.Path

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: Assembler <file>")
        println("       Assembler assemble <source> <target>")
        println("       Assembler disassemble <file>")
        println("       Assembler interpret <file> [start]")
        return
    }
    if (args.size == 1) {
        assemble(Path(args[0]), Path(args[0] + ".mbf"))
        return
    }
    when (args[0]) {
        "assemble" -> {
            if (args.size != 3) {
                println("Usage: Assembler assemble <source> <target>")
                return
            }
            assemble(Path(args[1]), Path(args[2]))
        }
        "disassemble" -> {
            if (args.size != 2) {
                println("Usage: Assembler disassemble <file>")
                return
            }
            disassemble(Path(args[1]))
        }
        "interpret" -> {
            if (args.size != 2) {
                println("Usage: Assembler interpret <file> [start]")
                println("       start: start address in hex (default: 0)")
                return
            }
            interpret(Path(args[1]), args.getOrNull(2)?.let { U24.parse(it) } ?: U24(0))
        }
        else -> println("Unknown command: ${args[0]}")
    }
}