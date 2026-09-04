package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.map.CityPreset
import com.example.data.map.LocationSourceMode
import com.example.data.map.MapLocation

data class NavRouteOption(
    val id: String,
    val title: String,
    val viaText: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val trafficCondition: String,
    val trafficColor: Color,
    val isToll: Boolean = false
)

@Composable
fun RouteOptimizerDialog(
    currentLocation: MapLocation,
    cityPresets: List<CityPreset>,
    locationSourceMode: LocationSourceMode,
    onEnableRealGps: () -> Unit,
    onSelectCity: (CityPreset) -> Unit,
    onStartNavigation: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRouteId by remember { mutableStateOf("route_fastest") }

    // Find active city preset or default list
    val currentPreset = cityPresets.find { it.name.equals(currentLocation.cityName, ignoreCase = true) }
        ?: cityPresets.firstOrNull()

    val popularDestinations = currentPreset?.popularDestinations ?: listOf(
        "Şehir Merkezi & Ana Meydan",
        "Havalimanı Terminali",
        "Organize Sanayi Bölgesi",
        "Üniversite Kampüsü & Hastane",
        "Alışveriş ve Yaşam Merkezi",
        "Sahil Bulvarı / Çevre Yolu"
    )

    val filteredDestinations = if (searchQuery.isEmpty()) popularDestinations else popularDestinations.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    val routeOptions = listOf(
        NavRouteOption(
            id = "route_fastest",
            title = "En Hızlı Rota (Ana Arter)",
            viaText = "${currentLocation.streetName} ve Çevre Yolu üzerinden",
            durationMinutes = 18,
            distanceKm = 14.2,
            trafficCondition = "Trafik Akıcı (Yeşil)",
            trafficColor = Color(0xFF10B981)
        ),
        NavRouteOption(
            id = "route_express",
            title = "Alternatif 1 (Ekspres Hat)",
            viaText = "Şehirlerarası Bağlantı Yolu ve Tünel",
            durationMinutes = 22,
            distanceKm = 17.5,
            trafficCondition = "Normal Akış (Sarı)",
            trafficColor = Color(0xFFF59E0B)
        ),
        NavRouteOption(
            id = "route_scenic",
            title = "Alternatif 2 (Manzaralı / Şehir İçi)",
            viaText = "Bulvar ve Sahil / Park Güzergahı",
            durationMinutes = 29,
            distanceKm = 12.8,
            trafficCondition = "Yoğun Akış (Kırmızı)",
            trafficColor = Color(0xFFEF4444)
        )
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .testTag("route_optimizer_dialog")
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp)),
            color = Color(0xFF0B132B)
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
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Akıllı Rota & Konum Ayarı",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (locationSourceMode == LocationSourceMode.REAL_GPS) Color(0xFF10B981) else Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Konum: ${currentLocation.cityName}, ${currentLocation.streetName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GPS & City Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live GPS Toggle Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (locationSourceMode == LocationSourceMode.REAL_GPS) Color(0xFF059669).copy(alpha = 0.35f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (locationSourceMode == LocationSourceMode.REAL_GPS) Color(0xFF10B981) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable { onEnableRealGps() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = if (locationSourceMode == LocationSourceMode.REAL_GPS) Color(0xFF10B981) else Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Canlı GPS Konumu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (locationSourceMode == LocationSourceMode.REAL_GPS) Color(0xFF34D399) else Color.White
                            )
                        }
                    }

                    // City Presets horizontal scroll
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(cityPresets) { city ->
                            val isSelected = currentLocation.cityName.equals(city.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)
                                ),
                                modifier = Modifier.clickable { onSelectCity(city) }
                            ) {
                                Text(
                                    text = city.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hedef veya adres arayın (Örn: Havalimanı, Merkez, Üniversite...)", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF00E5FF)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color(0xFF94A3B8))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Split: Quick Suggestions vs Calculated Alternative Routes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Suggested Destinations for the selected city/area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${currentLocation.cityName} Popüler Hedefleri",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filteredDestinations) { dest ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = dest
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dest,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Calculated Alternative Routes (Trafik ve Süre Optimizasyonu)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "Canlı Trafik & Alternatif Güzergahlar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(routeOptions) { route ->
                                val isSelected = selectedRouteId == route.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRouteId = route.id }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = route.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${route.durationMinutes} dk",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00E5FF)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = route.viaText,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(route.trafficColor)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = route.trafficCondition,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = route.trafficColor
                                                )
                                            }

                                            Text(
                                                text = "${route.distanceKm} km",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) {
                        Text("İptal", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val target = searchQuery.ifEmpty { filteredDestinations.firstOrNull() ?: "Şehir Merkezi" }
                            onStartNavigation(target)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Navigasyonu Başlat",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
