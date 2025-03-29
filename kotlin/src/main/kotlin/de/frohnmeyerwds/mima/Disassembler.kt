package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.extension.*
import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.toU24
import java.io.Writer

private class DismScope(
    extensions: Set<Extension>,
    val constants: MutableMap<U24, String> = mutableMapOf(),
) {
    val broad0 = extensions.flatMap { it.instructions0.entries }
        .mapNotNull { (k, v) -> (v as? BroadInstruction0)?.let { k to it } }
        .associateBy { it.second.concrete.opcode }
    val broad1 = extensions.flatMap { it.instructions1.entries }
        .mapNotNull { (k, v) -> (v as? BroadInstruction1)?.let { k to it } }
        .associateBy { it.second.concrete.opcode }
    val slim1 = extensions.flatMap { it.instructions1.entries }
        .mapNotNull { (k, v) -> (v as? SlimInstruction1)?.let { k to it } }
        .associateBy { it.second.concrete.opcode }
}

fun disassemble(dyBuf: DyBuf, extensions: Set<Extension>, writer: Writer) = DismScope(extensions).run {
    val labeled = mutableSetOf<U24>()
    dyBuf.forEachIndexed { pos, it ->
        if ((it.value and 0xF00000 shr 20) in (0x1..0x7)) {
            labeled.add(arg(it))
        }
    }

    var writing = false
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
            writer.appendLine(dataStore(pos.toU24(), it))
        } else {
            writer.appendLine(disassemble(pos.toU24(), it))
        }
    }
    constants.forEach { (pos, name) ->
        writer.appendLine("$name = ${pos.value}")
    }
    writer.flush()
}

private fun DismScope.disassemble(position: U24, command: U24): String {
    val head = (command.value and 0xF00000 shr 20)
    if (head == 0xF) {
        val tail = (command.value and 0xFF0000 shr 16).toUByte()
        broad0[tail]?.let {
            return "${it.first} ; ${arg2(command)}"
        }
        broad1[tail]?.let {
            return "${it.first} ${arg2(command)}"
        }
        return dataStore(position, command)
    }
    slim1[head.toUByte()]?.let {
        return "${it.first} ${if (it.second.isConst) constant(arg(command)) else label(arg(command))}"
    }
    return dataStore(position, command)
}

private fun DismScope.dataStore(position: U24, value: U24): String {
    return "${label(position)} DS ${constant(value)}"
}

private fun arg(command: U24): U24 {
    return (command.value and 0xFFFFF).toU24()
}

private fun arg2(command: U24): U24 {
    return (command.value and 0xFFFF).toU24()
}

private fun label(pos: U24): String {
    return "l" + pos.value.toString(16).padStart(6, '0')
}

private fun DismScope.constant(pos: U24): String {
    if (pos in constants) {
        return constants[pos]!!
    }
    val name = "c" + pos.value.toString(16).padStart(6, '0')
    constants[pos] = name
    return name
}