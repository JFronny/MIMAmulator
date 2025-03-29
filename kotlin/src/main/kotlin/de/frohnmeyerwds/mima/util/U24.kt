package de.frohnmeyerwds.mima.util

class U24(value: Int) : Comparable<U24> {
    val value = value and 0xFFFFFF

    override fun toString(): String = toString(value)
    fun toStringFlat(): String = toStringFlat(value)

    operator fun compareTo(other: Int): Int = toInt().compareTo(other)
    override operator fun compareTo(other: U24): Int = toInt().compareTo(other.toInt())

    fun toInt() = if (value and 0x800000 != 0) value or 0xFF000000.toInt() else value
    fun toUInt(): UInt = value.toUInt()

    infix fun shl(right: Int): U24 = U24(value shl right)
    infix fun shr(right: Int): U24 = U24(value shr right)
    infix fun and(right: U24): U24 = U24(value and right.value)
    infix fun or(right: U24): U24 = U24(value or right.value)
    infix fun xor(right: U24): U24 = U24(value xor right.value)
    fun inv() = U24(value.inv())

    override fun hashCode(): Int = value
    override fun equals(other: Any?): Boolean = other is U24 && value == other.value

    operator fun inc(): U24 = U24(value + 1)
    operator fun dec(): U24 = U24(value - 1)
    operator fun plus(i: Int): U24 = U24(value + i)
    operator fun plus(i: U24): U24 = U24(value + i.value)
    operator fun minus(i: Int): U24 = U24(value - i)
    operator fun minus(i: U24): U24 = U24(value - i.value)

    companion object {
        fun parse(value: String): U24 {
            val i = if (value.startsWith("0x")) value.substring(2).toInt(16)
                    else value.toInt()
            val res = U24(i)
            if (i != res.toInt()) throw NumberFormatException("Number out of representation range: $value")
            return res
        }

        fun tryParse(value: String): U24? = try {
            parse(value)
        } catch (e: NumberFormatException) {
            null
        }

        fun toString(value: Int): String = "0x${toStringFlat(value)}"
        fun toStringFlat(value: Int): String = value.toString(16).padStart(6, '0')
    }
}

fun UByte.toU24(): U24 = U24(this.toInt())
fun Int.toU24(): U24 = U24(this)

val ZERO = U24(0)
