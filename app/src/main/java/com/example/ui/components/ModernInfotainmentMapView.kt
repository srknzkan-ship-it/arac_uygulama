package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.map.ActiveRoute
import com.example.data.map.MapLocation
import com.example.data.map.MapOrientationMode
import com.example.data.map.TurnAction
import org.json.JSONArray

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ModernInfotainmentMapView(
    location: MapLocation,
    orientationMode: MapOrientationMode,
    zoomLevel: Float,
    isTrafficVisible: Boolean,
    activeRoute: ActiveRoute?,
    panOffsetX: Float,
    panOffsetY: Float,
    isUserPanning: Boolean,
    onPanMap: (Float, Float) -> Unit,
    onRecenter: () -> Unit,
    onToggleOrientation: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit = onSearchClick,
    onSettingsClick: () -> Unit,
    onLayersClick: () -> Unit,
    onDestinationChipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var currentTileTheme by remember { mutableStateOf("voyager") } // voyager, dark, osm, satellite

    // Convert active route waypoints to JSON array
    val routeGeoJson = remember(activeRoute?.waypoints) {
        val waypoints = activeRoute?.waypoints
        if (waypoints.isNullOrEmpty()) {
            "[]"
        } else {
            val arr = JSONArray()
            for (wp in waypoints) {
                val pt = JSONArray()
                pt.put(wp.first)
                pt.put(wp.second)
                arr.put(pt)
            }
            arr.toString()
        }
    }

    // Effect to update vehicle position and route on real live map
    LaunchedEffect(location.lat, location.lng, location.heading, zoomLevel, routeGeoJson, isUserPanning, currentTileTheme) {
        webViewRef?.let { wv ->
            val js = """
                if (window.updateVehicle) {
                    window.updateVehicle(
                        ${location.lat},
                        ${location.lng},
                        ${location.heading},
                        ${location.speedKmh},
                        $zoomLevel,
                        ${!isUserPanning},
                        $routeGeoJson,
                        '$currentTileTheme'
                    );
                }
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        }
    }

    Box(
        modifier = modifier
            .testTag("modern_infotainment_map")
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 1. Real Interactive Leaflet / OpenStreetMap AndroidView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onUserDraggedMap() {
                            Handler(Looper.getMainLooper()).post {
                                onPanMap(0f, 0f)
                            }
                        }

                        @JavascriptInterface
                        fun onMapReady() {
                            Handler(Looper.getMainLooper()).post {
                                isMapLoaded = true
                            }
                        }
                    }, "AndroidBridge")

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            val initJs = """
                                if (window.initMap) {
                                    window.initMap(
                                        ${location.lat},
                                        ${location.lng},
                                        ${location.heading},
                                        $zoomLevel,
                                        '$currentTileTheme'
                                    );
                                }
                            """.trimIndent()
                            view?.evaluateJavascript(initJs, null)
                        }
                    }

                    val mapHtml = buildRealMapHtml(
                        initialLat = location.lat,
                        initialLng = location.lng,
                        initialHeading = location.heading,
                        initialZoom = zoomLevel,
                        theme = currentTileTheme
                    )
                    loadDataWithBaseURL("https://openstreetmap.org", mapHtml, "text/html", "utf-8", null)
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top-Center Turn-by-Turn Guidance Card
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 180.dp)
                .clip(RoundedCornerShape(14.dp))
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x30000000))
                .clickable { onDestinationChipClick() },
            color = Color(0xFF1E293B).copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Maneuver Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    val turnIcon = when (location.nextTurnAction) {
                        TurnAction.TURN_RIGHT, TurnAction.SLIGHT_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                        TurnAction.TURN_LEFT, TurnAction.SLIGHT_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                        TurnAction.U_TURN -> Icons.Default.TurnLeft
                        else -> Icons.Default.Navigation
                    }
                    Icon(
                        imageVector = turnIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.5f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = location.cityName.ifEmpty { "Canlı GPS" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "${location.distanceToNextTurnMeters} m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = location.streetName.ifEmpty { "Canlı Konum Takibi" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Sonraki: ${location.nextStreetName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 3. Live GPS Fix & Satellite Status Pill (Top Left of Map)
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 18.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (location.isRealGpsFix) Color(0xFF10B981) else Color(0xFF38BDF8))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (location.isRealGpsFix) Color(0xFF10B981) else Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (location.isRealGpsFix) "CANLI GPS AKTİF" else "CANLI KONUM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${String.format(java.util.Locale.US, "%.4f", location.lat)}°, ${String.format(java.util.Locale.US, "%.4f", location.lng)}°",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // 4. Speedometer & Speed Limit Badges (Bottom Center)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp, start = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed limit sign (Red Circle)
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFEF4444))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${location.speedLimitKmh}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            // Current Speedometer Pill
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${location.speedKmh.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (location.speedKmh > location.speedLimitKmh) Color(0xFFEF4444) else Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "km/h",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // 5. Recenter Button (Appears if user drags/pans the map)
        AnimatedVisibility(
            visible = isUserPanning,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 180.dp)
        ) {
            Button(
                onClick = {
                    onRecenter()
                    webViewRef?.evaluateJavascript("if (window.recenterMap) window.recenterMap();", null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Canlı Konuma Dön", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // 6. Floating Action Controls on Right Side
        // Top Right: Settings, Search & Location Selector
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingMapIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Vehicle Settings",
                onClick = onSettingsClick
            )
            FloatingMapIconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Location & GPS",
                onClick = {
                    onRecenter()
                    webViewRef?.evaluateJavascript("if (window.recenterMap) window.recenterMap();", null)
                }
            )
            FloatingMapIconButton(
                icon = Icons.Default.Search,
                contentDescription = "Search Destination",
                onClick = onSearchClick
            )
        }

        // Bottom Right: Zoom In, Zoom Out, Layer Themes & Orientation
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingMapIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                onClick = {
                    onZoomIn()
                    webViewRef?.evaluateJavascript("if (window.map) window.map.zoomIn();", null)
                }
            )
            FloatingMapIconButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                onClick = {
                    onZoomOut()
                    webViewRef?.evaluateJavascript("if (window.map) window.map.zoomOut();", null)
                }
            )
            FloatingMapIconButton(
                icon = Icons.Default.Layers,
                contentDescription = "Map Layer Style",
                onClick = {
                    currentTileTheme = when (currentTileTheme) {
                        "voyager" -> "dark"
                        "dark" -> "satellite"
                        "satellite" -> "osm"
                        else -> "voyager"
                    }
                    webViewRef?.evaluateJavascript("if (window.setTileTheme) window.setTileTheme('$currentTileTheme');", null)
                }
            )
            FloatingMapIconButton(
                icon = if (orientationMode == MapOrientationMode.COURSE_UP) Icons.Default.Navigation else Icons.Default.Explore,
                contentDescription = "Toggle Orientation",
                onClick = {
                    onToggleOrientation()
                    webViewRef?.evaluateJavascript("if (window.toggleOrientation) window.toggleOrientation();", null)
                }
            )
        }
    }
}

@Composable
private fun FloatingMapIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .shadow(6.dp, CircleShape, spotColor = Color(0x30000000))
            .clickable { onClick() },
        color = Color(0xFF1E293B).copy(alpha = 0.90f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * Builds high-performance interactive Leaflet HTML with real OpenStreetMap & CartoDB tiles
 */
private fun buildRealMapHtml(
    initialLat: Double,
    initialLng: Double,
    initialHeading: Float,
    initialZoom: Float,
    theme: String
): String {
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map {
                margin: 0;
                padding: 0;
                width: 100%;
                height: 100%;
                background: #0f172a;
                overflow: hidden;
            }
            .leaflet-control-attribution, .leaflet-control-zoom {
                display: none !important;
            }
            .vehicle-marker-container {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 48px;
                height: 48px;
                position: relative;
                pointer-events: none;
            }
            .vehicle-radar {
                position: absolute;
                width: 44px;
                height: 44px;
                border-radius: 50%;
                background: rgba(0, 229, 255, 0.2);
                border: 1.5px solid rgba(0, 229, 255, 0.6);
                animation: pulseRadar 2s infinite ease-out;
            }
            @keyframes pulseRadar {
                0% { transform: scale(0.6); opacity: 1; }
                100% { transform: scale(1.4); opacity: 0; }
            }
            .vehicle-pointer {
                width: 28px;
                height: 28px;
                transform-origin: center center;
                transition: transform 0.2s ease-out;
                filter: drop-shadow(0 2px 6px rgba(0, 229, 255, 0.7));
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = null;
            var vehicleMarker = null;
            var routePolyline = null;
            var currentTileLayer = null;
            var isFollowing = true;
            var currentHeading = $initialHeading;
            var curLat = $initialLat;
            var curLng = $initialLng;

            var tileUrls = {
                voyager: 'https://cartodb-basemaps-a.global.ssl.fastly.net/rastertiles/voyager/{z}/{x}/{y}.png',
                dark: 'https://cartodb-basemaps-a.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png',
                osm: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                satellite: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
            };

            function initMap(lat, lng, heading, zoom, theme) {
                curLat = lat;
                curLng = lng;
                currentHeading = heading;

                map = L.map('map', {
                    center: [lat, lng],
                    zoom: zoom || 16,
                    zoomControl: false,
                    attributionControl: false
                });

                setTileTheme(theme || 'voyager');

                // Custom Vehicle Marker
                var vehicleHtml = '<div class="vehicle-marker-container">' +
                    '<div class="vehicle-radar"></div>' +
                    '<svg class="vehicle-pointer" id="vPointer" viewBox="0 0 40 40" style="transform: rotate(' + heading + 'deg);">' +
                    '<circle cx="20" cy="20" r="16" fill="rgba(15, 23, 42, 0.85)" stroke="#00E5FF" stroke-width="2.5" />' +
                    '<polygon points="20,7 28,30 20,24 12,30" fill="#00E5FF" />' +
                    '</svg>' +
                    '</div>';

                var vehicleIcon = L.divIcon({
                    className: 'vehicle-div-icon',
                    html: vehicleHtml,
                    iconSize: [48, 48],
                    iconAnchor: [24, 24]
                });

                vehicleMarker = L.marker([lat, lng], { icon: vehicleIcon, interactive: false }).addTo(map);

                // User pan / drag detection
                map.on('dragstart', function() {
                    isFollowing = false;
                    if (window.AndroidBridge && window.AndroidBridge.onUserDraggedMap) {
                        window.AndroidBridge.onUserDraggedMap();
                    }
                });

                if (window.AndroidBridge && window.AndroidBridge.onMapReady) {
                    window.AndroidBridge.onMapReady();
                }
            }

            function setTileTheme(theme) {
                var url = tileUrls[theme] || tileUrls.voyager;
                if (currentTileLayer) {
                    map.removeLayer(currentTileLayer);
                }
                currentTileLayer = L.tileLayer(url, {
                    maxZoom: 19,
                    subdomains: 'abcd'
                }).addTo(map);
            }

            function updateVehicle(lat, lng, heading, speed, zoom, autoFollow, routePoints, theme) {
                curLat = lat;
                curLng = lng;
                currentHeading = heading;

                if (vehicleMarker) {
                    vehicleMarker.setLatLng([lat, lng]);
                    var pointer = document.getElementById('vPointer');
                    if (pointer) {
                        pointer.style.transform = 'rotate(' + heading + 'deg)';
                    }
                }

                if (autoFollow && map) {
                    map.panTo([lat, lng], { animate: true, duration: 0.5 });
                }

                // Render Route Polyline
                if (routePoints && routePoints.length > 0) {
                    if (routePolyline) {
                        map.removeLayer(routePolyline);
                    }
                    routePolyline = L.polyline(routePoints, {
                        color: '#00E5FF',
                        weight: 6,
                        opacity: 0.85,
                        lineCap: 'round',
                        lineJoin: 'round'
                    }).addTo(map);
                } else if (routePolyline) {
                    map.removeLayer(routePolyline);
                    routePolyline = null;
                }
            }

            function recenterMap() {
                isFollowing = true;
                if (map) {
                    map.setView([curLat, curLng], 16, { animate: true });
                }
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}
