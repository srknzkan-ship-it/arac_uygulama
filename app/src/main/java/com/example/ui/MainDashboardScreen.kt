package com.example.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()

    val mapLocation by viewModel.mapLocation.collectAsStateWithLifecycle()
    val locationSourceMode by viewModel.locationSourceMode.collectAsStateWithLifecycle()
    val mapOrientation by viewModel.mapOrientation.collectAsStateWithLifecycle()
    val mapZoom by viewModel.mapZoom.collectAsStateWithLifecycle()
    val panOffsetX by viewModel.panOffsetX.collectAsStateWithLifecycle()
    val panOffsetY by viewModel.panOffsetY.collectAsStateWithLifecycle()
    val isUserPanning by viewModel.isUserPanning.collectAsStateWithLifecycle()
    val activeRoute by viewModel.activeRoute.collectAsStateWithLifecycle()
    val isTrafficVisible by viewModel.isTrafficVisible.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val regionPacks by viewModel.cachedRegionPacks.collectAsStateWithLifecycle()
    val cachedTileCount by viewModel.cachedTileCount.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isMediaPlaying by viewModel.isMediaPlaying.collectAsStateWithLifecycle()
    val radioStations by viewModel.radioStations.collectAsStateWithLifecycle()
    val currentFrequency by viewModel.currentFrequency.collectAsStateWithLifecycle()
    val ambientColor by viewModel.ambientColor.collectAsStateWithLifecycle()

    val isOfflineDialogOpen by viewModel.isOfflineDialogOpen.collectAsStateWithLifecycle()
    val isRouteOptimizerOpen by viewModel.isRouteOptimizerOpen.collectAsStateWithLifecycle()
    val is360PanoramaOpen by viewModel.is360PanoramaOpen.collectAsStateWithLifecycle()
    val isAppsCenterOpen by viewModel.isAppsCenterOpen.collectAsStateWithLifecycle()
    val isBluetoothDialogOpen by viewModel.isBluetoothDialogOpen.collectAsStateWithLifecycle()
    val isPhoneDialogOpen by viewModel.isPhoneDialogOpen.collectAsStateWithLifecycle()
    val isVehicleSettingsOpen by viewModel.isVehicleSettingsOpen.collectAsStateWithLifecycle()
    val isObdClusterOpen by viewModel.isObdClusterOpen.collectAsStateWithLifecycle()
    val isLocalMusicDialogOpen by viewModel.isLocalMusicDialogOpen.collectAsStateWithLifecycle()
    val localAudioTracks by viewModel.localAudioTracks.collectAsStateWithLifecycle()
    val isScanningLocalMedia by viewModel.isScanningLocalMedia.collectAsStateWithLifecycle()

    // Real-time Clock (e.g., 10:09 AM)
    var currentTimeString by remember { mutableStateOf("10:09 AM") }
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        while (true) {
            currentTimeString = timeFormat.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFCAD0D9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Status Bar
            TopMetallicStatusBar(
                currentTime = currentTimeString,
                temperature = "21°C",
                isBtConnected = telemetry.isBluetoothConnected,
                onCloseClick = { viewModel.openAppsCenter() }
            )

            // 2. Main Body: Left Rail + Floating Media Card + Right Vector Map
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Left Vertical Navigation Rail
                LeftRailNavigationSidebar(
                    onNavClick = { viewModel.openRouteOptimizer() },
                    onMusicClick = { viewModel.openLocalMusicDialog() },
                    onCarClick = { viewModel.openVehicleSettings() },
                    onToggleDrawer = { viewModel.open360Panorama() },
                    onAppsClick = { viewModel.openAppsCenter() }
                )

                // Main Content Area with Map as Background & Floating Infotainment Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Full Vector Navigation Map
                    ModernInfotainmentMapView(
                        location = mapLocation,
                        orientationMode = mapOrientation,
                        zoomLevel = mapZoom,
                        isTrafficVisible = isTrafficVisible,
                        activeRoute = activeRoute,
                        panOffsetX = panOffsetX,
                        panOffsetY = panOffsetY,
                        isUserPanning = isUserPanning,
                        onPanMap = { dx, dy -> viewModel.panMap(dx, dy) },
                        onRecenter = { viewModel.recenterMap() },
                        onToggleOrientation = { viewModel.mapEngine.toggleOrientationMode() },
                        onZoomIn = { viewModel.mapEngine.zoomIn() },
                        onZoomOut = { viewModel.mapEngine.zoomOut() },
                        onSearchClick = { viewModel.openRouteOptimizer() },
                        onLocationClick = { viewModel.openRouteOptimizer() },
                        onSettingsClick = { viewModel.openVehicleSettings() },
                        onLayersClick = { viewModel.openOfflineDialog() },
                        onDestinationChipClick = { viewModel.openRouteOptimizer() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Floating Left Frosted Infotainment Card (Blinding Lights & Menu)
                    TeslaStyleInfotainmentCard(
                        trackInfo = currentTrack,
                        isPlaying = isMediaPlaying,
                        onPlayPause = { viewModel.mediaManager.togglePlayPause() },
                        onNext = { viewModel.mediaManager.nextTrack() },
                        onPrev = { viewModel.mediaManager.prevTrack() },
                        onSeek = { newSec -> viewModel.seekToPosition(newSec) },
                        onOpenNavi = { viewModel.openRouteOptimizer() },
                        onOpenMusic = { viewModel.openLocalMusicDialog() },
                        onOpenPhone = { viewModel.openPhoneDialog() },
                        onOpenVehicleSettings = { viewModel.openVehicleSettings() },
                        onOpenApps = { viewModel.openAppsCenter() },
                        onOpenLocalMusic = { viewModel.openLocalMusicDialog() },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }

        // Local Music Library & Storage Files Dialog
        if (isLocalMusicDialogOpen) {
            LocalMusicLibraryDialog(
                localTracks = localAudioTracks,
                currentTrack = currentTrack,
                isPlaying = isMediaPlaying,
                isScanning = isScanningLocalMedia,
                onSelectTrack = { track -> viewModel.playLocalTrack(track) },
                onPickAudioFile = { uri, name -> viewModel.playAudioUri(uri, name) },
                onRescan = { viewModel.rescanLocalMedia() },
                onClose = { viewModel.closeLocalMusicDialog() }
            )
        }

        // Route Optimizer & Traffic Calculator Dialog
        if (isRouteOptimizerOpen) {
            RouteOptimizerDialog(
                currentLocation = mapLocation,
                cityPresets = viewModel.cityPresets,
                locationSourceMode = locationSourceMode,
                onEnableRealGps = { viewModel.enableRealGps() },
                onSelectCity = { city -> viewModel.selectCity(city) },
                onStartNavigation = { destination ->
                    viewModel.startNavigationTo(destination)
                },
                onClose = { viewModel.closeRouteOptimizer() }
            )
        }

        // Phone & Hands-Free Dialer Dialog
        if (isPhoneDialogOpen) {
            PhoneCallDialog(
                isConnected = telemetry.isBluetoothConnected,
                onPlayDtmfTone = { tone -> viewModel.playDtmfTone(tone) },
                onClose = { viewModel.closePhoneDialog() }
            )
        }

        // Vehicle Settings & Dynamic Ambience Dialog
        if (isVehicleSettingsOpen) {
            VehicleSettingsDialog(
                currentAmbientColor = ambientColor,
                onColorSelected = { col -> viewModel.setAmbientColor(col) },
                onOpenObdDiagnostics = { viewModel.openObdCluster() },
                onClose = { viewModel.closeVehicleSettings() }
            )
        }

        // 360° Panorama & Parking Assist Dialog
        if (is360PanoramaOpen) {
            Panorama360Dialog(
                onClose = { viewModel.close360Panorama() }
            )
        }

        // Apps Center & Radio Stations List Dialog
        if (isAppsCenterOpen) {
            AppsCenterDialog(
                radioStations = radioStations,
                currentFrequency = currentFrequency,
                onSelectStation = { freq ->
                    viewModel.mediaManager.selectStation(freq)
                },
                onOpenNavi = { viewModel.openRouteOptimizer() },
                onOpenObd = { viewModel.openObdCluster() },
                onOpenOfflineDb = { viewModel.openOfflineDialog() },
                onOpenLocalMusic = { viewModel.openLocalMusicDialog() },
                onClose = { viewModel.closeAppsCenter() }
            )
        }

        // Bluetooth & OBD2 Manager Dialog
        if (isBluetoothDialogOpen) {
            BluetoothManagerDialog(
                availableDevices = availableDevices,
                scanState = scanState,
                connectedDeviceName = telemetry.connectedDeviceName,
                onConnectDevice = { device ->
                    viewModel.obdManager.connectToDevice(device)
                },
                onStartScan = { viewModel.obdManager.scanPairedDevices() },
                onClose = { viewModel.closeBluetoothDialog() }
            )
        }

        // OBD2 Cluster & Live Diagnostics Modal
        if (isObdClusterOpen) {
            Dialog(onDismissRequest = { viewModel.closeObdCluster() }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.90f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp)),
                    color = Color(0xFF0F172A)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Araç Ayarları & OBD2 Dijital Gösterge",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = { viewModel.closeObdCluster() }) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        ObdClusterPanel(
                            telemetry = telemetry,
                            availableDevices = availableDevices,
                            scanState = scanState,
                            onScanDevices = { viewModel.obdManager.scanPairedDevices() },
                            onConnectDevice = { dev -> viewModel.obdManager.connectToDevice(dev) },
                            onDisconnect = { viewModel.obdManager.disconnect() },
                            onThrottleChanged = { /* dyno */ },
                            onScanDtc = { viewModel.obdManager.scanDtcCodes() },
                            onClearDtc = { viewModel.obdManager.clearDtcCodes() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Offline Maps Database Dialog
        if (isOfflineDialogOpen) {
            OfflineMapsDialog(
                cachedTileCount = cachedTileCount,
                regionPacks = regionPacks,
                isOfflineForced = isOfflineMode,
                onToggleOfflineForced = { viewModel.mapEngine.toggleOfflineMode() },
                onDownloadPack = { packId -> viewModel.mapEngine.downloadPack(packId) },
                onClearCache = { viewModel.mapEngine.clearCache() },
                onDismiss = { viewModel.closeOfflineDialog() }
            )
        }
    }
}

@Composable
private fun TopMetallicStatusBar(
    currentTime: String,
    temperature: String,
    isBtConnected: Boolean,
    onCloseClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        color = Color(0xFFD6DCE4)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Close X, Time (10:09 AM), Weather (Cloud/Sun + 21°C)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onCloseClick() }
                )

                Text(
                    text = currentTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF0F172A)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = temperature,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // Right: Bluetooth, Wi-Fi, Location, Battery Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = if (isBtConnected) Color(0xFF0284C7) else Color(0xFF64748B),
                    modifier = Modifier.size(17.dp)
                )
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(17.dp)
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(17.dp)
                )
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LeftRailNavigationSidebar(
    onNavClick: () -> Unit,
    onMusicClick: () -> Unit,
    onCarClick: () -> Unit,
    onToggleDrawer: () -> Unit,
    onAppsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(46.dp)
            .fillMaxHeight(),
        color = Color(0xFFD6DCE4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section Icons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                RailIconButton(icon = Icons.Default.NearMe, contentDescription = "Navigation", onClick = onNavClick)
                RailIconButton(icon = Icons.Default.MusicNote, contentDescription = "Music Player", onClick = onMusicClick)
                RailIconButton(icon = Icons.Default.DirectionsCar, contentDescription = "Vehicle", onClick = onCarClick)
                RailIconButton(icon = Icons.Default.KeyboardArrowDown, contentDescription = "Collapse", onClick = onToggleDrawer)
            }

            // Bottom Section Icon (9-Dot App Drawer Grid)
            RailIconButton(icon = Icons.Default.Apps, contentDescription = "App Drawer", onClick = onAppsClick)
        }
    }
}

@Composable
private fun RailIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF334155),
            modifier = Modifier.size(20.dp)
        )
    }
}
