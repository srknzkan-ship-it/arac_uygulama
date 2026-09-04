package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.voice.VoiceState
import kotlin.math.sin

@Composable
fun VoiceWaveOverlay(
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onDirectCommand: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("voice_wave_overlay")
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AutoDrive Eller Serbest Sesli Asistan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Voice Wave Visualizer
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(0.6f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barCount = 18
                for (i in 0 until barCount) {
                    val baseFactor = sin(i * 0.45).toFloat().coerceAtLeast(0.1f)
                    val targetHeight = if (voiceState.isListening) {
                        (baseFactor * voiceState.waveformIntensity).coerceIn(0.15f, 1.0f)
                    } else 0.1f

                    val animHeight by animateFloatAsState(
                        targetValue = targetHeight,
                        animationSpec = tween(durationMillis = 80),
                        label = "voice_wave"
                    )

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight(fraction = animHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (voiceState.isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spoken Text / Feedback message
            Text(
                text = if (voiceState.spokenText.isNotEmpty()) voiceState.spokenText else voiceState.feedbackMessage,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Mic Trigger Button
            FilledIconButton(
                onClick = {
                    if (voiceState.isListening) onStopListening() else onStartListening()
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (voiceState.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (voiceState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // One-Tap Quick Automotive Commands
            val quickCommands = listOf(
                "Gece modu",
                "Gündüz modu",
                "Radyoyu aç",
                "Power FM aç",
                "Haritayı yakınlaştır",
                "Trafik durumunu göster",
                "Hız kaç",
                "OBD hata kodlarını tara",
                "Eco modu"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickCommands) { cmd ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDirectCommand(cmd) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = cmd,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
