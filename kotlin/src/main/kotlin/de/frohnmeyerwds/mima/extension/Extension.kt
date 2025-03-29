package de.frohnmeyerwds.mima.extension

import de.frohnmeyerwds.mima.Mima
import de.frohnmeyerwds.mima.util.DyBuf
import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.toU24

data class InsertContext(
    val dyBuf: DyBuf,
    val position: U24,
    val dependencies: Set<Extension>,
)

fun interface MimaAction {
    operator fun Mima.invoke(): Boolean
}
data class Concrete(val opcode: UByte, val action: MimaAction)

inline fun MimaAction.invoke(mima: Mima) = mima.run { invoke() }

sealed interface Instruction {
    val concrete: Concrete?
}

sealed interface Instruction0 : Instruction {
    fun InsertContext.insert(): U24
}

sealed interface Instruction1 : Instruction {
    val isConst: Boolean get() = false
    fun InsertContext.insert(argument: U24): U24
}

interface Extension {
    val name: String
    val dependencies: Set<String> get() = emptySet()

    val instructions0: Map<String, Instruction0>
    val instructions1: Map<String, Instruction1>
}

inline fun barg(command: U24): U24 = (command.value and 0xFFFF).toU24()
inline fun arg(command: U24): U24 = (command.value and 0xFFFFF).toU24()

val EXTENSIONS = setOf(BaseExtension, IndirectExtension, IOExtension)
    .associateBy { it.name }

sealed interface Conflict {
    data class UnfulfilledDependency(val extension: Extension, val dependency: String) : Conflict {
        override fun toString(): String = "Extension ${extension.name} requires $dependency"
    }
    data class Duplicate(val extension: Extension, val name: String) : Conflict {
        override fun toString(): String = "Extension ${extension.name} has duplicate name"
    }
    data class OpcodeConflict(val extension: Extension, val opcode: UByte) : Conflict {
        override fun toString(): String = "Extension ${extension.name} has duplicate opcode $opcode"
    }
    data class InstructionConflict(val extension: Extension, val name: String) : Conflict {
        override fun toString(): String = "Extension ${extension.name} has duplicate instruction $name"
    }
}

fun Set<Extension>.findConflicts(): Set<Conflict> {
    val conflicts = mutableSetOf<Conflict>()

    val seenExtensions = mutableSetOf<String>()
    val seenInstructions = mutableSetOf<String>()
    val seenOpcodes = mutableSetOf<UByte>()
    for (extension in this) {
        if (!seenExtensions.add(extension.name)) conflicts.add(Conflict.Duplicate(extension, extension.name))

        for (dependency in extension.dependencies) {
            if (dependency !in seenExtensions) {
                // this may be a false positive, so this is filtered later
                conflicts.add(Conflict.UnfulfilledDependency(extension, dependency))
            }
        }

        for ((name, instruction) in extension.instructions0) {
            if (!seenInstructions.add(name)) conflicts.add(Conflict.InstructionConflict(extension, name))
            val opcode = instruction.concrete?.opcode ?: continue
            if (!seenOpcodes.add(opcode)) conflicts.add(Conflict.OpcodeConflict(extension, opcode))
        }
    }

    return conflicts.filter { it !is Conflict.UnfulfilledDependency || it.dependency !in seenExtensions }.toSet()
}
