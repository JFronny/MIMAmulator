package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.frohnmeyerwds.mima.util.ZERO
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class SpeakerPort : Port {
    override val kind: U24 get() = U24(5)
    override fun read(): U24 = ZERO

    private val sampleRate = 16000f // 44100f

    private val line by lazy {
        val lit = AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            24,
            1,
            3,
            sampleRate,
            true
        ))) as SourceDataLine
        lit
    }

    private var started = false

    override fun write(value: U24) {
        val buffer = byteArrayOf(
            (value.value shr 16).toByte(),
            (value.value shr 8).toByte(),
            value.value.toByte()
        )
        if (!started) {
            line.open()
            line.start()
            started = true
        }
        line.write(buffer, 0, buffer.size)
    }

    override fun close() {
        line.drain()
        line.close()
    }
}

fun main() {
    // ffmpeg -i source.mp3 -f s24be -acodec pcm_s24be source.pcm
    // ffmpeg -i source.mp3 -f s24be -acodec pcm_s24be -ac 1 -ar 16000 source.pcm
    // ffmpeg -i source.mp3 -f s24be -acodec pcm_s24be -ac 1 -ar 44100 source.pcm

    val bufferSize = 1024 * 3

    AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        16000f,
        24,
        1,
        3,
        16000f,
        true
    ))).use {
        val audioLine = it as SourceDataLine

        val buffer = ByteArray(bufferSize)

        audioLine.open()
        audioLine.start()

        Path("source.pcm").inputStream().use {
            while (true) {
                val read = it.read(buffer)
                if (read == -1) {
                    break
                }
                audioLine.write(buffer, 0, read)
            }
        }

        audioLine.drain()
    }
}
