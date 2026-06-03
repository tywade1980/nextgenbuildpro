package com.nextgenbuildpro.inference

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

data class HrmResult(
    val score: Float,
    val confidence: Float,
    val label: String
)

/**
 * Converted from tywade1980/hrm (pretrain.py + models/ directory).
 * Wraps a pre-trained HRM (Hierarchical Reasoning Model) ONNX checkpoint
 * for on-device inference. The HRM was trained on ARC-style grid puzzles
 * and produces a quality/confidence score for decision sequences.
 *
 * Place hrm_model.onnx in assets/ at build time, or download it via
 * ModelDownloaderService into getFilesDir() before calling load().
 */
@Singleton
class HrmInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "HrmInference"
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    fun isLoaded() = session != null

    fun load(modelFileName: String = "hrm_model.onnx"): Boolean {
        return try {
            val modelBytes: ByteArray = File(context.filesDir, modelFileName).let { f ->
                if (f.exists()) f.readBytes()
                else context.assets.open(modelFileName).use { it.readBytes() }
            }
            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setInterOpNumThreads(1)
            }
            session = env.createSession(modelBytes, opts)
            Log.i(TAG, "HRM model loaded")
            true
        } catch (e: Exception) {
            Log.w(TAG, "HRM model not available: ${e.message}")
            false
        }
    }

    fun infer(features: FloatArray): HrmResult {
        val sess = session ?: return HrmResult(0f, 0f, "model_not_loaded")
        return try {
            val shape = longArrayOf(1, features.size.toLong())
            val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(features), shape)
            val out = sess.run(mapOf(sess.inputNames.first() to tensor))
            val logits = (out[0].value as Array<*>)[0] as FloatArray
            val sm = softmax(logits)
            val maxIdx = sm.indices.maxByOrNull { sm[it] } ?: 0
            HrmResult(score = sm.getOrElse(1) { 0f }, confidence = sm[maxIdx], label = if (maxIdx == 1) "correct" else "incorrect")
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            HrmResult(0f, 0f, "inference_error")
        }
    }

    private fun softmax(v: FloatArray): FloatArray {
        val m = v.max()
        val e = v.map { Math.exp((it - m).toDouble()).toFloat() }.toFloatArray()
        val s = e.sum()
        return e.map { it / s }.toFloatArray()
    }
}
