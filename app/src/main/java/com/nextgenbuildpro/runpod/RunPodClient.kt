package com.nextgenbuildpro.runpod

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class RunPodInfo(
    val id: String,
    val name: String,
    val desiredStatus: String,
    val costPerHr: Double
)

/**
 * Converted from manus-master-archive/skills/runpod-connector/scripts/runpod_connector.py.
 * Android client for the RunPod GraphQL API.
 */
@Singleton
class RunPodClient @Inject constructor() {

    private val TAG = "RunPodClient"
    private val ENDPOINT = "https://api.runpod.io/graphql"
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()
    private var apiKey: String = ""

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun configure(key: String) { apiKey = key }

    private suspend fun gql(query: String, vars: Map<String, Any> = emptyMap()): JSONObject = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw IllegalStateException("RunPod API key not configured")
        val payload = JSONObject().apply {
            put("query", query)
            if (vars.isNotEmpty()) put("variables", JSONObject(vars as Map<*, *>))
        }
        val req = Request.Builder()
            .url("$ENDPOINT?api_key=$apiKey")
            .post(payload.toString().toRequestBody(JSON_TYPE))
            .header("Content-Type", "application/json")
            .build()
        val resp = http.newCall(req).execute()
        val text = resp.body?.string() ?: throw IllegalStateException("Empty RunPod response")
        if (!resp.isSuccessful) throw IllegalStateException("RunPod error ${resp.code}: $text")
        JSONObject(text)
    }

    suspend fun listPods(): List<RunPodInfo> = runCatching {
        val arr = gql("{ myself { pods { id name desiredStatus costPerHr } } }")
            .getJSONObject("data").getJSONObject("myself").getJSONArray("pods")
        (0 until arr.length()).map { i ->
            arr.getJSONObject(i).let { p ->
                RunPodInfo(p.optString("id"), p.optString("name"), p.optString("desiredStatus"), p.optDouble("costPerHr", 0.0))
            }
        }
    }.getOrElse { e -> Log.e(TAG, "listPods", e); emptyList() }

    suspend fun startPod(podId: String, gpuCount: Int = 1): Boolean = runCatching {
        gql("mutation R(\$p:String!,\$g:Int!){podResume(input:{podId:\$p,gpuCount:\$g}){id}}",
            mapOf("p" to podId, "g" to gpuCount)); true
    }.getOrElse { e -> Log.e(TAG, "startPod", e); false }

    suspend fun stopPod(podId: String): Boolean = runCatching {
        gql("mutation R(\$p:String!){podStop(input:{podId:\$p}){id}}", mapOf("p" to podId)); true
    }.getOrElse { e -> Log.e(TAG, "stopPod", e); false }

    suspend fun terminatePod(podId: String): Boolean = runCatching {
        gql("mutation R(\$p:String!){podTerminate(input:{podId:\$p})}", mapOf("p" to podId)); true
    }.getOrElse { e -> Log.e(TAG, "terminatePod", e); false }
}
