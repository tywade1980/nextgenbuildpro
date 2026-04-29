package com.example.modeldownloader

import android.content.Context
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

/**
 * ModelDownloader handles downloading an ONNX model from a remote URL and
 * saving it into the app's private storage. It uses OkHttp to stream
 * the file to disk. Once downloaded, other applications on the device can
 * locate the model in `context.filesDir/models`.
 */
object ModelDownloader {

    /**
     * Downloads a model from the specified [provider] using the supplied
     * [modelName] identifier.  The [onComplete] callback is invoked on
     * success or failure.  Provider‑specific authentication headers are
     * added when available via [AuthManager].
     *
     * @param context a [Context] used to locate the app's files directory
     * @param provider the chosen provider from which to fetch the model
     * @param modelName the identifier of the model.  For Hugging Face and
     * GitHub this should be the repository or path as appropriate.
     * @param onComplete callback invoked when the download finishes.  Runs
     * on the OkHttp worker thread.
     */
    fun downloadModel(
        context: Context,
        provider: Provider,
        modelName: String,
        onComplete: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()
        when (provider) {
            Provider.OLLAMA -> {
                // For Ollama we call the local pull API.  The modelName
                // represents the model to pull, e.g. "phi3".  This endpoint
                // streams the model back.  See https://github.com/ollama/ollama for details.
                val requestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.get("application/json"),
                    "{\"name\":\"${'$'}modelName\"}"
                )
                val request = Request.Builder()
                    .url("http://127.0.0.1:11434/api/pull")
                    .post(requestBody)
                    .build()
                executeDownload(client, request, context, "${'$'}modelName.onnx", onComplete)
            }
            Provider.HUGGING_FACE -> {
                // Construct a Hugging Face download URL.  This assumes the
                // model lives at <modelName>/resolve/main/<modelName>.onnx.  If
                // different naming conventions are used, adapt accordingly.
                val fileName = if (modelName.endsWith(".onnx")) modelName else "${'$'}modelName.onnx"
                val url = "https://huggingface.co/${'$'}modelName/resolve/main/${'$'}fileName"
                val builder = Request.Builder().url(url)
                // Add Authorization header if a token exists
                AuthManager.getAccessToken(provider)?.let { token ->
                    builder.addHeader("Authorization", "Bearer ${'$'}token")
                }
                val request = builder.build()
                executeDownload(client, request, context, fileName, onComplete)
            }
            Provider.GITHUB -> {
                // For GitHub the modelName should be in the format
                // <owner>/<repo>/path/to/file.onnx.  We translate this into a
                // raw.githubusercontent.com URL.  Example: myorg/models/foo.onnx
                val url = "https://raw.githubusercontent.com/${'$'}modelName"
                val fileName = modelName.substringAfterLast('/')
                val builder = Request.Builder().url(url)
                AuthManager.getAccessToken(provider)?.let { token ->
                    // Use GitHub token as an Authorization header
                    builder.addHeader("Authorization", "token ${'$'}token")
                }
                val request = builder.build()
                executeDownload(client, request, context, fileName, onComplete)
            }
        }
    }

    /**
     * Executes the HTTP request and writes the response body to a file
     * within the app's private storage.  Shared implementation for all
     * providers.
     */
    private fun executeDownload(
        client: OkHttpClient,
        request: Request,
        context: Context,
        fileName: String,
        onComplete: (Boolean) -> Unit
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ModelDownloader", "Download failed", e)
                onComplete(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("ModelDownloader", "Unexpected HTTP response: ${'$'}{response.code}")
                    onComplete(false)
                    return
                }
                val body = response.body
                if (body == null) {
                    Log.e("ModelDownloader", "Empty response body")
                    onComplete(false)
                    return
                }
                try {
                    val dir = File(context.filesDir, "models")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val outFile = File(dir, fileName)
                    outFile.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    onComplete(true)
                } catch (e: Exception) {
                    Log.e("ModelDownloader", "Failed to write file", e)
                    onComplete(false)
                }
            }
        })
    }
}