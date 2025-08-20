package com.nextgenbuildpro.receptionist.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.core.api.SecureCredentialStorage
import com.nextgenbuildpro.core.api.impl.SecureCredentialStorageImpl
import com.nextgenbuildpro.receptionist.service.CallHandlingService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Settings screen for the AI Receptionist
 * Allows customization of:
 * - Greeting message
 * - Voice model
 * - Call screening preferences
 * - API key for speech services
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIReceptionistSettingsScreen(navController: NavController) {
    val context = LocalContext.current

    // State for settings
    var ringCount by remember { mutableStateOf(3) }
    var customGreeting by remember { mutableStateOf("Hello, thank you for calling. This is the AI receptionist. How may I help you today?") }
    var selectedVoiceModel by remember { mutableStateOf("default") }
    var transcribeEnabled by remember { mutableStateOf(true) }

    // Call screening preferences
    var screenSpamCalls by remember { mutableStateOf(true) }
    var screenUnknownCalls by remember { mutableStateOf(false) }
    var screenAfterHoursCalls by remember { mutableStateOf(false) }
    var businessHoursStart by remember { mutableStateOf(9) } // 9 AM
    var businessHoursEnd by remember { mutableStateOf(17) } // 5 PM

    // API key
    var speechApiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }

    // Status
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Load current settings
    LaunchedEffect(Unit) {
        // Load settings from SharedPreferences
        val sharedPrefs = context.getSharedPreferences("ai_receptionist_settings", Context.MODE_PRIVATE)
        ringCount = sharedPrefs.getInt("ring_count", 3)
        customGreeting = sharedPrefs.getString("custom_greeting", customGreeting) ?: customGreeting
        selectedVoiceModel = sharedPrefs.getString("voice_model", selectedVoiceModel) ?: selectedVoiceModel
        transcribeEnabled = sharedPrefs.getBoolean("transcribe_enabled", true)

        screenSpamCalls = sharedPrefs.getBoolean("screen_spam_calls", true)
        screenUnknownCalls = sharedPrefs.getBoolean("screen_unknown_calls", false)
        screenAfterHoursCalls = sharedPrefs.getBoolean("screen_after_hours_calls", false)
        businessHoursStart = sharedPrefs.getInt("business_hours_start", 9)
        businessHoursEnd = sharedPrefs.getInt("business_hours_end", 17)

        // Load API key from secure storage
        val secureStorage = SecureCredentialStorageImpl()
        secureStorage.initialize(context)
        speechApiKey = secureStorage.getCredential(SecureCredentialStorage.KEY_SPEECH_API) ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Receptionist Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Save settings
                            isSaving = true

                            // Save to SharedPreferences
                            val sharedPrefs = context.getSharedPreferences("ai_receptionist_settings", Context.MODE_PRIVATE)
                            with(sharedPrefs.edit()) {
                                putInt("ring_count", ringCount)
                                putString("custom_greeting", customGreeting)
                                putString("voice_model", selectedVoiceModel)
                                putBoolean("transcribe_enabled", transcribeEnabled)

                                putBoolean("screen_spam_calls", screenSpamCalls)
                                putBoolean("screen_unknown_calls", screenUnknownCalls)
                                putBoolean("screen_after_hours_calls", screenAfterHoursCalls)
                                putInt("business_hours_start", businessHoursStart)
                                putInt("business_hours_end", businessHoursEnd)

                                apply()
                            }

                            // Save API key to secure storage
                            val secureStorage = SecureCredentialStorageImpl()
                            secureStorage.initialize(context)
                            secureStorage.storeCredential(SecureCredentialStorage.KEY_SPEECH_API, speechApiKey)

                            isSaving = false
                            showSuccessMessage = true

                            // Hide success message after a delay
                            kotlinx.coroutines.MainScope().launch {
                                kotlinx.coroutines.delay(2000)
                                showSuccessMessage = false
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Call Answering Settings
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Call Answering",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ring count
                        Text("Rings before answering")
                        Slider(
                            value = ringCount.toFloat(),
                            onValueChange = { ringCount = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("$ringCount rings")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom greeting
                        OutlinedTextField(
                            value = customGreeting,
                            onValueChange = { customGreeting = it },
                            label = { Text("Greeting Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Voice model selection
                        Text("Voice Model")
                        var voiceModelExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = voiceModelExpanded,
                            onExpandedChange = { voiceModelExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedVoiceModel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceModelExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = voiceModelExpanded,
                                onDismissRequest = { voiceModelExpanded = false }
                            ) {
                                listOf("default", "female", "male", "british", "australian").forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            selectedVoiceModel = model
                                            voiceModelExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Transcription toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transcribe Calls",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = transcribeEnabled,
                                onCheckedChange = { transcribeEnabled = it }
                            )
                        }
                    }
                }

                // Call Screening Settings
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Call Screening",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Screen spam calls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Block Spam Calls",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = screenSpamCalls,
                                onCheckedChange = { screenSpamCalls = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Screen unknown calls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Screen Unknown Callers",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = screenUnknownCalls,
                                onCheckedChange = { screenUnknownCalls = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Screen after-hours calls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Screen After-Hours Calls",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = screenAfterHoursCalls,
                                onCheckedChange = { screenAfterHoursCalls = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Business hours
                        Text("Business Hours")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start time
                            OutlinedTextField(
                                value = businessHoursStart.toString(),
                                onValueChange = { 
                                    val hours = it.toIntOrNull() ?: 9
                                    if (hours in 0..23) {
                                        businessHoursStart = hours
                                    }
                                },
                                label = { Text("Start") },
                                modifier = Modifier.weight(1f)
                            )

                            // End time
                            OutlinedTextField(
                                value = businessHoursEnd.toString(),
                                onValueChange = { 
                                    val hours = it.toIntOrNull() ?: 17
                                    if (hours in 0..23) {
                                        businessHoursEnd = hours
                                    }
                                },
                                label = { Text("End") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Hours in 24-hour format (0-23)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // API Settings
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Speech API Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // API key
                        OutlinedTextField(
                            value = speechApiKey,
                            onValueChange = { speechApiKey = it },
                            label = { Text("Speech API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showApiKey) "Hide API Key" else "Show API Key"
                                    )
                                }
                            }
                        )

                        Text(
                            text = "This API key is used for speech-to-text and text-to-speech services",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Spacer at the bottom for better scrolling
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Success message
            if (showSuccessMessage) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Settings saved successfully",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Loading indicator
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
