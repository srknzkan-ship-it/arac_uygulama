package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.media.TrackInfo

/**
 * Universal Album Artwork Composable
 * Displays real embedded ID3 album cover, MediaStore album art, FM radio artwork,
 * or beautiful dynamic stylized album artwork for any song or station.
 */
@Composable
fun AlbumArtworkView(
    trackInfo: TrackInfo,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(trackInfo.albumArtBytes) {
        trackInfo.albumArtBytes?.let { bytes ->
            try {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 1. Embedded MP3 ID3 Picture Bitmap
            decodedBitmap != null -> {
                Image(
                    bitmap = decodedBitmap.asImageBitmap(),
                    contentDescription = trackInfo.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Subtle gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x99000000))
                            )
                        )
                )
            }

            // 2. MediaStore Album Art Uri (Coil)
            trackInfo.albumArtUri != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trackInfo.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = trackInfo.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 3. Radio Station Artwork
            trackInfo.frequency != null -> {
                RadioStationArtwork(
                    stationName = trackInfo.stationName ?: "FM Radio",
                    frequency = trackInfo.frequency,
                    genre = trackInfo.album,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Bespoke Song/Album Styled Artworks
            trackInfo.title.contains("Blinding", ignoreCase = true) || trackInfo.albumArtRes == "blinding_lights" -> {
                BlindingLightsCoverArt(modifier = Modifier.fillMaxSize())
            }

            trackInfo.title.contains("Lucky", ignoreCase = true) || trackInfo.albumArtRes == "get_lucky" -> {
                DaftPunkCoverArt(isPlaying = isPlaying, modifier = Modifier.fillMaxSize())
            }

            trackInfo.title.contains("Starboy", ignoreCase = true) || trackInfo.albumArtRes == "starboy" -> {
                StarboyCoverArt(isPlaying = isPlaying, modifier = Modifier.fillMaxSize())
            }

            trackInfo.title.contains("Midnight", ignoreCase = true) || trackInfo.albumArtRes == "midnight_city" -> {
                MidnightCityCoverArt(isPlaying = isPlaying, modifier = Modifier.fillMaxSize())
            }

            trackInfo.title.contains("Nightcall", ignoreCase = true) || trackInfo.albumArtRes == "nightcall" -> {
                NightcallCoverArt(isPlaying = isPlaying, modifier = Modifier.fillMaxSize())
            }

            // 5. Default High-Fidelity Automotive Vinyl & Disc Art
            else -> {
                DefaultAutomotiveMusicArt(
                    title = trackInfo.title,
                    artist = trackInfo.artist,
                    isLocal = trackInfo.isLocalFile,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Daft Punk Random Access Memories Theme
 */
@Composable
private fun DaftPunkCoverArt(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "daft_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F0A1C), Color(0xFF030712))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Golden / Chrome Twin Helmets Silhouette
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700).copy(alpha = 0.35f * pulse), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(cx, cy)
            )

            // Gold Visor Left
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFE082))
                ),
                topLeft = Offset(cx - w * 0.28f, cy - h * 0.12f),
                size = Size(w * 0.26f, h * 0.24f),
                cornerRadius = CornerRadius(14.dp.toPx())
            )

            // Chrome Visor Right
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF00E5FF))
                ),
                topLeft = Offset(cx + w * 0.02f, cy - h * 0.12f),
                size = Size(w * 0.26f, h * 0.24f),
                cornerRadius = CornerRadius(14.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        ) {
            Text(
                text = "DAFT PUNK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD700),
                letterSpacing = 2.sp
            )
            Text(
                text = "Random Access Memories",
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

/**
 * Starboy / Cyberpunk Neon Cross Artwork
 */
@Composable
private fun StarboyCoverArt(isPlaying: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF831843), Color(0xFF1E1B4B), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Glowing Neon Cross
            drawLine(
                color = Color(0xFFF43F5E),
                start = Offset(cx, cy - h * 0.32f),
                end = Offset(cx, cy + h * 0.32f),
                strokeWidth = 8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFF43F5E),
                start = Offset(cx - w * 0.24f, cy - h * 0.08f),
                end = Offset(cx + w * 0.24f, cy - h * 0.08f),
                strokeWidth = 8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFFE4E6),
                start = Offset(cx, cy - h * 0.30f),
                end = Offset(cx, cy + h * 0.30f),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }

        Text(
            text = "STARBOY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFE4E6),
            letterSpacing = 3.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
        )
    }
}

/**
 * Midnight City / Synthwave Horizon Artwork
 */
@Composable
private fun MidnightCityCoverArt(isPlaying: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF312E81), Color(0xFF4C1D95), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Giant Neon Sun
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFEC4899))
                ),
                radius = w * 0.28f,
                center = Offset(cx, cy - h * 0.05f)
            )

            // Horizon Grid
            val horizonY = h * 0.60f
            for (i in 0..4) {
                val y = horizonY + (h - horizonY) * (i / 4f)
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Text(
            text = "MIDNIGHT CITY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF38BDF8),
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

/**
 * Nightcall / Drive Synthwave Artwork
 */
@Composable
private fun NightcallCoverArt(isPlaying: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0284C7), Color(0xFF0F172A), Color(0xFF020617))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NIGHTCALL",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "KAVINSKY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * FM Radio Live Broadcast Station Artwork
 */
@Composable
private fun RadioStationArtwork(
    stationName: String,
    frequency: Double,
    genre: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radio_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0284C7).copy(alpha = 0.3f),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Radio signal concentric pulse circles
            if (isPlaying) {
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    radius = (w * 0.35f) * waveScale,
                    center = Offset(cx, cy - 10.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                    radius = (w * 0.24f) * waveScale,
                    center = Offset(cx, cy - 10.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF334155),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "CANLI YAYIN" else "RADYO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Text(
                text = stationName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", frequency)} MHz",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            Text(
                text = genre,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

/**
 * Default High-Fidelity Automotive Music Album Art with vinyl rotation & waveform
 */
@Composable
private fun DefaultAutomotiveMusicArt(
    title: String,
    artist: String,
    isLocal: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF0284C7).copy(alpha = 0.25f),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Hi-Fi Vinyl Disc
        Box(
            modifier = Modifier
                .size(92.dp)
                .rotate(if (isPlaying) rotation else 0f)
                .clip(CircleShape)
                .background(Color(0xFF0B1120))
                .border(2.dp, Color(0xFF334155), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = Color(0xFF1E293B), radius = 38.dp.toPx(), center = center)
                drawCircle(color = Color(0xFF0F172A), radius = 28.dp.toPx(), center = center)
                drawCircle(color = Color(0xFF0284C7), radius = 16.dp.toPx(), center = center)
                drawCircle(color = Color(0xFF00E5FF), radius = 6.dp.toPx(), center = center)
            }
        }

        // Top-End Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xCC0F172A)
        ) {
            Text(
                text = if (isLocal) "Yerel MP3" else "Hi-Fi Audio",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Bottom Song Title Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xCC0F172A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = artist,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
