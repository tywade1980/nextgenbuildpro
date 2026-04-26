package com.caroline.ai

import android.content.Context
import com.caroline.ai.assistant.CarolineAssistant
import com.caroline.ai.calls.CarolineCallHandler
import com.caroline.ai.voice.CarolineVoiceEngine
import com.caroline.ai.calendar.CarolineCalendarService
import com.caroline.ai.contacts.CarolineContactsService
import com.caroline.ai.email.CarolineEmailService
import com.caroline.ai.llm.CarolineLLMService

/**
 * Lightweight service locator — avoids Dagger/Hilt dependency for cross-repo portability.
 */
object CarolineServiceLocator {

    lateinit var llmService: CarolineLLMService
        private set
    lateinit var voiceEngine: CarolineVoiceEngine
        private set
    lateinit var callHandler: CarolineCallHandler
        private set
    lateinit var assistant: CarolineAssistant
        private set
    lateinit var calendarService: CarolineCalendarService
        private set
    lateinit var contactsService: CarolineContactsService
        private set
    lateinit var emailService: CarolineEmailService
        private set

    suspend fun initialize(context: Context) {
        llmService = CarolineLLMService(context)
        voiceEngine = CarolineVoiceEngine(context, llmService)
        callHandler = CarolineCallHandler(context, llmService, voiceEngine)
        calendarService = CarolineCalendarService(context)
        contactsService = CarolineContactsService(context)
        emailService = CarolineEmailService(context)
        assistant = CarolineAssistant(
            context = context,
            llmService = llmService,
            voiceEngine = voiceEngine,
            calendarService = calendarService,
            contactsService = contactsService,
            emailService = emailService
        )
    }
}
