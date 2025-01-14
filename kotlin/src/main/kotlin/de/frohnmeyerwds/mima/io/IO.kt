package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO

class IO(ports: List<Port>) {
    private val ports = listOf(IntrospectionPort(ports)) + ports

    fun read(port: U24): U24 = ports.getOrNull(port.value)?.read() ?: ZERO

    fun write(port: U24, value: U24): Unit = ports.getOrNull(port.value)?.write(value) ?: Unit
}
