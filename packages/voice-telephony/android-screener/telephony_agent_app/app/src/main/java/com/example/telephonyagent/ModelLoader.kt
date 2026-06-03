package com.example.telephonyagent

import android.util.Log
import com.microsoft.onnxruntime.OrtEnvironment
import com.microsoft.onnxruntime.OrtSession
import java.io.File

/**
 * ModelLoader encapsulates the process of loading an ONNX model from
 * arbitrary file paths. It supports both assets bundled with the APK
 * and external files provided by the user. The resulting OrtSession
 * can be passed to AiProcessor for inference.
 */
object ModelLoader {
    private var environment: OrtEnvironment? = null

    init {
        try {
            environment = OrtEnvironment.getEnvironment()
        } catch (e: Exception) {
            Log.e("ModelLoader", "Failed to create OrtEnvironment", e)
        }
    }

    /**
     * Loads a model from the given absolute or relative path. When loading
     * from assets, use the asset manager to open the stream instead.
     */
    fun loadModelFromFile(path: String): OrtSession? {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.e("ModelLoader", "Model file not found: $path")
                return null
            }
            val bytes = file.readBytes()
            val env = environment ?: return null
            env.createSession(bytes)
        } catch (e: Exception) {
            Log.e("ModelLoader", "Failed to load model from $path", e)
            null
        }
    }
}