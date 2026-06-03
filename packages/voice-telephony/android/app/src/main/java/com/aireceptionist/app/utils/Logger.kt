package com.aireceptionist.app.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced logging utility for the AI Receptionist app
 */
object Logger {
    
    private var context: Context? = null
    private var isInitialized = false
    private var logToFile = true
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    fun init(context: Context) {
        this.context = context
        if (logToFile) {
            initializeLogFile()
        }
        isInitialized = true
        i("Logger", "Logger initialized")
    }
    
    private fun initializeLogFile() {
        try {
            val logsDir = File(context?.filesDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            logFile = File(logsDir, "ai_receptionist_$today.log")
            
            if (!logFile?.exists()!!) {
                logFile?.createNewFile()
            }
        } catch (e: Exception) {
            Log.e("Logger", "Failed to initialize log file", e)
            logToFile = false
        }
    }
    
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        Log.v(tag, message, throwable)
        writeToFile("V", tag, message, throwable)
    }
    
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        writeToFile("D", tag, message, throwable)
    }
    
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Log.i(tag, message, throwable)
        writeToFile("I", tag, message, throwable)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        writeToFile("W", tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        writeToFile("E", tag, message, throwable)
    }
    
    private fun writeToFile(level: String, tag: String, message: String, throwable: Throwable?) {
        if (!logToFile || logFile == null || !isInitialized) return
        
        try {
            val timestamp = dateFormat.format(Date())
            val logEntry = StringBuilder()
                .append(timestamp)
                .append(" ")
                .append(level)
                .append("/")
                .append(tag)
                .append(": ")
                .append(message)
            
            throwable?.let {
                logEntry.append("\n").append(Log.getStackTraceString(it))
            }
            
            logEntry.append("\n")
            
            FileWriter(logFile, true).use { writer ->
                writer.write(logEntry.toString())
                writer.flush()
            }
        } catch (e: Exception) {
            Log.e("Logger", "Failed to write to log file", e)
        }
    }
    
    fun getLogFile(): File? = logFile
    
    fun clearLogs() {
        try {
            logFile?.writeText("")
            i("Logger", "Log file cleared")
        } catch (e: Exception) {
            e("Logger", "Failed to clear log file", e)
        }
    }
    
    fun setLogToFile(enabled: Boolean) {
        logToFile = enabled
        if (enabled && logFile == null && isInitialized) {
            initializeLogFile()
        }
    }
}