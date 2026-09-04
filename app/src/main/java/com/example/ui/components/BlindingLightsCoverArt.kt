package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlindingLightsCoverArt(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0818))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Cyberpunk / Synthwave background gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F051D),
                        Color(0xFF1E0A3C),
                        Color(0xFF380835),
                        Color(0xFF080D21)
                    )
                )
            )

            // 2. Horizon Neon Grid Lines
            val horizonY = h * 0.62f
            for (i in 0..6) {
                val y = horizonY + (h - horizonY) * (i * i / 36f)
                drawLine(
                    color = Color(0xFFE11D48).copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
            // Vertical Perspective lines
            for (i in -4..4) {
                val startX = w / 2f + i * (w * 0.08f)
                val endX = w / 2f + i * (w * 0.32f)
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                    start = Offset(startX, horizonY),
                    end = Offset(endX, h),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // 3. Neon City Backdrop Silhouette
            val cityBuildings = listOf(
                Pair(w * 0.10f, h * 0.22f),
                Pair(w * 0.20f, h * 0.32f),
                Pair(w * 0.32f, h * 0.18f),
                Pair(w * 0.44f, h * 0.28f),
                Pair(w * 0.58f, h * 0.16f),
                Pair(w * 0.70f, h * 0.30f),
                Pair(w * 0.82f, h * 0.24f)
            )
            for (b in cityBuildings) {
                drawRect(
                    color = Color(0xFF1A0B2E).copy(alpha = 0.7f),
                    topLeft = Offset(b.first, horizonY - b.second),
                    size = Size(w * 0.11f, b.second)
                )
            }

            // 4. Retro Sports Car (Ferrari Testarossa / Synthwave Coupe Rear)
            val carCenterX = w * 0.5f
            val carCenterY = h * 0.68f
            val carWidth = w * 0.72f
            val carHeight = h * 0.28f

            // Car Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(carCenterX - carWidth * 0.52f, carCenterY + carHeight * 0.2f),
                size = Size(carWidth * 1.04f, carHeight * 0.35f)
            )

            // Car Body Lower Base
            val bodyLeft = carCenterX - carWidth * 0.5f
            val bodyTop = carCenterY - carHeight * 0.35f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B1259), Color(0xFF160E2E), Color(0xFF0A0717))
                ),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(carWidth, carHeight * 0.7f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )

            // Car Cabin / Rear Glass
            val cabinWidth = carWidth * 0.62f
            val cabinHeight = carHeight * 0.45f
            val cabinLeft = carCenterX - cabinWidth * 0.5f
            val cabinTop = bodyTop - cabinHeight * 0.65f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFF070B19))
                ),
                topLeft = Offset(cabinLeft, cabinTop),
                size = Size(cabinWidth, cabinHeight),
                cornerRadius = CornerRadius(10.dp.toPx())
            )

            // Rear Neon Tail-Light Bar (Bright Glowing Red / Orange)
            val lightBarTop = carCenterY - carHeight * 0.05f
            val lightBarWidth = carWidth * 0.88f
            val lightBarLeft = carCenterX - lightBarWidth * 0.5f
            // Outer glow
            drawRoundRect(
                color = Color(0xFFFF1744).copy(alpha = 0.6f * glowAlpha),
                topLeft = Offset(lightBarLeft - 4.dp.toPx(), lightBarTop - 3.dp.toPx()),
                size = Size(lightBarWidth + 8.dp.toPx(), 12.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            // Core bright light
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF5252),
                        Color(0xFFFFE57F),
                        Color(0xFFFF1744),
                        Color(0xFFFFE57F),
                        Color(0xFFFF5252)
                    )
                ),
                topLeft = Offset(lightBarLeft, lightBarTop),
                size = Size(lightBarWidth, 6.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            // Dual Exhaust & Rear Diffuser Glow
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.7f * glowAlpha),
                topLeft = Offset(carCenterX - carWidth * 0.38f, carCenterY + carHeight * 0.28f),
                size = Size(carWidth * 0.22f, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.7f * glowAlpha),
                topLeft = Offset(carCenterX + carWidth * 0.16f, carCenterY + carHeight * 0.28f),
                size = Size(carWidth * 0.22f, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Neon Wet Ground Reflections
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF1744).copy(alpha = 0.45f * glowAlpha),
                        Color(0xFF00E5FF).copy(alpha = 0.25f * glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(carCenterX, h * 0.88f),
                    radius = w * 0.45f
                ),
                topLeft = Offset(w * 0.1f, h * 0.78f),
                size = Size(w * 0.8f, h * 0.2f)
            )
        }

        // Top Neon Text: "Blinding Lights" in 2 lines with yellow/amber glow
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Blinding",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFFFE082),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Lights",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFFFD54F),
                letterSpacing = 0.5.sp
            )
        }
    }
}
