package com.nextgenbuildpro.apps

import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.util.UUID

/**
 * DialerApp
 * 
 * NextGen AI-powered dialer application with intelligent contact suggestions,
 * smart dialing, and construction industry-specific features.
 */
class DialerApp(private val context: Context) : NextGenService {
    
    override val serviceName: String = "DialerApp"
    
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()
    
    private val _recentCalls = MutableStateFlow<List<CallRecord>>(emptyList())
    val recentCalls: StateFlow<List<CallRecord>> = _recentCalls.asStateFlow()
    
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()
    
    private val _smartSuggestions = MutableStateFlow<List<SmartContact>>(emptyList())
    val smartSuggestions: StateFlow<List<SmartContact>> = _smartSuggestions.asStateFlow()
    
    private val mutex = Mutex()
    private val callHistory = mutableListOf<CallRecord>()
    private val contactManager = ContactManager()
    private val smartDialer = SmartDialer()
    private val constructionFeatures = ConstructionDialerFeatures()
    
    // Configuration
    private val config = DialerConfig(
        enableSmartSuggestions = true,
        enableConstructionMode = true,
        enableVoiceDialing = true,
        enableCallAnalytics = true,
        autoCompleteContacts = true,
        enableEmergencyContacts = true
    )
    
    override suspend fun start(): Result<Unit> = try {
        mutex.withLock {
            Log.i("DialerApp", "Starting Dialer App...")
            
            // Initialize components
            contactManager.initialize(context)
            smartDialer.initialize()
            constructionFeatures.initialize()
            
            // Load data
            loadContacts()
            loadRecentCalls()
            generateSmartSuggestions()
            
            _isRunning.value = true
            Log.i("DialerApp", "Dialer App started successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("DialerApp", "Failed to start Dialer App", e)
        Result.failure(e)
    }
    
    override suspend fun stop(): Result<Unit> = try {
        mutex.withLock {
            Log.i("DialerApp", "Stopping Dialer App...")
            
            // Save current state
            saveCallHistory()
            
            // Shutdown components
            contactManager.shutdown()
            smartDialer.shutdown()
            constructionFeatures.shutdown()
            
            _isRunning.value = false
            Log.i("DialerApp", "Dialer App stopped successfully")
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("DialerApp", "Error stopping Dialer App", e)
        Result.failure(e)
    }
    
    override suspend fun restart(): Result<Unit> {
        stop()
        return start()
    }
    
    override suspend fun getHealthStatus(): ServiceHealth {
        return ServiceHealth(
            isHealthy = _isRunning.value,
            lastCheckTime = LocalDateTime.now(),
            issues = if (_isRunning.value) emptyList() else listOf("Service not running"),
            metrics = mapOf(
                "contacts_loaded" to _contacts.value.size.toDouble(),
                "recent_calls" to _recentCalls.value.size.toDouble(),
                "smart_suggestions" to _smartSuggestions.value.size.toDouble()
            )
        )
    }
    
    // === DIALING METHODS ===
    
    suspend fun dialNumber(number: String): Result<Unit> = try {
        Log.d("DialerApp", "Dialing number: $number")
        
        // Validate number
        val validatedNumber = validatePhoneNumber(number)
        if (validatedNumber == null) {
            return Result.failure(IllegalArgumentException("Invalid phone number"))
        }
        
        // Pre-call analysis
        val callContext = smartDialer.analyzeCallContext(validatedNumber)
        
        // Record call attempt
        recordCallAttempt(validatedNumber, callContext)
        
        // Initiate call
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$validatedNumber")
        }
        
        context.startActivity(intent)
        
        // Update suggestions based on call
        updateSmartSuggestions(validatedNumber)
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("DialerApp", "Error dialing number: $number", e)
        Result.failure(e)
    }
    
    suspend fun addDigit(digit: String) {
        val currentNumber = _dialedNumber.value
        val newNumber = currentNumber + digit
        _dialedNumber.value = newNumber
        
        // Generate smart suggestions as user types
        if (config.autoCompleteContacts) {
            generateAutoCompleteSuggestions(newNumber)
        }
    }
    
    suspend fun removeLastDigit() {
        val currentNumber = _dialedNumber.value
        if (currentNumber.isNotEmpty()) {
            _dialedNumber.value = currentNumber.dropLast(1)
            
            // Update suggestions
            if (config.autoCompleteContacts) {
                generateAutoCompleteSuggestions(_dialedNumber.value)
            }
        }
    }
    
    suspend fun clearNumber() {
        _dialedNumber.value = ""
        _smartSuggestions.value = emptyList()
    }
    
    suspend fun dialContact(contact: Contact): Result<Unit> {
        return dialNumber(contact.phoneNumber)
    }
    
    suspend fun dialFromHistory(callRecord: CallRecord): Result<Unit> {
        return dialNumber(callRecord.phoneNumber)
    }
    
    // === SMART FEATURES ===
    
    suspend fun searchContacts(query: String): List<Contact> {
        return try {
            contactManager.searchContacts(query)
        } catch (e: Exception) {
            Log.e("DialerApp", "Error searching contacts", e)
            emptyList()
        }
    }
    
    suspend fun getConstructionContacts(): List<Contact> {
        return try {
            constructionFeatures.getConstructionSpecificContacts()
        } catch (e: Exception) {
            Log.e("DialerApp", "Error getting construction contacts", e)
            emptyList()
        }
    }
    
    suspend fun getEmergencyContacts(): List<Contact> {
        return try {
            constructionFeatures.getEmergencyContacts()
        } catch (e: Exception) {
            Log.e("DialerApp", "Error getting emergency contacts", e)
            emptyList()
        }
    }
    
    suspend fun enableVoiceDialing(): Result<Unit> = try {
        Log.d("DialerApp", "Enabling voice dialing...")
        // Voice dialing implementation would go here
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("DialerApp", "Error enabling voice dialing", e)
        Result.failure(e)
    }
    
    // === PRIVATE METHODS ===
    
    private fun validatePhoneNumber(number: String): String? {
        // Remove non-numeric characters except +
        val cleaned = number.replace(Regex("[^\\d+]"), "")
        
        return when {
            cleaned.isEmpty() -> null
            cleaned.length < 10 -> null
            cleaned.startsWith("+") && cleaned.length > 10 -> cleaned
            cleaned.length >= 10 -> cleaned
            else -> null
        }
    }
    
    private suspend fun loadContacts() {
        try {
            val loadedContacts = contactManager.loadAllContacts()
            _contacts.value = loadedContacts
        } catch (e: Exception) {
            Log.e("DialerApp", "Error loading contacts", e)
        }
    }
    
    private suspend fun loadRecentCalls() {
        try {
            val recentCallsList = contactManager.loadRecentCalls()
            _recentCalls.value = recentCallsList
            callHistory.addAll(recentCallsList)
        } catch (e: Exception) {
            Log.e("DialerApp", "Error loading recent calls", e)
        }
    }
    
    private suspend fun generateSmartSuggestions() {
        try {
            if (config.enableSmartSuggestions) {
                val suggestions = smartDialer.generateSuggestions(
                    recentCalls = _recentCalls.value,
                    contacts = _contacts.value,
                    currentTime = LocalDateTime.now()
                )
                _smartSuggestions.value = suggestions
            }
        } catch (e: Exception) {
            Log.e("DialerApp", "Error generating smart suggestions", e)
        }
    }
    
    private suspend fun generateAutoCompleteSuggestions(partialNumber: String) {
        try {
            if (partialNumber.isNotEmpty()) {
                val suggestions = contactManager.getAutoCompleteSuggestions(partialNumber)
                _smartSuggestions.value = suggestions
            } else {
                generateSmartSuggestions()
            }
        } catch (e: Exception) {
            Log.e("DialerApp", "Error generating autocomplete suggestions", e)
        }
    }
    
    private fun recordCallAttempt(number: String, context: CallContext) {
        val record = CallRecord(
            id = UUID.randomUUID().toString(),
            phoneNumber = number,
            displayName = findContactName(number) ?: "Unknown",
            direction = "OUTGOING",
            timestamp = LocalDateTime.now(),
            duration = 0,
            callType = determineCallType(number, context)
        )
        
        callHistory.add(record)
        
        // Update recent calls
        val updatedRecentCalls = (listOf(record) + _recentCalls.value).take(50)
        _recentCalls.value = updatedRecentCalls
    }
    
    private fun findContactName(number: String): String? {
        return _contacts.value.find { it.phoneNumber == number }?.name
    }
    
    private fun determineCallType(number: String, context: CallContext): String {
        return when {
            number.startsWith("911") || number.startsWith("999") -> "EMERGENCY"
            context.isBusinessCall -> "BUSINESS"
            context.isConstructionRelated -> "CONSTRUCTION"
            else -> "PERSONAL"
        }
    }
    
    private suspend fun updateSmartSuggestions(dialedNumber: String) {
        // Update smart suggestions based on dialing patterns
        smartDialer.updateCallPattern(dialedNumber, LocalDateTime.now())
        generateSmartSuggestions()
    }
    
    private fun saveCallHistory() {
        // Save call history to persistent storage
        Log.d("DialerApp", "Saving call history with ${callHistory.size} records")
    }
    
    // Helper classes
    
    private inner class ContactManager {
        fun initialize(context: Context) {
            Log.d("ContactManager", "Initializing contact manager")
        }
        
        fun shutdown() {
            Log.d("ContactManager", "Shutting down contact manager")
        }
        
        suspend fun loadAllContacts(): List<Contact> {
            // In a real implementation, this would load from device contacts
            return listOf(
                Contact("1", "John Doe", "+1234567890", "Contractor", "john@example.com"),
                Contact("2", "Jane Smith", "+0987654321", "Architect", "jane@example.com"),
                Contact("3", "Construction Co", "+1122334455", "Supplier", "contact@construction.com")
            )
        }
        
        suspend fun loadRecentCalls(): List<CallRecord> {
            // Load recent calls from call log
            return emptyList() // Simplified
        }
        
        suspend fun searchContacts(query: String): List<Contact> {
            return loadAllContacts().filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.phoneNumber.contains(query) ||
                it.role.contains(query, ignoreCase = true)
            }
        }
        
        suspend fun getAutoCompleteSuggestions(partialNumber: String): List<SmartContact> {
            val matchingContacts = loadAllContacts().filter { 
                it.phoneNumber.contains(partialNumber) 
            }
            
            return matchingContacts.map { contact ->
                SmartContact(
                    contact = contact,
                    relevanceScore = calculateRelevanceScore(contact, partialNumber),
                    reason = "Phone number match",
                    suggestedTime = "Now"
                )
            }
        }
        
        private fun calculateRelevanceScore(contact: Contact, partialNumber: String): Double {
            return if (contact.phoneNumber.startsWith(partialNumber)) 0.9 else 0.6
        }
    }
    
    private inner class SmartDialer {
        private val callPatterns = mutableMapOf<String, CallPattern>()
        
        fun initialize() {
            Log.d("SmartDialer", "Initializing smart dialer")
        }
        
        fun shutdown() {
            Log.d("SmartDialer", "Shutting down smart dialer")
        }
        
        suspend fun generateSuggestions(
            recentCalls: List<CallRecord>,
            contacts: List<Contact>,
            currentTime: LocalDateTime
        ): List<SmartContact> {
            val suggestions = mutableListOf<SmartContact>()
            
            // Frequent contacts
            val frequentContacts = analyzeCallFrequency(recentCalls)
            suggestions.addAll(frequentContacts.take(3))
            
            // Time-based suggestions
            val timeBasedSuggestions = analyzeTimePatterns(recentCalls, currentTime)
            suggestions.addAll(timeBasedSuggestions.take(2))
            
            // Business hour suggestions
            if (isBusinessHours(currentTime)) {
                val businessContacts = contacts.filter { it.role in listOf("Contractor", "Architect", "Supplier") }
                suggestions.addAll(businessContacts.take(2).map { contact ->
                    SmartContact(
                        contact = contact,
                        relevanceScore = 0.7,
                        reason = "Business hours",
                        suggestedTime = "Now"
                    )
                })
            }
            
            return suggestions.distinctBy { it.contact.phoneNumber }.take(5)
        }
        
        fun analyzeCallContext(number: String): CallContext {
            val pattern = callPatterns[number]
            return CallContext(
                isBusinessCall = pattern?.isBusinessCall ?: false,
                isConstructionRelated = pattern?.isConstructionRelated ?: false,
                averageCallDuration = pattern?.averageDuration ?: 0,
                lastCallTime = pattern?.lastCallTime
            )
        }
        
        fun updateCallPattern(number: String, callTime: LocalDateTime) {
            val existing = callPatterns[number]
            if (existing != null) {
                callPatterns[number] = existing.copy(
                    callCount = existing.callCount + 1,
                    lastCallTime = callTime
                )
            } else {
                callPatterns[number] = CallPattern(
                    phoneNumber = number,
                    callCount = 1,
                    lastCallTime = callTime,
                    isBusinessCall = isBusinessHours(callTime),
                    isConstructionRelated = false, // Would be determined by contact analysis
                    averageDuration = 0
                )
            }
        }
        
        private fun analyzeCallFrequency(recentCalls: List<CallRecord>): List<SmartContact> {
            val frequencyMap = recentCalls.groupBy { it.phoneNumber }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
            
            return frequencyMap.take(3).mapNotNull { (number, count) ->
                val contact = _contacts.value.find { it.phoneNumber == number }
                contact?.let {
                    SmartContact(
                        contact = it,
                        relevanceScore = minOf(count / 10.0, 1.0),
                        reason = "Frequently called ($count times)",
                        suggestedTime = "Now"
                    )
                }
            }
        }
        
        private fun analyzeTimePatterns(recentCalls: List<CallRecord>, currentTime: LocalDateTime): List<SmartContact> {
            val currentHour = currentTime.hour
            val sameDayCalls = recentCalls.filter { call ->
                val callHour = call.timestamp.hour
                kotlin.math.abs(callHour - currentHour) <= 1
            }
            
            return sameDayCalls.mapNotNull { call ->
                val contact = _contacts.value.find { it.phoneNumber == call.phoneNumber }
                contact?.let {
                    SmartContact(
                        contact = it,
                        relevanceScore = 0.8,
                        reason = "Usually called at this time",
                        suggestedTime = "Now"
                    )
                }
            }
        }
        
        private fun isBusinessHours(time: LocalDateTime): Boolean {
            val hour = time.hour
            val dayOfWeek = time.dayOfWeek.value
            return dayOfWeek in 1..5 && hour in 9..17
        }
    }
    
    private inner class ConstructionDialerFeatures {
        fun initialize() {
            Log.d("ConstructionDialerFeatures", "Initializing construction features")
        }
        
        fun shutdown() {
            Log.d("ConstructionDialerFeatures", "Shutting down construction features")
        }
        
        suspend fun getConstructionSpecificContacts(): List<Contact> {
            return _contacts.value.filter { contact ->
                contact.role in listOf("Contractor", "Architect", "Engineer", "Supplier", "Inspector")
            }
        }
        
        suspend fun getEmergencyContacts(): List<Contact> {
            return listOf(
                Contact("emergency_1", "Emergency Services", "911", "Emergency", ""),
                Contact("emergency_2", "Site Safety Hotline", "+1-800-SAFETY", "Emergency", ""),
                Contact("emergency_3", "OSHA Hotline", "+1-800-321-6742", "Emergency", "")
            )
        }
    }
    
    // Data classes
    
    data class Contact(
        val id: String,
        val name: String,
        val phoneNumber: String,
        val role: String,
        val email: String
    )
    
    data class CallRecord(
        val id: String,
        val phoneNumber: String,
        val displayName: String,
        val direction: String,
        val timestamp: LocalDateTime,
        val duration: Long,
        val callType: String
    )
    
    data class SmartContact(
        val contact: Contact,
        val relevanceScore: Double,
        val reason: String,
        val suggestedTime: String
    )
    
    data class CallContext(
        val isBusinessCall: Boolean,
        val isConstructionRelated: Boolean,
        val averageCallDuration: Long,
        val lastCallTime: LocalDateTime?
    )
    
    data class CallPattern(
        val phoneNumber: String,
        val callCount: Int,
        val lastCallTime: LocalDateTime,
        val isBusinessCall: Boolean,
        val isConstructionRelated: Boolean,
        val averageDuration: Long
    )
    
    data class DialerConfig(
        val enableSmartSuggestions: Boolean,
        val enableConstructionMode: Boolean,
        val enableVoiceDialing: Boolean,
        val enableCallAnalytics: Boolean,
        val autoCompleteContacts: Boolean,
        val enableEmergencyContacts: Boolean
    )
}

/**
 * Composable UI for the NextGen Dialer
 */
@Composable
fun DialerAppUI(
    dialerApp: DialerApp,
    modifier: Modifier = Modifier
) {
    val dialedNumber by dialerApp.dialedNumber.collectAsState()
    val recentCalls by dialerApp.recentCalls.collectAsState()
    val contacts by dialerApp.contacts.collectAsState()
    val smartSuggestions by dialerApp.smartSuggestions.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Keypad", "Recent", "Contacts", "Smart")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> KeypadTab(dialerApp, dialedNumber)
            1 -> RecentCallsTab(recentCalls) { dialerApp.dialFromHistory(it) }
            2 -> ContactsTab(contacts) { dialerApp.dialContact(it) }
            3 -> SmartSuggestionsTab(smartSuggestions) { dialerApp.dialContact(it.contact) }
        }
    }
}

@Composable
private fun KeypadTab(
    dialerApp: DialerApp,
    dialedNumber: String
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display number
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = if (dialedNumber.isEmpty()) "Enter number" else dialedNumber,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
                color = if (dialedNumber.isEmpty()) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Keypad
        DialerKeypad(
            onDigitPressed = { digit -> 
                kotlinx.coroutines.runBlocking { dialerApp.addDigit(digit) }
            },
            onBackspacePressed = { 
                kotlinx.coroutines.runBlocking { dialerApp.removeLastDigit() }
            },
            onCallPressed = {
                if (dialedNumber.isNotEmpty()) {
                    kotlinx.coroutines.runBlocking { dialerApp.dialNumber(dialedNumber) }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { kotlinx.coroutines.runBlocking { dialerApp.clearNumber() } },
                enabled = dialedNumber.isNotEmpty()
            ) {
                Text("Clear")
            }
            
            Button(
                onClick = { /* Add to contacts */ },
                enabled = dialedNumber.isNotEmpty()
            ) {
                Text("Add Contact")
            }
        }
    }
}

@Composable
private fun DialerKeypad(
    onDigitPressed: (String) -> Unit,
    onBackspacePressed: () -> Unit,
    onCallPressed: () -> Unit
) {
    val keypadButtons = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("*" to "", "0" to "+", "#" to "")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keypadButtons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { (digit, letters) ->
                    DialerKeypadButton(
                        digit = digit,
                        letters = letters,
                        onClick = { onDigitPressed(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Bottom row with backspace and call
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) // Empty space
            
            FloatingActionButton(
                onClick = onCallPressed,
                containerColor = Color.Green,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
            
            IconButton(
                onClick = onBackspacePressed,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Backspace")
            }
        }
    }
}

@Composable
private fun DialerKeypadButton(
    digit: String,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecentCallsTab(
    recentCalls: List<CallRecord>,
    onCallSelected: (CallRecord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recentCalls) { call ->
            CallRecordItem(call, onCallSelected)
        }
    }
}

@Composable
private fun CallRecordItem(
    call: CallRecord,
    onCallSelected: (CallRecord) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onCallSelected(call) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (call.direction) {
                    "INCOMING" -> Icons.Default.CallReceived
                    "OUTGOING" -> Icons.Default.CallMade
                    else -> Icons.Default.Call
                },
                contentDescription = call.direction,
                tint = when (call.callType) {
                    "EMERGENCY" -> Color.Red
                    "BUSINESS" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = call.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = call.callType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { onCallSelected(call) }) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}

@Composable
private fun ContactsTab(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts) { contact ->
            ContactItem(contact, onContactSelected)
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    onContactSelected: (Contact) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onContactSelected(contact) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Contact",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = contact.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { onContactSelected(contact) }) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}

@Composable
private fun SmartSuggestionsTab(
    suggestions: List<SmartContact>,
    onSuggestionSelected: (SmartContact) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Smart Suggestions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(suggestions) { suggestion ->
            SmartSuggestionItem(suggestion, onSuggestionSelected)
        }
    }
}

@Composable
private fun SmartSuggestionItem(
    suggestion: SmartContact,
    onSuggestionSelected: (SmartContact) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSuggestionSelected(suggestion) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = "Smart Suggestion",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = suggestion.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Confidence: ${(suggestion.relevanceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { onSuggestionSelected(suggestion) }) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}