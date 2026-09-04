package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.media.TrackInfo

@Composable
fun TeslaStyleInfotainmentCard(
    trackInfo: TrackInfo,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Int) -> Unit,
    onOpenNavi: () -> Unit,
    onOpenMusic: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenVehicleSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenLocalMusic: () -> Unit = onOpenMusic,
    modifier: Modifier = Modifier
) {
    var isShuffleActive by remember { mutableStateOf(false) }
    var isRepeatActive by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf("Music") }

    val currentSeconds = trackInfo.currentPositionSeconds
    val totalSeconds = trackInfo.durationSeconds.coerceAtLeast(1)
    val sliderProgress = (currentSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val currentMinutes = currentSeconds / 60
    val currentSecRem = currentSeconds % 60
    val totalMinutes = totalSeconds / 60
    val totalSecRem = totalSeconds % 60
    val timeFormatted = String.format("%02d:%02d / %02d:%02d", currentMinutes, currentSecRem, totalMinutes, totalSecRem)

    Card(
        modifier = modifier
            .testTag("infotainment_floating_card")
            .width(280.dp)
            .fillMaxHeight()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(22.dp))
            .border(1.2.dp, Color(0xFFD1D5DB).copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEBEFF4).copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF3F5F8),
                            Color(0xFFE5E9F0)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Album Artwork Square Card (Clickable to open Local Music Library)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenLocalMusic() }
            ) {
                AlbumArtworkView(
                    trackInfo = trackInfo,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x30000000))
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xCC0F172A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (trackInfo.isLocalFile) Icons.Default.Folder else if (trackInfo.frequency != null) Icons.Default.Radio else Icons.Default.MusicNote,
                            contentDescription = "Müzik",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (trackInfo.isLocalFile) "Yerel MP3" else if (trackInfo.frequency != null) "FM Radyo" else "Müzik",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Song Title & Artist / Radio Station
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLocalMusic() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = trackInfo.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
                Text(
                    text = if (trackInfo.frequency != null) "${trackInfo.stationName ?: "FM"} • ${trackInfo.frequency} MHz" else trackInfo.artist,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Playback Controls Row: Shuffle, Prev, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isShuffleActive = !isShuffleActive },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleActive) Color(0xFF0284C7) else Color(0xFF475569),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onPrev,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { isRepeatActive = !isRepeatActive },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) Color(0xFF0284C7) else Color(0xFF475569),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 4. Progress Seek Bar & Real Time Display
            Slider(
                value = sliderProgress,
                onValueChange = { newProg ->
                    val newSec = (newProg * totalSeconds).toInt()
                    onSeek(newSec)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0284C7),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .padding(horizontal = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                if (isPlaying) {
                    Text(
                        text = "● CANLI SES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Divider(color = Color(0xFFCBD5E1).copy(alpha = 0.6f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Vertical Menu List Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                InfotainmentMenuItem(
                    icon = Icons.Default.NearMe,
                    title = "Navigation",
                    isSelected = selectedMenuItem == "Navigation",
                    onClick = {
                        selectedMenuItem = "Navigation"
                        onOpenNavi()
                    }
                )

                InfotainmentMenuItem(
                    icon = Icons.Default.MusicNote,
                    title = "Music & Radio",
                    isSelected = selectedMenuItem == "Music",
                    onClick = {
                        selectedMenuItem = "Music"
                        onOpenMusic()
                    }
                )

                InfotainmentMenuItem(
                    icon = Icons.Default.Phone,
                    title = "Phone & Dialer",
                    isSelected = selectedMenuItem == "Phone",
                    onClick = {
                        selectedMenuItem = "Phone"
                        onOpenPhone()
                    }
                )

                InfotainmentMenuItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Vehicle Settings",
                    isSelected = selectedMenuItem == "Vehicle Settings",
                    onClick = {
                        selectedMenuItem = "Vehicle Settings"
                        onOpenVehicleSettings()
                    }
                )

                InfotainmentMenuItem(
                    icon = Icons.Default.GridView,
                    title = "Apps Center",
                    isSelected = selectedMenuItem == "Apps",
                    onClick = {
                        selectedMenuItem = "Apps"
                        onOpenApps()
                    }
                )
            }
        }
    }
}

@Composable
private fun InfotainmentMenuItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFCBD5E1).copy(alpha = 0.45f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color(0xFF0284C7) else Color(0xFF475569),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF334155)
        )
    }
}
