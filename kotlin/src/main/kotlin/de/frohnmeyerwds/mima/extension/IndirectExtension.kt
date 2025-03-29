package de.frohnmeyerwds.mima.extension

object IndirectExtension : Extension {
    override val name: String get() = "indirect"
    override val dependencies: Set<String> get() = setOf("base")
    override val instructions0: Map<String, Instruction0> get() = emptyMap()
    override val instructions1: Map<String, Instruction1> get() = mapOf(
        "LDI" to PseudoInstruction1("""
                LDV il
                ADD a1
                STV cm
                JMP cm
            il: LDV 0
            cm: LDV 0
        """)
    )
}
