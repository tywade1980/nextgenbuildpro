package com.example.telephonyagent

import android.content.Context
import android.content.SharedPreferences

/**
 * PreferencesManager provides access to simple user preferences for the
 * Telephony Agent.  It stores data such as call screening thresholds
 * and other user configurable settings.  These values are not considered
 * sensitive and are stored in plain SharedPreferences.
 */
class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("telephony_agent_prefs", Context.MODE_PRIVATE)

    /**
     * Returns the current call screening threshold (0–100).  The default
     * value is 50 if no preference has been set.
     */
    fun getScreeningThreshold(): Int {
        return prefs.getInt(KEY_CALL_SCREENING_THRESHOLD, DEFAULT_THRESHOLD)
    }

    /**
     * Saves a new call screening threshold.  Values should be in the
     * range 0–100, where higher values make the filter more permissive.
     */
    fun setScreeningThreshold(value: Int) {
        prefs.edit().putInt(KEY_CALL_SCREENING_THRESHOLD, value).apply()
    }

    companion object {
        private const val KEY_CALL_SCREENING_THRESHOLD = "call_screening_threshold"
        private const val DEFAULT_THRESHOLD = 50
    }
}