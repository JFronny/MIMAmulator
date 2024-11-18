package de.frohnmeyer_wds

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

    fun copyInto(target: U24Array) {
        value.copyInto(target.value)
    }

    fun copyOf(size: Int) = U24Array(value.copyOf(size))
}