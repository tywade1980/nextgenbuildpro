package com.nextgenbuildpro.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Debug Screen for NextGenBuildPro
 * 
 * This screen has been emptied to remove debugging functionality.
 */
@Composable
fun DebugScreen(navController: NavController) {
    // Empty implementation - debugging disabled
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Debugging disabled")
    }
}
