package de.frohnmeyer_wds

import kotlin.math.max
import kotlin.math.min

class DyBuf {
    private val maxSize = 0x100000

    private var buffer = U24Array(0)
    var size = 0
        private set

    private fun grow(newSize: Int) {
        if (newSize <= size) {
            return
        }
        if (newSize > maxSize) throw IndexOutOfBoundsException("Buffer overflow")
        if (newSize > buffer.size) {
            val newBuffer = U24Array(min(maxSize, max(buffer.size * 2, newSize)))
            buffer.copyInto(newBuffer)
            buffer = newBuffer
        }
        size = newSize
    }

    operator fun set(index: Int, value: U24) {
        grow(index + 1)
        buffer[index] = value
    }
    operator fun set(index: U24, value: U24) = set(index.value, value)
    operator fun get(index: Int): U24 {
        grow(index + 1)
        return buffer[index]
    }
    operator fun get(index: U24) = get(index.value)

    operator fun plusAssign(value: U24) {
        grow(size + 1)
        buffer[size++] = value
    }

    operator fun plusAssign(value: ByteArray) {
        if (value.size % 3 != 0) {
            throw IllegalArgumentException("Invalid byte array size: ${value.size}")
        }
        val start = size
        grow(size + value.size / 3)
        for (i in 0 until value.size / 3) {
            buffer[start + i] = U24((value[i * 3].toInt() and 0xFF shl 16) or
                    (value[i * 3 + 1].toInt() and 0xFF shl 8) or
                    (value[i * 3 + 2].toInt() and 0xFF))
        }
    }

    fun toByteArray(): ByteArray {
        val result = ByteArray(size * 3)
        for (i in 0 until size) {
            val v = buffer[i].value
            result[i * 3] = (v shr 16).toByte()
            result[i * 3 + 1] = (v shr 8).toByte()
            result[i * 3 + 2] = v.toByte()
        }
        return result
    }

    fun forEach(function: (U24) -> Unit) {
        for (i in 0 until size) {
            function(buffer[i])
        }
    }

    fun forEachIndexed(function: (Int, U24) -> Unit) {
        for (i in 0 until size) {
            function(i, buffer[i])
        }
    }
}