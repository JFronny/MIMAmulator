package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import java.io.InputStream
import java.io.OutputStream

class ConsolePort(private val `in`: InputStream, private val out: OutputStream) : Port {
    constructor() : this(System.`in`, System.out)

    override fun read(): U24 = if (`in`.available() > 0) U24(`in`.read()) else ZERO

    override fun write(value: U24) {
        if (value.value and 0xFFFF80 == 0) out.write(value.value)
        else value.value.toChar().toString().byteInputStream(Charsets.UTF_8).use { it.copyTo(out) }
    }

    override val kind: U24 get() = U24(3)

    override fun close() {
        `in`.close()
        out.close()
    }
}
