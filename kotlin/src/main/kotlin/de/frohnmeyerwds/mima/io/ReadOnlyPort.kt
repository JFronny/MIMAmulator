package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import java.io.RandomAccessFile

class ReadOnlyPort(private val file: RandomAccessFile) : Port {
    override fun read(): U24 {
        val data = ByteArray(3)
        if (file.read(data) != 3) return ZERO
        return U24(
            (data[0].toInt() and 0xFF shl 16)
                    or (data[1].toInt() and 0xFF shl 8)
                    or (data[2].toInt() and 0xFF)
        )
    }

    override fun write(value: U24) {
        file.seek(file.filePointer + value.value.toLong())
    }

    override val kind: U24 get() = U24(4)

    override fun close() = file.close()
}