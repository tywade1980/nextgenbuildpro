package com.nextgenbuildpro.crm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CallLog
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.crm.data.model.CallRecord
import com.nextgenbuildpro.crm.data.repository.LeadRepository
import com.nextgenbuildpro.crm.data.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Service to monitor system call logs and automatically log calls with clients
 */
class CallMonitorService : Service() {
    private val TAG = "CallMonitorService"
    private lateinit var leadRepository: LeadRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var callLogObserver: CallLogObserver
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // For call transcription (simulated in this implementation)
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CallMonitorService created")

        // Initialize repositories
        leadRepository = LeadRepository()
        messageRepository = MessageRepository()

        // Initialize text-to-speech for simulating transcription
        initializeTextToSpeech()

        // Initialize call log observer but don't register it yet
        callLogObserver = CallLogObserver(Handler(Looper.getMainLooper()))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "CallMonitorService started")

        // Check if this is a manual check request
        if (intent?.action == ACTION_CHECK_CALLS) {
            serviceScope.launch {
                Log.d(TAG, "Manually checking for new calls")
                callLogObserver.processNewCalls()

                // Stop the service after processing
                stopSelf()
            }
        }

        // Don't restart automatically - trigger-based approach
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CallMonitorService destroyed")
        contentResolver.unregisterContentObserver(callLogObserver)

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    /**
     * Initialize text-to-speech engine
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isTtsReady = true
                    textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {}

                        override fun onError(utteranceId: String?) {}
                    })
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    /**
     * Content observer to detect new call log entries
     */
    inner class CallLogObserver(handler: Handler) : ContentObserver(handler) {

        private var lastCheckedTimestamp = System.currentTimeMillis()

        fun getLastCheckedTimestamp(): Long {
            return lastCheckedTimestamp
        }

        fun setLastCheckedTimestamp(timestamp: Long) {
            lastCheckedTimestamp = timestamp
        }

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "Call log changed: $uri")

            // Process in background
            serviceScope.launch {
                processNewCalls()
            }
        }

        /**
         * Process new calls added to the call log
         */
        suspend fun processNewCalls() {
            val currentTime = System.currentTimeMillis()
            val selection = "${CallLog.Calls.DATE} > ?"
            val selectionArgs = arrayOf(lastCheckedTimestamp.toString())
            lastCheckedTimestamp = currentTime

            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                ),
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val idColumn = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                    val numberColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    val dateColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                    val durationColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                    val typeColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)

                    val id = cursor.getLong(idColumn)
                    val number = cursor.getString(numberColumn)
                    val date = cursor.getLong(dateColumn)
                    val duration = cursor.getInt(durationColumn)
                    val type = cursor.getInt(typeColumn)

                    processCall(id, number, date, duration, type)
                }
            }
        }

        /**
         * Process a single call
         */
        private suspend fun processCall(id: Long, number: String, date: Long, duration: Int, type: Int) {
            try {
                Log.d(TAG, "Processing call: $number, duration: $duration seconds")

                // Find matching lead
                val leads = leadRepository.getAll()
                val matchingLead = leads.find { it.phone.replace("[^0-9]".toRegex(), "") == number.replace("[^0-9]".toRegex(), "") }

                if (matchingLead != null) {
                    Log.d(TAG, "Found matching lead: ${matchingLead.name}")

                    // Generate a simulated transcription
                    val transcription = generateTranscription(matchingLead.name, duration, type)

                    // Create call record
                    val callType = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        else -> "Unknown"
                    }

                    CallRecord(
                        id = UUID.randomUUID().toString(),
                        leadId = matchingLead.id,
                        leadName = matchingLead.name,
                        phoneNumber = number,
                        timestamp = DateUtils.formatTimestamp(date),
                        duration = duration,
                        notes = transcription,
                        type = callType
                    )

                    // Add to lead's notes
                    val noteText = "Call ($callType): $duration seconds\n$transcription"
                    leadRepository.addLeadNote(matchingLead.id, noteText)

                    Log.d(TAG, "Call record created and note added to lead")
                } else {
                    Log.d(TAG, "No matching lead found for number: $number")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing call", e)
            }
        }

        /**
         * Generate a simulated transcription for the call
         * In a real app, this would use a speech-to-text service
         */
        private fun generateTranscription(leadName: String, duration: Int, callType: Int): String {
            // For demonstration purposes, we'll generate a simulated transcription
            if (duration < 10) {
                return "Call was too short for transcription"
            }

            val transcriptionBuilder = StringBuilder()

            if (callType == CallLog.Calls.INCOMING_TYPE) {
                transcriptionBuilder.append("$leadName: Hello, I'm calling about my project.\n")
                transcriptionBuilder.append("You: Hi $leadName, thanks for calling. How can I help you today?\n")

                if (duration > 30) {
                    transcriptionBuilder.append("$leadName: I was wondering about the timeline for completion.\n")
                    transcriptionBuilder.append("You: We're currently on schedule. The next milestone is scheduled for next week.\n")
                }

                if (duration > 60) {
                    transcriptionBuilder.append("$leadName: Great, and what about the materials we discussed?\n")
                    transcriptionBuilder.append("You: I've ordered them and they should arrive in 3-4 business days.\n")
                    transcriptionBuilder.append("$leadName: Perfect, thank you for the update.\n")
                    transcriptionBuilder.append("You: You're welcome. I'll keep you posted on any changes.\n")
                }
            } else {
                transcriptionBuilder.append("You: Hello $leadName, I'm calling to update you on your project.\n")

                if (duration > 30) {
                    transcriptionBuilder.append("$leadName: Hi, thanks for calling. What's the update?\n")
                    transcriptionBuilder.append("You: We've made good progress. The framing is complete and we're moving to the next phase.\n")
                }

                if (duration > 60) {
                    transcriptionBuilder.append("$leadName: That sounds great. Any issues I should know about?\n")
                    transcriptionBuilder.append("You: Nothing major. We had a small delay with a material delivery but we're still on schedule.\n")
                    transcriptionBuilder.append("$leadName: Good to hear. When do you think you'll be finished?\n")
                    transcriptionBuilder.append("You: We're still on track for completion by the end of the month.\n")
                }
            }

            return transcriptionBuilder.toString()
        }
    }

    companion object {
        const val ACTION_CHECK_CALLS = "com.nextgenbuildpro.action.CHECK_CALLS"

        /**
         * Start the call monitor service
         */
        fun startService(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java)
            context.startService(intent)
        }

        /**
         * Check for new calls (trigger-based approach)
         */
        fun checkCalls(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java).apply {
                action = ACTION_CHECK_CALLS
            }
            context.startService(intent)
        }

        /**
         * Stop the call monitor service
         */
        fun stopService(context: Context) {
            val intent = Intent(context, CallMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
