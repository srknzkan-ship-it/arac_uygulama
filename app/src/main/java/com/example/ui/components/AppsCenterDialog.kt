package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.media.RadioStation

data class CarAppItem(
    val title: String,
    val category: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AppsCenterDialog(
    radioStations: List<RadioStation>,
    currentFrequency: Double,
    onSelectStation: (Double) -> Unit,
    onOpenNavi: () -> Unit,
    onOpenObd: () -> Unit,
    onOpenOfflineDb: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Apps, 1: Radio Stations

    val appsList = listOf(
        CarAppItem("Navigasyon", "Harita & Rota", Icons.Default.Navigation, Color(0xFF0284C7)),
        CarAppItem("FM Radyo", "Multimedya", Icons.Default.Radio, Color(0xFF00E5FF)),
        CarAppItem("Müzik Çalar", "Ses & Çalma Listesi", Icons.Default.MusicNote, Color(0xFFD946EF)),
        CarAppItem("OBD2 Göstergeler", "Araç Telemetrisi", Icons.Default.Speed, Color(0xFFF59E0B)),
        CarAppItem("360° Park Asistanı", "Güvenlik & Kamera", Icons.Default.CameraAlt, Color(0xFF10B981)),
        CarAppItem("Bluetooth Telefon", "Bağlantı & Çağrılar", Icons.Default.Phone, Color(0xFF3B82F6)),
        CarAppItem("Çevrimdışı Harita DB", "Veri Deposu", Icons.Default.CloudOff, Color(0xFF8B5CF6)),
        CarAppItem("Sürüş Ayarları", "Sistem & Ekran", Icons.Default.Settings, Color(0xFF64748B))
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .testTag("apps_center_dialog")
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
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
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Multimedya Uygulama & Radyo Merkezi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector (Apps vs Radio Stations)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(3.dp)
                ) {
                    TabPillButton(title = "Yüklü Araç Uygulamaları", isSelected = selectedTab == 0, modifier = Modifier.weight(1f)) {
                        selectedTab = 0
                    }
                    TabPillButton(title = "FM Radyo İstasyonları (Kanal Listesi)", isSelected = selectedTab == 1, modifier = Modifier.weight(1f)) {
                        selectedTab = 1
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Apps Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(appsList) { app ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (app.title) {
                                            "Navigasyon" -> { onOpenNavi(); onClose() }
                                            "OBD2 Göstergeler" -> { onOpenObd(); onClose() }
                                            "Çevrimdışı Harita DB" -> { onOpenOfflineDb(); onClose() }
                                            "Müzik Çalar" -> { onOpenLocalMusic(); onClose() }
                                            "FM Radyo" -> { selectedTab = 1 }
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(app.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = app.icon,
                                            contentDescription = app.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = app.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = app.category,
                                        fontSize = 9.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Radio Stations Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(radioStations) { station ->
                            val isCurrent = String.format("%.1f", station.frequency) == String.format("%.1f", currentFrequency)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isCurrent) Color(0xFF00E5FF) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectStation(station.frequency)
                                        onClose()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color(0xFF00E5FF) else Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CellTower,
                                            contentDescription = null,
                                            tint = if (isCurrent) Color(0xFF0F172A) else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${station.frequency} MHz",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isCurrent) Color(0xFF00E5FF) else Color.White
                                        )
                                        Text(
                                            text = station.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
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
    }
}

@Composable
private fun TabPillButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}
