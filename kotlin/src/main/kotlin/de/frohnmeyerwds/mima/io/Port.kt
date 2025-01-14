package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import java.io.Closeable

interface Port : Closeable {
    fun read(): U24
    fun write(value: U24)
    val kind: U24
    override fun close() {}
}