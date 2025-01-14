package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO

class IntrospectionPort(private val other: List<Port>) : Port {
    private var introspectionResult: U24 = U24(other.size)

    override fun read(): U24 {
        val result = introspectionResult
        introspectionResult = ZERO
        return result
    }

    override fun write(value: U24) {
        introspectionResult = when(value.value and 0xFF0000 shr 20) {
            0 -> other.getOrNull(value.value and 0xFFFF)?.kind ?: ZERO
            else -> ZERO
        }
    }

    override val kind: U24 get() = U24(2)
}