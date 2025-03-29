package de.frohnmeyerwds.mima

import de.frohnmeyerwds.mima.extension.*
import de.frohnmeyerwds.mima.io.IO
import de.frohnmeyerwds.mima.io.Port
import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO

object UnknownInstructionAction : MimaAction {
    override fun Mima.invoke(): Boolean {
        println("Error: Unknown command: $ir (address ${iar - 1})")
        return false
    }
}

class WideWrapperAction(private val actions: Array<MimaAction>) : MimaAction {
    override fun Mima.invoke(): Boolean = actions[ir.value and 0xF0000 shr 16].invoke(this)
}

class Mima(val memory: DyBuf, extensions: Set<Extension>, ports: List<Port>, start: U24) {
    var iar = start
    var ir: U24 = ZERO
    var akku = ZERO
    var io = IO(ports)

    private val actions: Array<MimaAction> = arrayOfNulls<Any?>(16)
        .map { UnknownInstructionAction }
        .toTypedArray()

    init {
        val broad = extensions.flatMap { it.instructions1.entries + it.instructions0.entries }
            .mapNotNull { (k, v) -> (v as? BroadInstruction)?.let { k to it } }
            .associateBy { it.second.concrete.opcode }
        val slim = extensions.flatMap { it.instructions1.entries + it.instructions0.entries }
            .mapNotNull { (k, v) -> (v as? SlimInstruction)?.let { k to it } }
            .associateBy { it.second.concrete.opcode }

        val wideActions = actions.copyOf()
        for ((code, t) in broad) wideActions[code.toInt() and 0xF] = t.second.concrete.action
        for ((code, t) in slim) actions[code.toInt()] = t.second.concrete.action
        actions[0xF] = WideWrapperAction(wideActions)
    }

    fun executeSingle(): Boolean {
        ir = memory[iar++]
        return actions[ir.value and 0xF00000 shr 20].invoke(this)
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
