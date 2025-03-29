package de.frohnmeyerwds.mima.extension

import de.frohnmeyerwds.mima.assemble
import de.frohnmeyerwds.mima.util.ONE
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.toU24
import java.io.StringReader

sealed class SlimInstruction(opcode: UByte, action: MimaAction) : Instruction {
    final override val concrete = Concrete(opcode, action)
    init {
        if (concrete.opcode > 0xF.toUByte()) {
            throw IllegalArgumentException("Opcode must be in range 0x0 to 0xE")
        }
    }
}

class SlimInstruction1(opcode: UByte, override val isConst: Boolean = false, action: MimaAction) : SlimInstruction(opcode, action), Instruction1 {
    override fun InsertContext.insert(argument: U24): U24 {
        if (argument.toUInt() >= 1u shl 20) throw IllegalArgumentException("Argument must be in range 0 to 0xFFFFF")
        dyBuf[position] = (concrete.opcode.toU24() shl 20) or argument
        return ONE
    }
}

sealed class BroadInstruction(opcode: UByte, action: MimaAction) : Instruction {
    final override val concrete = Concrete(opcode, action)
    init {
        if (opcode < 0xF0.toUByte()) {
            throw IllegalArgumentException("Opcode must be in range 0xF0 to 0xFF")
        }
    }
}

class BroadInstruction0(opcode: UByte, action: MimaAction) : BroadInstruction(opcode, action), Instruction0 {
    override fun InsertContext.insert(): U24 {
        dyBuf[position] = concrete.opcode.toU24() shl 16
        return ONE
    }
}

class BroadInstruction1(opcode: UByte, action: MimaAction) : BroadInstruction(opcode, action), Instruction1 {
    override fun InsertContext.insert(argument: U24): U24 {
        if (argument.toUInt() >= 1u shl 16) throw IllegalArgumentException("Argument must be in range 0 to 0xFFFF")
        dyBuf[position] = (concrete.opcode.toU24() shl 16) or argument
        return ONE
    }
}

sealed class PseudoInstruction(code: String) : Instruction {
    override val concrete: Concrete? get() = null
    final val code = code.trimIndent()
}

class PseudoInstruction0(code: String) : PseudoInstruction(code), Instruction0 {
    override fun InsertContext.insert(): U24 {
        val ibuf = assemble(StringReader(code), dependencies, position)
        dyBuf[position] = ibuf.copyOfRange(position.value, ibuf.size)
        return (ibuf.size - position.toInt()).toU24()
    }
}

class PseudoInstruction1(code: String) : PseudoInstruction(code), Instruction1 {
    override fun InsertContext.insert(argument: U24): U24 {
        val ibuf = assemble(StringReader(code), dependencies, position, mapOf(
            "a1" to argument
        ))
        dyBuf[position] = ibuf.copyOfRange(position.value, ibuf.size)
        return (ibuf.size - position.toInt()).toU24()
    }
}
