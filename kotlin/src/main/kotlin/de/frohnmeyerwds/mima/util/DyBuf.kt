package de.frohnmeyerwds.mima.util

class DyBuf {
    private var buffer = U24Array(0)
    private var size = 0

    private fun grow(newSize: Int) {
        if (newSize <= buffer.size) {
            return
        }
        val newBuffer = U24Array(newSize)
        buffer.copyInto(newBuffer)
        buffer = newBuffer
        size = newSize
    }

    operator fun set(index: Int, value: U24) {
        grow(index + 1)
        buffer[index] = value
    }
    operator fun set(index: U24, value: U24) = set(index.value, value)
    operator fun set(index: Int, value: DyBuf) {
        grow(index + value.size)
        value.buffer.copyInto(buffer, index)
    }
    operator fun set(index: U24, value: DyBuf) = set(index.value, value)
    operator fun get(index: Int): U24 = buffer[index]
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
            buffer[start + i] = U24((value[i * 3].toUByte().toInt() shl 16) or
                    (value[i * 3 + 1].toUByte().toInt() shl 8) or
                    (value[i * 3 + 2].toUByte().toInt()))
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

    fun copyOfRange(from: Int, to: Int): DyBuf {
        if (from < 0 || to > size || from > to) {
            throw IndexOutOfBoundsException("Invalid range: $from..$to")
        }
        val result = DyBuf()
        result.grow(to - from)
        for (i in from until to) {
            result.buffer[i - from] = buffer[i]
        }
        return result
    }

    override fun toString(): String = buildString {
        this@DyBuf.forEachIndexed { index, value ->
            if (index > 0) append(" ")
            append(value.toStringFlat())
        }
    }
}
