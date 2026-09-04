package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.map.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotatingMapCanvas(
    location: MapLocation,
    orientationMode: MapOrientationMode,
    zoomLevel: Float,
    isTrafficVisible: Boolean,
    isOfflineMode: Boolean,
    mapTheme: MapStyleTheme,
    onToggleOrientation: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleTraffic: () -> Unit,
    onSearchClick: () -> Unit,
    onQuickDestClick: (String) -> Unit,
    onVoiceToggle: () -> Unit,
    isVoiceActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedHeading by animateFloatAsState(
        targetValue = location.heading,
        animationSpec = tween(durationMillis = 150),
        label = "heading"
    )

    Box(
        modifier = modifier
            .testTag("rotating_map_canvas")
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .border(1.2.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val outlineColor = MaterialTheme.colorScheme.outline

        // 1. Canvas Map Drawing (Rotating Ground & Routes)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val mapRotation = if (orientationMode == MapOrientationMode.COURSE_UP) -animatedHeading else 0f

            // Map Background Base
            drawRect(
                color = if (mapTheme == MapStyleTheme.NIGHT_AMBER_HUD) Color(0xFF0D0B05) else Color(0xFF131A29)
            )

            rotate(degrees = mapRotation, pivot = center) {
                // Water body
                drawWaterBody(center, zoomLevel)
                // Urban grid & city blocks
                drawUrbanGrid(center, zoomLevel, outlineColor)
                // Highways with traffic overlay
                drawHighways(center, zoomLevel, isTrafficVisible)
                // Navigation route glow path
                drawNavigationRoute(center, zoomLevel, primaryColor)
                // POIs (EV, Gas, Speed cameras)
                drawPois(center, zoomLevel, secondaryColor)
            }

            // Vehicle Cursor (Rotating Triangle / Pulsing Beacon)
            val vehicleTriangleAngle = if (orientationMode == MapOrientationMode.COURSE_UP) 0f else animatedHeading
            rotate(degrees = vehicleTriangleAngle, pivot = center) {
                drawVehicleTriangle(center, primaryColor, secondaryColor)
            }

            // Radar Circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = 65.dp.toPx(),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }

        // 2. Top-Left Turn-by-Turn Card + Live Speed
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TurnByTurnNavigationCard(location = location)

            // Live Vehicle Speed Gauge Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B111E).copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Hız",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${location.speedKmh.toInt()} km/h",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // 3. Top-Right Action Badges (Speed Limit, Offline DB, Layers, Orientation, Voice Audio)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOfflineMode) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ÇEVRİMDIŞI DB",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // European Speed Limit Round Sign
            SpeedLimitBadge(speedLimit = location.speedLimitKmh)

            // Map Layer / 3D Toggle
            MapIconButton(
                icon = Icons.Default.Layers,
                isActive = isTrafficVisible,
                onClick = onToggleTraffic
            )

            // Voice Turn Guidance Audio Toggle
            MapIconButton(
                icon = if (isVoiceActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                isActive = isVoiceActive,
                onClick = onVoiceToggle
            )

            // Map Orientation Toggle (Course-Up vs North-Up)
            MapIconButton(
                icon = if (orientationMode == MapOrientationMode.COURSE_UP) Icons.Default.Navigation else Icons.Default.Explore,
                isActive = orientationMode == MapOrientationMode.COURSE_UP,
                onClick = onToggleOrientation
            )

            // Re-center / GPS Location
            MapIconButton(
                icon = Icons.Default.MyLocation,
                isActive = true,
                onClick = { /* Centered */ }
            )
        }

        // 4. Bottom-Left Branding: "Petal Maps / AutoNavi"
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 62.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.75f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Petal Maps",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        // 5. Floating Bottom Destination Search Bar (Matching Attached Photo: "Hi, where to?")
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0C121D).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search Input Field Simulator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hi, where to? / Nereye gitmek istersiniz?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }

                // Quick POI Pills (Fuel, EV Charger, Parking, Route)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickSearchChip(
                        icon = Icons.Default.LocalGasStation,
                        label = "Benzin",
                        onClick = { onQuickDestClick("En Yakın Benzin İstasyonu") }
                    )
                    QuickSearchChip(
                        icon = Icons.Default.EvStation,
                        label = "Şarj",
                        onClick = { onQuickDestClick("Hızlı Şarj İstasyonu (EV)") }
                    )
                    QuickSearchChip(
                        icon = Icons.Default.LocalParking,
                        label = "Otopark",
                        onClick = { onQuickDestClick("En Yakın Kapalı Otopark") }
                    )
                    QuickSearchChip(
                        icon = Icons.Default.AltRoute,
                        label = "Rota",
                        onClick = onSearchClick
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickSearchChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MapIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (isActive) Color(0xFF0284C7).copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFF38BDF8) else Color(0xFF475569)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun TurnByTurnNavigationCard(
    location: MapLocation,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 240.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0C121D).copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (location.nextTurnAction) {
                        TurnAction.TURN_RIGHT -> Icons.Default.TurnRight
                        TurnAction.TURN_LEFT -> Icons.Default.TurnLeft
                        TurnAction.SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
                        TurnAction.SLIGHT_LEFT -> Icons.Default.TurnSlightLeft
                        TurnAction.ROUNDABOUT -> Icons.Default.Sync
                        TurnAction.U_TURN -> Icons.Default.UTurnLeft
                        else -> Icons.Default.Straight
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${location.distanceToNextTurnMeters} m sonra",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = location.nextStreetName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SpeedLimitBadge(speedLimit: Int) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color(0xFFEF4444), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$speedLimit",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
    }
}

// Map Drawing Helper Functions
private fun DrawScope.drawWaterBody(center: Offset, zoom: Float) {
    val scale = zoom / 15f
    val path = Path().apply {
        moveTo(center.x - 300 * scale, center.y - 450 * scale)
        cubicTo(
            center.x - 120 * scale, center.y - 200 * scale,
            center.x + 80 * scale, center.y + 100 * scale,
            center.x + 350 * scale, center.y + 400 * scale
        )
        lineTo(center.x + 500 * scale, center.y + 500 * scale)
        lineTo(center.x + 500 * scale, center.y - 500 * scale)
        close()
    }
    drawPath(path, color = Color(0xFF0A2540).copy(alpha = 0.6f))
}

private fun DrawScope.drawUrbanGrid(center: Offset, zoom: Float, outlineColor: Color) {
    val scale = zoom / 15f
    val step = 60f * scale
    val gridColor = Color(0xFF1E293B).copy(alpha = 0.5f)

    for (i in -6..6) {
        val y = center.y + i * step
        drawLine(gridColor, Offset(center.x - 400 * scale, y), Offset(center.x + 400 * scale, y), strokeWidth = 1.5f)
    }
    for (j in -6..6) {
        val x = center.x + j * step
        drawLine(gridColor, Offset(x, center.y - 400 * scale), Offset(x, center.y + 400 * scale), strokeWidth = 1.5f)
    }
}

private fun DrawScope.drawHighways(center: Offset, zoom: Float, showTraffic: Boolean) {
    val scale = zoom / 15f

    // Main D-100 Highway
    val highwayPath = Path().apply {
        moveTo(center.x - 350 * scale, center.y + 250 * scale)
        cubicTo(
            center.x - 100 * scale, center.y + 120 * scale,
            center.x + 100 * scale, center.y - 120 * scale,
            center.x + 350 * scale, center.y - 250 * scale
        )
    }

    // Base road
    drawPath(highwayPath, Color(0xFF334155), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 14f * scale))

    // Real-Time Traffic Congestion Overlay
    if (showTraffic) {
        // Flowing green section
        drawPath(
            highwayPath,
            Color(0xFF10B981),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 8f * scale,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(120f * scale, 40f * scale), 0f)
            )
        )
        // Moderate traffic amber line
        val moderatePath = Path().apply {
            moveTo(center.x - 80 * scale, center.y + 100 * scale)
            lineTo(center.x + 60 * scale, center.y - 40 * scale)
        }
        drawPath(moderatePath, Color(0xFFF59E0B), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f * scale))

        // Congested crimson line
        val congestedPath = Path().apply {
            moveTo(center.x + 60 * scale, center.y - 40 * scale)
            lineTo(center.x + 180 * scale, center.y - 140 * scale)
        }
        drawPath(congestedPath, Color(0xFFEF4444), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f * scale))
    }
}

private fun DrawScope.drawNavigationRoute(center: Offset, zoom: Float, color: Color) {
    val scale = zoom / 15f
    val navPath = Path().apply {
        moveTo(center.x - 200 * scale, center.y + 200 * scale)
        lineTo(center.x - 50 * scale, center.y + 100 * scale)
        lineTo(center.x, center.y) // Current vehicle position
        lineTo(center.x + 60 * scale, center.y - 120 * scale)
        lineTo(center.x + 180 * scale, center.y - 220 * scale)
    }

    // Glowing cyan navigation route line
    drawPath(
        navPath,
        Color(0xFF00E5FF).copy(alpha = 0.35f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 16f * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    drawPath(
        navPath,
        Color(0xFF00E5FF),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 8f * scale,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawPois(center: Offset, zoom: Float, secondaryColor: Color) {
    val scale = zoom / 15f
    // EV Charger Marker
    drawCircle(
        color = Color(0xFF10B981),
        radius = 7f * scale,
        center = Offset(center.x - 90 * scale, center.y + 70 * scale)
    )
    // Gas Station Marker
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 7f * scale,
        center = Offset(center.x + 120 * scale, center.y - 160 * scale)
    )
    // Speed Camera Marker
    drawCircle(
        color = Color(0xFFEF4444),
        radius = 7f * scale,
        center = Offset(center.x + 40 * scale, center.y - 90 * scale)
    )
}

private fun DrawScope.drawVehicleTriangle(center: Offset, primary: Color, secondary: Color) {
    val trianglePath = Path().apply {
        moveTo(center.x, center.y - 16.dp.toPx()) // Forward tip
        lineTo(center.x + 11.dp.toPx(), center.y + 12.dp.toPx()) // Right corner
        lineTo(center.x, center.y + 6.dp.toPx()) // Inward notch
        lineTo(center.x - 11.dp.toPx(), center.y + 12.dp.toPx()) // Left corner
        close()
    }

    // Outer glow
    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = 0.3f),
        radius = 24.dp.toPx(),
        center = center
    )

    // Triangle fill & outline
    drawPath(trianglePath, color = Color(0xFF00E5FF))
    drawPath(
        trianglePath,
        color = Color.White,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
    )
}
