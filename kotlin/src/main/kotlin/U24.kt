package de.frohnmeyer_wds

class U24 constructor(value: Int) {
    val value = value and 0xFFFFFF

    override fun toString(): String {
        return "0x${value.toString(16).padStart(6, '0')}"
    }

    operator fun inc(): U24 {
        return U24(value + 1)
    }

    operator fun dec(): U24 {
        return U24(value - 1)
    }

    operator fun compareTo(other: Int): Int {
        return value.compareTo(other)
    }

    operator fun compareTo(other: U24): Int {
        val thisValue = if (value and 0x800000 != 0) value or 0xFF000000.toInt() else value
        val otherValue = if (other.value and 0x800000 != 0) other.value or 0xFF000000.toInt() else other.value
        return thisValue.compareTo(otherValue)
    }

    fun toUInt(): UInt {
        return value.toUInt()
    }

    infix fun shl(right: Int): U24 {
        return U24(value shl right)
    }

    infix fun shr(right: Int): U24 {
        return U24(value shr right)
    }

    infix fun and(right: U24): U24 {
        return U24(value and right.value)
    }

    infix fun or(right: U24): U24 {
        return U24(value or right.value)
    }

    infix fun xor(right: U24): U24 {
        return U24(value xor right.value)
    }

    fun inv() = U24(value.inv())

    override fun hashCode(): Int {
        return value
    }

    override fun equals(other: Any?): Boolean {
        return other is U24 && value == other.value
    }

    operator fun plus(i: Int): U24 {
        return U24(value + i)
    }

    operator fun plus(i: U24): U24 {
        return U24(value + i.value)
    }

    companion object {
        fun parse(value: String): U24 {
            return if (value.startsWith("0x")) {
                U24(value.substring(2).toInt(16))
            } else {
                U24(value.toInt())
            }
        }

        fun tryParse(value: String): U24? {
            return try {
                parse(value)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}

fun UByte.toU24(): U24 {
    return U24(this.toInt())
}

fun Int.toU24(): U24 {
    return U24(this)
}