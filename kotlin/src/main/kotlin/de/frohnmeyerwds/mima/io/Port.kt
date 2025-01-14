package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24

interface Port {
    fun read(): U24
    fun write(value: U24)
    val kind: U24
}