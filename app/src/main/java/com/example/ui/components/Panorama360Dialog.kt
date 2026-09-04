package com.example.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun Panorama360Dialog(onClose: () -> Unit) {
    var selectedCameraView by remember { mutableStateOf("ALL_360") } // ALL_360, REAR, FRONT, LEFT, RIGHT

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .testTag("panorama_360_dialog")
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "360° Çevre Görüş & Park Asistanı",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Ultrasonik Radar Sensörleri Aktif (0.4m)",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Angle Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CameraTabButton(title = "360° Kuşbakışı", isSelected = selectedCameraView == "ALL_360") {
                        selectedCameraView = "ALL_360"
                    }
                    CameraTabButton(title = "Arka Kamera", isSelected = selectedCameraView == "REAR") {
                        selectedCameraView = "REAR"
                    }
                    CameraTabButton(title = "Ön Kamera", isSelected = selectedCameraView == "FRONT") {
                        selectedCameraView = "FRONT"
                    }
                    CameraTabButton(title = "Yan Kameralar", isSelected = selectedCameraView == "LEFT") {
                        selectedCameraView = "LEFT"
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 360 Visualization & Grid Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B0F19))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w / 2f, h / 2f)

                        // Draw Parking Dynamic Guidelines (Green / Yellow / Red arcs)
                        // Distance Arcs Rear
                        drawArc(
                            color = Color(0xFFEF4444).copy(alpha = 0.8f),
                            startAngle = 45f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(center.x - 110.dp.toPx(), center.y + 20.dp.toPx()),
                            size = Size(220.dp.toPx(), 120.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                        drawArc(
                            color = Color(0xFFF59E0B).copy(alpha = 0.8f),
                            startAngle = 45f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(center.x - 140.dp.toPx(), center.y + 35.dp.toPx()),
                            size = Size(280.dp.toPx(), 160.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                        drawArc(
                            color = Color(0xFF10B981).copy(alpha = 0.8f),
                            startAngle = 45f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(center.x - 170.dp.toPx(), center.y + 50.dp.toPx()),
                            size = Size(340.dp.toPx(), 200.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )

                        // Draw Vehicle Top-Down Body Wireframe
                        val carW = 60.dp.toPx()
                        val carH = 110.dp.toPx()
                        val carRect = androidx.compose.ui.geometry.Rect(
                            center.x - carW / 2, center.y - carH / 2,
                            center.x + carW / 2, center.y + carH / 2
                        )
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(carRect.left, carRect.top),
                            size = Size(carW, carH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF00E5FF),
                            topLeft = Offset(carRect.left, carRect.top),
                            size = Size(carW, carH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )

                        // Windshields & Roof
                        drawRoundRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(center.x - 22.dp.toPx(), center.y - 35.dp.toPx()),
                            size = Size(44.dp.toPx(), 70.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                    }

                    // Floating Camera Status Badges
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "4x HD CANLI AKIŞ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF10B981) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
