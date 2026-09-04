package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherClockCard(
    currentTime: String,
    currentDate: String,
    temperature: String = "24°C",
    condition: String = "Güneşli",
    city: String = "İstanbul",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.2.dp, Color(0xFF334155), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0C121D)
                        ),
                        center = Offset(150f, 100f),
                        radius = 450f
                    )
                )
        ) {
            // Ambient Art: Glowing Planet Spheres & Skyline Silhouette
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Glowing Cyan Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0x0000E5FF)),
                        center = Offset(w * 0.52f, h * 0.4f),
                        radius = 65.dp.toPx()
                    ),
                    radius = 55.dp.toPx(),
                    center = Offset(w * 0.52f, h * 0.4f)
                )

                // Glowing Purple/Magenta Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD946EF).copy(alpha = 0.55f), Color(0x00D946EF)),
                        center = Offset(w * 0.78f, h * 0.35f),
                        radius = 75.dp.toPx()
                    ),
                    radius = 65.dp.toPx(),
                    center = Offset(w * 0.78f, h * 0.35f)
                )

                // Stylized City Skyline Silhouette at bottom right
                val skylinePath = Path().apply {
                    moveTo(w * 0.65f, h)
                    lineTo(w * 0.65f, h * 0.62f)
                    lineTo(w * 0.70f, h * 0.62f)
                    lineTo(w * 0.70f, h * 0.48f)
                    lineTo(w * 0.73f, h * 0.48f)
                    lineTo(w * 0.73f, h * 0.38f) // High rise tower
                    lineTo(w * 0.76f, h * 0.38f)
                    lineTo(w * 0.76f, h * 0.55f)
                    lineTo(w * 0.80f, h * 0.55f)
                    lineTo(w * 0.80f, h * 0.44f)
                    lineTo(w * 0.85f, h * 0.44f)
                    lineTo(w * 0.85f, h * 0.60f)
                    lineTo(w * 0.90f, h * 0.60f)
                    lineTo(w * 0.90f, h * 0.50f)
                    lineTo(w * 0.95f, h * 0.50f)
                    lineTo(w * 0.95f, h)
                    close()
                }
                drawPath(
                    skylinePath,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.45f), Color(0xFF0F172A).copy(alpha = 0.9f)),
                        startY = h * 0.35f,
                        endY = h
                    )
                )
            }

            // Foreground Content: Time, Date & Weather Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Large Digital Clock (14:49)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = currentTime.ifEmpty { "14:49" },
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = currentDate.ifEmpty { "2024-01-08 • Pazartesi" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Weather Indicator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Weather",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "$temperature $condition",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = city,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
