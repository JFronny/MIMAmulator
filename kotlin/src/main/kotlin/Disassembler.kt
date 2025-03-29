package de.frohnmeyer_wds

import java.io.Writer

fun disassemble(dyBuf: DyBuf, writer: Writer) {
    val labeled = mutableSetOf<U24>()
    dyBuf.forEachIndexed { pos, it ->
        if ((it.value and 0xF00000 shr 20) in (0x1..0x9)) {
            labeled.add(arg(it))
        }
    }

    var writing = false
    val constants = mutableMapOf<U24, String>()
    dyBuf.forEachIndexed { pos, it ->
        if (it.value == 0) {
            if (writing) {
                writer.appendLine()
            }
            writing = false
            return@forEachIndexed
        }
        if (!writing) {
            writing = true
            writer.appendLine("* = " + pos.toU24())
        }
        if (pos.toU24() in labeled) {
            writer.appendLine(dataStore(pos.toU24(), it, constants))
        } else {
            writer.appendLine(disassemble(pos.toU24(), it, constants))
        }
    }
    constants.forEach { (pos, name) ->
        writer.appendLine("$name = ${pos.value}")
    }
}

private fun disassemble(position: U24, command: U24, constants: MutableMap<U24, String>): String {
    return when (command.value and 0xF00000 shr 20) {
        0x0 -> "LDC ${constant(arg(command), constants)}"
        0x1 -> "LDV ${label(arg(command))}"
        0x2 -> "STV ${label(arg(command))}"
        0x3 -> "ADD ${label(arg(command))}"
        0x4 -> "AND ${label(arg(command))}"
        0x5 -> "OR ${label(arg(command))}"
        0x6 -> "XOR ${label(arg(command))}"
        0x7 -> "EQL ${label(arg(command))}"
        0x8 -> "JMP ${label(arg(command))}"
        0x9 -> "JMN ${label(arg(command))}"
        0xF -> when (command.value and 0xF0000 shr 16) {
            0x0 -> "HALT ; ${arg(command)}"
            0x1 -> "NOT ; ${arg(command)}"
            0x2 -> "RAR ; ${arg(command)}"
            else -> dataStore(position, command, constants)
        }
        else -> dataStore(position, command, constants)
    }
}

private fun dataStore(position: U24, value: U24, constants: MutableMap<U24, String>): String {
    return "${label(position)} DS ${constant(value, constants)}"
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}

private fun label(pos: U24): String {
    return "l" + pos.value.toString(16).padStart(6, '0')
}

private fun constant(pos: U24, constants: MutableMap<U24, String>): String {
    if (pos in constants) {
        return constants[pos]!!
    }
    val name = "c" + pos.value.toString(16).padStart(6, '0')
    constants[pos] = name
    return name
}