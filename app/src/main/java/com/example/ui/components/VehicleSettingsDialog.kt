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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun VehicleSettingsDialog(
    currentAmbientColor: Color,
    onColorSelected: (Color) -> Unit,
    onOpenObdDiagnostics: () -> Unit,
    onClose: () -> Unit
) {
    var selectedDriveMode by remember { mutableStateOf("Comfort") }
    var selectedRegen by remember { mutableStateOf("Standart") }
    var selectedSteering by remember { mutableStateOf("Sport") }
    var headlightsMode by remember { mutableStateOf("Otomatik (Matrix LED)") }
    var activeTab by remember { mutableStateOf(0) } // 0: Sürüş, 1: Işıklandırma & Ambiyans, 2: Lastik Basıncı (TPMS)

    val ambientColors = listOf(
        Pair("Siber Mavi", Color(0xFF00E5FF)),
        Pair("Neon Amber", Color(0xFFFFB300)),
        Pair("Kızıl Gece", Color(0xFFFF1744)),
        Pair("Zümrüt Yeşil", Color(0xFF10B981)),
        Pair("Buz Beyazı", Color(0xFFE2E8F0)),
        Pair("Mor Şafak", Color(0xFFA855F7))
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .testTag("vehicle_settings_dialog")
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
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
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Araç Dinamikleri & Konfigürasyon", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Sürüş Modu: $selectedDriveMode • Matrix LED Aktif", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs (Sürüş / Ambiyans / TPMS)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabPill(title = "Sürüş Dinamikleri", isSelected = activeTab == 0, onClick = { activeTab = 0 })
                    TabPill(title = "İç Ambiyans & Işık", isSelected = activeTab == 1, onClick = { activeTab = 1 })
                    TabPill(title = "Lastik Basıncı (TPMS)", isSelected = activeTab == 2, onClick = { activeTab = 2 })
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        0 -> {
                            // Sürüş Dinamikleri Tab
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Sürüş Modu Seçici
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sürüş Profili", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                    listOf("Sport (Maksimum Tepki)", "Comfort (Pürüzsüz Süspansiyon)", "Eco (Maksimum Menzil)").forEach { mode ->
                                        val isSelected = selectedDriveMode in mode
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFF0F172A),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedDriveMode = mode.substringBefore(" ") }
                                        ) {
                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = isSelected, onClick = { selectedDriveMode = mode.substringBefore(" ") })
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(mode, fontSize = 12.sp, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            onClose()
                                            onOpenObdDiagnostics()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("OBD2 Canlı Telemetriyi Aç", fontSize = 11.sp, color = Color.White)
                                    }
                                }

                                // Direksiyon ve Rejeneratif Frenleme
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column {
                                        Text("Rejeneratif Fren Seviyesi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Düşük", "Standart", "Yüksek").forEach { reg ->
                                                val isSel = selectedRegen == reg
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { selectedRegen = reg }
                                                ) {
                                                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                        Text(reg, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Column {
                                        Text("Direksiyon Sertliği", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Konfor", "Standart", "Sport").forEach { st ->
                                                val isSel = selectedSteering == st
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSel) Color(0xFF0284C7) else Color(0xFF0F172A),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { selectedSteering = st }
                                                ) {
                                                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                        Text(st, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // İç Ambiyans & LED Işıklandırma Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kabine Özel LED Ambiyans Renkleri", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ambientColors.forEach { (name, col) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { onColorSelected(col) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(col)
                                                    .border(2.dp, if (currentAmbientColor == col) Color.White else Color.Transparent, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(name, fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Farlar & Matrix Gece Aydınlatması", fontSize = 12.sp, color = Color.White)
                                    listOf("Kapalı", "Park", "Otomatik").forEach { f ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (headlightsMode.startsWith(f)) Color(0xFF0284C7) else Color(0xFF0F172A),
                                            modifier = Modifier.clickable { headlightsMode = "$f (Matrix LED)" }
                                        ) {
                                            Text(f, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Lastik Basınç Takip Sistemi (TPMS)
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    TireStatusCard("Ön Sol", "2.4 Bar / 35 PSI", "24°C", true)
                                    TireStatusCard("Arka Sol", "2.4 Bar / 35 PSI", "25°C", true)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(70.dp))
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    TireStatusCard("Ön Sağ", "2.4 Bar / 35 PSI", "24°C", true)
                                    TireStatusCard("Arka Sağ", "2.4 Bar / 35 PSI", "25°C", true)
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
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun TireStatusCard(
    position: String,
    psi: String,
    temp: String,
    isOk: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOk) Color(0xFF22C55E) else Color(0xFFEF4444))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(position, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(psi, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isOk) Color(0xFF22C55E) else Color(0xFFEF4444))
            Text(temp, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}
