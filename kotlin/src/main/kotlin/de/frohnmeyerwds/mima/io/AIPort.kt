package de.frohnmeyerwds.mima.io

import de.frohnmeyerwds.mima.util.U24
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaIterator
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import de.kherud.llama.args.LogFormat
import de.kherud.llama.args.MiroStat
import java.nio.file.Path

class AIPort(private val source: Path) : Port {
    private var model: LlamaModel? = null

    private fun getModel(): LlamaModel {
        if (model == null) {
            LlamaModel.setLogger(LogFormat.TEXT) { level, text ->
                // Do nothing
            }
            model = LlamaModel(ModelParameters()
                .setModelFilePath(source.toAbsolutePath().toString())
                .setNGpuLayers(43)
            )
        }
        return model!!
    }

    private val prompt = StringBuffer()
    private var iterator: LlamaIterator? = null
    private var outputIterator: CharIterator? = null
    private var needsWrite = false

    override fun read(): U24 {
        if (outputIterator != null) return advanceOutputIterator()
        if (needsWrite) return U24(0)
        val iter = iterator ?: getModel().generate(
            InferenceParameters(prompt.toString())
                .setTemperature(0.7f)
                .setPenalizeNl(true)
                .setMiroStat(MiroStat.V2)
                .setStopStrings("<|eot_id|>")
        ).iterator()
        iterator = iter
        if (!iter.hasNext()) return U24(0)
        outputIterator = iter.next().text.iterator()
        if (!outputIterator!!.hasNext()) {
            outputIterator = null
            needsWrite = true
            return U24(0)
        }
        return advanceOutputIterator()
    }

    private fun advanceOutputIterator(): U24 {
        val ch = outputIterator!!.next()
        if (outputIterator?.hasNext() != true) outputIterator = null
        prompt.append(ch)
        return U24(ch.code)
    }

    override fun write(value: U24) {
        needsWrite = false
        iterator?.cancel()
        iterator = null
        outputIterator = null
        prompt.append(value.value.toChar())
    }

    override val kind: U24 get() = U24(1)

    override fun close() {
        model?.close()
    }
}
