package com.example.telephonyagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * MainActivity is the entry point of the application. It uses Jetpack Compose to
 * display a simple message indicating that the Telephony Agent service is
 * running. In a production application this screen would present call logs,
 * analytics and configuration options.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Defines navigation destinations and shows a bottom navigation bar.  The
 * current screen is controlled by a local mutable state.  Add more
 * destinations here as needed.
 */
@Composable
fun AppNavigation() {
    var selectedTab by remember { mutableStateOf(0) }
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("History", Icons.Filled.List),
        NavItem("Settings", Icons.Filled.Settings)
    )
    Column {
        // Main content area
        Surface(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> CallHistoryScreen()
                2 -> SettingsScreen()
            }
        }
        // Bottom navigation
        NavigationBar {
            navItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) }
                )
            }
        }
    }
}

@Composable
fun HomeScreen() {
    // Placeholder home screen.  Display a simple message or dashboard here.
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Telephony Agent running.", style = MaterialTheme.typography.bodyLarge)
    }
}

private data class NavItem(val label: String, val icon: ImageVector)