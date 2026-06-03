package com.nextgenbuildpro.voice.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextgenbuildpro.voice.CarolineVoiceViewModel

/**
 * Converted from voice-ai-app/App.tsx JSX.
 * Animated orb that pulses while listening/speaking.
 * Press-and-hold to record; release to send.
 */
@Composable
fun CarolineVoiceScreen(
    viewModel: CarolineVoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.connect()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Caroline",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFE8D5FF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.isConnected) "● Live" else "○ Connecting",
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.isConnected) Color(0xFF66FF99) else Color(0xFF888888)
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedOrb(
                isListening = uiState.isListening,
                isSpeaking = uiState.isSpeaking,
                pulse = uiState.orbPulse,
                onPressStart = { viewModel.startListening() },
                onPressEnd = { viewModel.stopListening() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFAAAAAA)
            )

            if (uiState.transcriptText.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = uiState.transcriptText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDDDDDD),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            uiState.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = err,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF6666)
                )
            }
        }
    }
}

@Composable
private fun AnimatedOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    pulse: Float,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleScale"
    )

    val activeScale = when {
        isListening -> 1.0f + pulse * 0.4f
        isSpeaking  -> 1.15f
        else        -> idleScale
    }

    val orbColors = when {
        isListening -> listOf(Color(0xFF7B2FBE), Color(0xFFE040FB), Color(0xFF7B2FBE))
        isSpeaking  -> listOf(Color(0xFF1A6B8A), Color(0xFF00E5FF), Color(0xFF1A6B8A))
        else        -> listOf(Color(0xFF3D1A6B), Color(0xFF9C27B0), Color(0xFF3D1A6B))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .scale(activeScale)
            .background(
                brush = Brush.radialGradient(orbColors),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressEnd()
                    }
                )
            }
    ) {
        Text(
            text = when {
                isListening -> "🎤"
                isSpeaking  -> "🔊"
                else        -> "✨"
            },
            style = MaterialTheme.typography.displaySmall
        )
    }
}
