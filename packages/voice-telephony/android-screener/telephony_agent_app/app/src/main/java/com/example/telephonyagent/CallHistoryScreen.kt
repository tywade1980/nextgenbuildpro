package com.example.telephonyagent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A simple call history screen showing a list of recent calls and whether
 * each was allowed or blocked.  This is a placeholder implementation;
 * integrate with Room or the system call log to display real data.
 */
@Composable
fun CallHistoryScreen() {
    val sampleHistory = listOf(
        CallLogEntry(phoneNumber = "+15551234567", time = "09:23", outcome = "Allowed"),
        CallLogEntry(phoneNumber = "+15559876543", time = "10:15", outcome = "Blocked"),
        CallLogEntry(phoneNumber = "+15557654321", time = "11:05", outcome = "Allowed"),
    )
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(sampleHistory) { entry ->
            CallLogRow(entry)
        }
    }
}

@Composable
private fun CallLogRow(entry: CallLogEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.phoneNumber, style = MaterialTheme.typography.bodyLarge)
            Text(text = entry.time, style = MaterialTheme.typography.bodyMedium)
            Text(text = entry.outcome, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class CallLogEntry(val phoneNumber: String, val time: String, val outcome: String)