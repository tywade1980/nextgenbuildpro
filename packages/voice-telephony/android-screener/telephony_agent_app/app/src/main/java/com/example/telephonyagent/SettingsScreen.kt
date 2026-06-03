package com.example.telephonyagent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * A settings screen that lets the user adjust the call screening
 * sensitivity threshold.  The value is persisted via [PreferencesManager].
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    // Retrieve the initial threshold once.  The slider uses a float value.
    var threshold by remember { mutableStateOf(prefs.getScreeningThreshold().toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Call Screening Sensitivity: ${'$'}{threshold.toInt()}")
        Slider(
            value = threshold,
            onValueChange = { threshold = it },
            valueRange = 0f..100f,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = {
            prefs.setScreeningThreshold(threshold.toInt())
        }) {
            Text(text = "Save")
        }
    }
}