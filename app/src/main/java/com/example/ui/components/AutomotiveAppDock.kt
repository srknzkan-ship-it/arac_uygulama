package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun AutomotiveAppDock(
    onOpenWindowApp: () -> Unit,
    onOpenNavi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenPanorama: () -> Unit,
    onOpenApps: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("automotive_app_dock")
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.2.dp, Color(0xFF334155), RoundedCornerShape(18.dp)),
        color = Color(0xFF0F172A).copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Window App (Purple Gradient 3D layers)
            AppDockItem(
                title = "window app",
                icon = Icons.Default.Layers,
                gradient = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                onClick = onOpenWindowApp
            )

            // 2. Navi (Blue Gradient Navigation Paper Airplane)
            AppDockItem(
                title = "Navi",
                icon = Icons.Default.Send,
                gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                onClick = onOpenNavi
            )

            // 3. Bluetooth (Cyan-Blue Gradient)
            AppDockItem(
                title = "Bluetooth",
                icon = Icons.Default.Bluetooth,
                gradient = listOf(Color(0xFF00E5FF), Color(0xFF0284C7)),
                onClick = onOpenBluetooth
            )

            // 4. 360 Panorama (Green Gradient 360 Camera)
            AppDockItem(
                title = "360 panorama...",
                icon = Icons.Default.CameraAlt,
                gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                onClick = onOpenPanorama
            )

            // 5. Apps (Red/Orange Gradient 4-Grid)
            AppDockItem(
                title = "Apps",
                icon = Icons.Default.GridView,
                gradient = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                onClick = onOpenApps
            )

            // 6. Theme (Pink/Purple Gradient Paint Roller)
            AppDockItem(
                title = "Theme",
                icon = Icons.Default.ColorLens,
                gradient = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                onClick = onToggleTheme
            )
        }
    }
}

@Composable
private fun AppDockItem(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // Glossy App Icon Squircle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(gradient))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCBD5E1),
            maxLines = 1
        )
    }
}
