package com.nextgenbuildpro.hermes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nextgenbuildpro.orchestrator.WadeGlobalState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists and loads WadeGlobalState to/from the app's files directory.
 * Mirrors the JSON read/write logic from wade-global-state/hermes.py.
 */
@Singleton
class WgsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val wgsFile: File get() = File(context.filesDir, "wade_global_state.json")

    fun load(): WadeGlobalState {
        return if (wgsFile.exists()) {
            try {
                gson.fromJson(wgsFile.readText(), WadeGlobalState::class.java)
            } catch (e: Exception) {
                WadeGlobalState()
            }
        } else {
            WadeGlobalState()
        }
    }

    fun save(state: WadeGlobalState) {
        wgsFile.writeText(gson.toJson(state))
    }

    fun update(block: WadeGlobalState.() -> Unit): WadeGlobalState {
        val state = load()
        state.block()
        save(state)
        return state
    }
}
