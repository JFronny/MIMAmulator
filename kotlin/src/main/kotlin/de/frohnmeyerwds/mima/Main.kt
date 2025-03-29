package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.io.ConsolePort
import de.frohnmeyerwds.mima.io.Port
import de.frohnmeyerwds.mima.io.ReadOnlyPort
import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import java.io.RandomAccessFile
import kotlin.io.path.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: Assembler <file>")
        println("       Assembler assemble <source> [target]")
        println("       Assembler disassemble <file>")
        println("       Assembler interpret <file> [start]")
        println("       Assembler performance <file> [iterations]")
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
                main(arrayOf(args[0], args[1], args[1].removeSuffix(".masm") + ".mbf"))
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
            fun help() {
                println("Usage: Assembler interpret <file> [start] [options]")
                println("       start: start address in hex (default: 0)")
                println("Options:")
                println("       -d: disassemble memory after execution")
                return
            }

            val start = if (args.size == 2 || args[2].startsWith("-")) ZERO else U24.parse(args[2])
            val ports = mutableListOf<Port>()
            var disassemble = false
            for (i in 3..<args.size) {
                if (args[i] == "-d") {
                    disassemble = true
                    continue
                } else if (args[i].startsWith("--port.")) {
                    val split = args[i].substring("--port.".length).split('=', limit = 2)
                    val key = split[0]
                    val value = split.getOrNull(1)
                    when (key) {
                        "console" -> {
                            if (value == null) {
                                ports.add(ConsolePort())
                            } else {
                                val path = Path(value)
                                ports.add(ConsolePort(path.inputStream(), path.outputStream()))
                            }
                        }
                        "ro" -> {
                            if (value == null) {
                                println("No file specified for read-only port")
                                ports.forEach { it.close() }
                                return
                            }
                            ports.add(ReadOnlyPort(RandomAccessFile(value, "r")))
                        }
                        else -> {
                            println("Unknown port: $key")
                            ports.forEach { it.close() }
                            return
                        }
                    }
                } else {
                    println("Unknown option: ${args[i]}")
                    help()
                    return
                }
            }

            if (ports.isEmpty()) ports.add(ConsolePort())
            val dyBuf = DyBuf()
            dyBuf += Path(args[1]).readBytes()
            Mima(dyBuf, ports, start).interpret()
            ports.forEach { it.close() }
            if (disassemble) {
                println("Last state was:")
                disassemble(dyBuf, System.out.writer())
            }
        }

        "performance" -> {
            if (args.size !in 2..3) {
                println("Usage: Assembler performance <file> [iterations]")
                println("       iterations: maximum number of instructions to execute (default: 50000)")
                return
            }
            if (args.size == 2) {
                main(arrayOf(args[0], args[1], "50000"))
                return
            }
            val iterationCount = args[2].toInt()
            val dyBuf = DyBuf()
            dyBuf += Path(args[1]).readBytes()
            val mima = Mima(dyBuf, listOf(ConsolePort()), ZERO)
            val start = System.nanoTime()
            for (i in 0 ..< iterationCount) {
                if (!mima.executeSingle()) {
                    println("Execution stopped prematurely after $i instructions, cancelling performance test")
                    return
                }
            }
            val end = System.nanoTime()
            disassemble(dyBuf, System.out.writer())
            println("Took ${(end - start) / 1000000}ms for $iterationCount instructions at ${iterationCount.toDouble() / (end - start) * 1000} MHz")
        }

        else -> println("Unknown command: ${args[0]}")
    }
}
