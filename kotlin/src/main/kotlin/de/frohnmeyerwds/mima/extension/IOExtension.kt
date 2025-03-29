package de.frohnmeyerwds.mima.extension

object IOExtension : Extension {
    override val name: String get() = "io"
    override val dependencies: Set<String> get() = setOf("base", "indirect")
    override val instructions0: Map<String, Instruction0> get() = emptyMap()
    override val instructions1: Map<String, Instruction1> get() = mapOf(
        "IN" to BroadInstruction1(0xF3.toUByte()) {
            akku = io.read(barg(ir))
            true
        },
        "OUT" to BroadInstruction1(0xF4.toUByte()) {
            io.write(barg(ir), akku)
            true
        },
        "OUTS" to PseudoInstruction1("""
                JMP prepare
                
            arg DS 0
            end DS 0
            pos DS 0
            
            prepare:
                STV arg
                LDC 1
                ADD arg
                STV pos
                LDI arg
                ADD pos
                STV end
            
            print:
                LDV pos
                EQL end
                JMN _end
                LDI pos
                OUT a1
                LDC 1
                ADD pos
                STV pos
                JMP print
            _end:
                LDV arg
        """)
    )
}
