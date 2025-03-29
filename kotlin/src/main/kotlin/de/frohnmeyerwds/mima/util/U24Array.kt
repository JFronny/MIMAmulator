package de.frohnmeyerwds.mima.util

class U24Array constructor(val value: IntArray) {
    constructor(size: Int) : this(IntArray(size))
    constructor(size: U24) : this(size.value)

    init {
        value.forEachIndexed { index, i ->
            if (i !in 0..0xFFFFFF) {
                throw IllegalArgumentException("Invalid value at index $index: $i")
            }
        }
    }

    operator fun get(index: Int): U24 = U24(value[index])
    operator fun get(index: U24): U24 = get(index.value)
    operator fun set(index: Int, value: U24) {
        this.value[index] = value.value
    }

    operator fun set(index: U24, value: U24) {
        this.value[index.value] = value.value
    }

    val size get() = value.size

    fun copyInto(target: U24Array, destinationIndex: Int = 0, startIndex: Int = 0, endIndex: Int = size) {
        value.copyInto(target.value, destinationIndex, startIndex, endIndex)
    }

    fun copyOf(size: Int) = U24Array(value.copyOf(size))

    override fun toString(): String {
        return value.joinToString(prefix = "[", postfix = "]") { U24.toString(it) }
    }
}