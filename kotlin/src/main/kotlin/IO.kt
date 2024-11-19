package de.frohnmeyer_wds

import java.io.Reader

class IO(private val input: Reader? = System.console()?.reader()) {
    private var introspectionResult: U24 = U24(1)

    fun read(port: U24): U24 = when (port) {
        U24(0) -> {
            val result = introspectionResult
            introspectionResult = U24(0)
            result
        }
        U24(1) -> input?.ready()?.let {
            if (it) U24(input.read())
            else null
        } ?: U24(0)
        else -> U24(0)
    }

    fun write(port: U24, value: U24): Unit = when (port) {
        U24(0) -> introspectionResult = when (value.value and 0xFF0000 shr 20) {
            0 -> when (value.value and 0xFFFF) {
                0 -> U24(2)
                1 -> U24(3)
                else -> U24(0)
            }
            else -> U24(0)
        }
        U24(1) -> print(value.value.toChar())
        else -> {}
    }
}