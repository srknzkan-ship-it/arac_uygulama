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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AutoDrivingThemeMode

enum class DashboardActiveTab {
    HOME_DASHBOARD,
    FULL_MAP,
    RADIO_MEDIA,
    OBD_DIAGNOSTICS
}

@Composable
fun QuickAppDock(
    activeTab: DashboardActiveTab,
    themeMode: AutoDrivingThemeMode,
    isVoiceActive: Boolean,
    isEcoMode: Boolean,
    onTabSelected: (DashboardActiveTab) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleVoice: () -> Unit,
    onToggleEco: () -> Unit,
    onOpenOfflineMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("quick_app_dock")
            .width(68.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Navigation Icons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DockIconButton(
                    icon = Icons.Default.Dashboard,
                    label = "Gösterge",
                    isSelected = activeTab == DashboardActiveTab.HOME_DASHBOARD,
                    onClick = { onTabSelected(DashboardActiveTab.HOME_DASHBOARD) }
                )
                DockIconButton(
                    icon = Icons.Default.Map,
                    label = "Harita",
                    isSelected = activeTab == DashboardActiveTab.FULL_MAP,
                    onClick = { onTabSelected(DashboardActiveTab.FULL_MAP) }
                )
                DockIconButton(
                    icon = Icons.Default.Radio,
                    label = "Radyo",
                    isSelected = activeTab == DashboardActiveTab.RADIO_MEDIA,
                    onClick = { onTabSelected(DashboardActiveTab.RADIO_MEDIA) }
                )
                DockIconButton(
                    icon = Icons.Default.Speed,
                    label = "OBD2",
                    isSelected = activeTab == DashboardActiveTab.OBD_DIAGNOSTICS,
                    onClick = { onTabSelected(DashboardActiveTab.OBD_DIAGNOSTICS) }
                )
                DockIconButton(
                    icon = Icons.Default.DownloadForOffline,
                    label = "Çevrimdışı",
                    isSelected = false,
                    onClick = onOpenOfflineMaps
                )
            }

            // Bottom Utilities (Voice, Night/Day Theme, Eco Mode)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Eco Mode Toggle
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onToggleEco() },
                    shape = CircleShape,
                    color = if (isEcoMode) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEcoMode) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Eco",
                            tint = if (isEcoMode) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Night/Day Driving Mode Toggle
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onToggleTheme() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (themeMode) {
                                AutoDrivingThemeMode.CYBER_NIGHT -> Icons.Default.NightlightRound
                                AutoDrivingThemeMode.AMBER_HUD_NIGHT -> Icons.Default.Brightness2
                                AutoDrivingThemeMode.DAY_HIGH_CONTRAST -> Icons.Default.WbSunny
                            },
                            contentDescription = "Gece/Gündüz Modu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Hands-free Voice Mic Button
                FilledIconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.size(46.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isVoiceActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Sesli Komut",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
