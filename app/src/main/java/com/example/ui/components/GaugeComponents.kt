package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.obd.ObdTelemetry
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speed: Float,
    gear: String,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speed,
        animationSpec = tween(durationMillis = 150),
        label = "speed"
    )

    Box(
        modifier = modifier
            .testTag("speedometer_gauge")
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val errorColor = MaterialTheme.colorScheme.error
        val trackColor = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            // Background sweep track (from 135 deg to 405 deg = 270 deg total)
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active speed arc
            val maxSpeed = 220f
            val speedFraction = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)
            val sweep = speedFraction * 270f

            val arcColor = when {
                animatedSpeed > 140 -> errorColor
                animatedSpeed > 90 -> secondaryColor
                else -> primaryColor
            }

            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tick marks
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = diameter / 2f
            for (i in 0..11) {
                val angleDeg = 135f + (i / 11f) * 270f
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = radius - strokeWidth - 6.dp.toPx()
                val outerR = radius - strokeWidth - 1.dp.toPx()

                val start = Offset(
                    center.x + (innerR * cos(angleRad)).toFloat(),
                    center.y + (innerR * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    center.x + (outerR * cos(angleRad)).toFloat(),
                    center.y + (outerR * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (i > 8) errorColor else primaryColor.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedSpeed.toInt()}",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "KM / H",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = gear,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun RpmCircularGauge(
    rpm: Int,
    modifier: Modifier = Modifier
) {
    val animatedRpm by animateFloatAsState(
        targetValue = rpm.toFloat(),
        animationSpec = tween(durationMillis = 120),
        label = "rpm"
    )

    Box(
        modifier = modifier
            .testTag("rpm_gauge")
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val errorColor = MaterialTheme.colorScheme.error
        val trackColor = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val maxRpm = 7000f
            val fraction = (animatedRpm / maxRpm).coerceIn(0f, 1f)
            val sweep = fraction * 270f

            drawArc(
                color = if (animatedRpm > 5500) errorColor else primaryColor,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedRpm / 1000f).let { String.format("%.1f", it) }}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "RPM x1000",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${animatedRpm.toInt()} d/dk",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (animatedRpm > 5500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun TelemetryQuickBar(
    telemetry: ObdTelemetry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TelemetryItem(
            label = "HARARET",
            value = "${telemetry.coolantTempC.toInt()}°C",
            color = if (telemetry.coolantTempC > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        TelemetryItem(
            label = "YAKIT",
            value = "%${telemetry.fuelLevelPct.toInt()}",
            color = MaterialTheme.colorScheme.secondary
        )
        TelemetryItem(
            label = "TURBO",
            value = "${String.format("%.2f", telemetry.turboBoostBar)} bar",
            color = MaterialTheme.colorScheme.primary
        )
        TelemetryItem(
            label = "AKÜ",
            value = "${String.format("%.1f", telemetry.batteryVoltage)}V",
            color = MaterialTheme.colorScheme.primary
        )
        TelemetryItem(
            label = "TÜKETİM",
            value = "${String.format("%.1f", telemetry.instantL100km)} L",
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}
