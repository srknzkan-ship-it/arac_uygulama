package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AutoRepository
import com.example.data.map.*
import com.example.data.media.*
import com.example.data.obd.*
import com.example.data.voice.VoiceAssistantManager
import com.example.data.voice.VoiceState
import com.example.ui.components.DashboardActiveTab
import com.example.ui.theme.AutoDrivingThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AutoRepository.getInstance(application)
    val obdManager = ObdManager(application)
    val mapEngine = MapEngine(application, repository)
    val mediaManager = MediaManager(application, repository)

    private val _themeMode = MutableStateFlow(AutoDrivingThemeMode.CYBER_NIGHT)
    val themeMode: StateFlow<AutoDrivingThemeMode> = _themeMode.asStateFlow()

    private val _ambientColor = MutableStateFlow(Color(0xFF00E5FF))
    val ambientColor: StateFlow<Color> = _ambientColor.asStateFlow()

    private val _activeTab = MutableStateFlow(DashboardActiveTab.HOME_DASHBOARD)
    val activeTab: StateFlow<DashboardActiveTab> = _activeTab.asStateFlow()

    private val _isEcoMode = MutableStateFlow(false)
    val isEcoMode: StateFlow<Boolean> = _isEcoMode.asStateFlow()

    private val _isVoiceOverlayVisible = MutableStateFlow(false)
    val isVoiceOverlayVisible: StateFlow<Boolean> = _isVoiceOverlayVisible.asStateFlow()

    private val _isOfflineDialogOpen = MutableStateFlow(false)
    val isOfflineDialogOpen: StateFlow<Boolean> = _isOfflineDialogOpen.asStateFlow()

    private val _isRouteOptimizerOpen = MutableStateFlow(false)
    val isRouteOptimizerOpen: StateFlow<Boolean> = _isRouteOptimizerOpen.asStateFlow()

    private val _is360PanoramaOpen = MutableStateFlow(false)
    val is360PanoramaOpen: StateFlow<Boolean> = _is360PanoramaOpen.asStateFlow()

    private val _isAppsCenterOpen = MutableStateFlow(false)
    val isAppsCenterOpen: StateFlow<Boolean> = _isAppsCenterOpen.asStateFlow()

    private val _isBluetoothDialogOpen = MutableStateFlow(false)
    val isBluetoothDialogOpen: StateFlow<Boolean> = _isBluetoothDialogOpen.asStateFlow()

    private val _isPhoneDialogOpen = MutableStateFlow(false)
    val isPhoneDialogOpen: StateFlow<Boolean> = _isPhoneDialogOpen.asStateFlow()

    private val _isVehicleSettingsOpen = MutableStateFlow(false)
    val isVehicleSettingsOpen: StateFlow<Boolean> = _isVehicleSettingsOpen.asStateFlow()

    private val _isObdClusterOpen = MutableStateFlow(false)
    val isObdClusterOpen: StateFlow<Boolean> = _isObdClusterOpen.asStateFlow()

    private val _isLocalMusicDialogOpen = MutableStateFlow(false)
    val isLocalMusicDialogOpen: StateFlow<Boolean> = _isLocalMusicDialogOpen.asStateFlow()

    val telemetry: StateFlow<ObdTelemetry> = obdManager.telemetry
    val availableDevices: StateFlow<List<BluetoothObdDevice>> = obdManager.availableDevices
    val scanState: StateFlow<String> = obdManager.scanState

    val mapLocation: StateFlow<MapLocation> = mapEngine.currentLocation
    val locationSourceMode: StateFlow<LocationSourceMode> = mapEngine.locationSourceMode
    val cityPresets: List<CityPreset> = mapEngine.cityPresets
    val mapOrientation: StateFlow<MapOrientationMode> = mapEngine.orientationMode
    val mapZoom: StateFlow<Float> = mapEngine.zoomLevel
    val panOffsetX: StateFlow<Float> = mapEngine.panOffsetX
    val panOffsetY: StateFlow<Float> = mapEngine.panOffsetY
    val isUserPanning: StateFlow<Boolean> = mapEngine.isUserPanning
    val activeRoute: StateFlow<ActiveRoute?> = mapEngine.activeRoute
    val isTrafficVisible: StateFlow<Boolean> = mapEngine.isTrafficVisible
    val isOfflineMode: StateFlow<Boolean> = mapEngine.isOfflineMode
    val mapTheme: StateFlow<MapStyleTheme> = mapEngine.mapTheme
    val cachedRegionPacks: StateFlow<List<OfflineRegionPack>> = mapEngine.cachedPacks

    val mediaSource: StateFlow<MediaSource> = mediaManager.currentSource
    val isMediaPlaying: StateFlow<Boolean> = mediaManager.isPlaying
    val currentTrack: StateFlow<TrackInfo> = mediaManager.currentTrack
    val localAudioTracks: StateFlow<List<LocalAudioTrack>> = mediaManager.localAudioTracks
    val isScanningLocalMedia: StateFlow<Boolean> = mediaManager.isScanningLocalMedia
    val equalizerBands: StateFlow<List<Float>> = mediaManager.equalizerBands
    val radioStations: StateFlow<List<RadioStation>> = mediaManager.radioStations
    val currentFrequency: StateFlow<Double> = mediaManager.currentFrequency
    val volume: StateFlow<Float> = mediaManager.volume

    val cachedTileCount: StateFlow<Int> = repository.cachedTileCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1850
    )

    val voiceAssistant = VoiceAssistantManager(application) { action, arg ->
        handleVoiceCommand(action, arg)
    }
    val voiceState: StateFlow<VoiceState> = voiceAssistant.voiceState

    init {
        viewModelScope.launch {
            repository.insertInitialData()
        }
        obdManager.start()
        mapEngine.start()
        mediaManager.start()
    }

    fun setActiveTab(tab: DashboardActiveTab) {
        _activeTab.value = tab
    }

    fun setAmbientColor(color: Color) {
        _ambientColor.value = color
    }

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            AutoDrivingThemeMode.CYBER_NIGHT -> AutoDrivingThemeMode.AMBER_HUD_NIGHT
            AutoDrivingThemeMode.AMBER_HUD_NIGHT -> AutoDrivingThemeMode.DAY_HIGH_CONTRAST
            AutoDrivingThemeMode.DAY_HIGH_CONTRAST -> AutoDrivingThemeMode.CYBER_NIGHT
        }
        mapEngine.setMapTheme(
            when (_themeMode.value) {
                AutoDrivingThemeMode.CYBER_NIGHT -> MapStyleTheme.NIGHT_STEALTH_CYAN
                AutoDrivingThemeMode.AMBER_HUD_NIGHT -> MapStyleTheme.NIGHT_AMBER_HUD
                AutoDrivingThemeMode.DAY_HIGH_CONTRAST -> MapStyleTheme.DAY_HIGH_CONTRAST
            }
        )
    }

    fun toggleEcoMode() {
        _isEcoMode.value = !_isEcoMode.value
    }

    fun toggleVoiceOverlay() {
        _isVoiceOverlayVisible.value = !_isVoiceOverlayVisible.value
        if (_isVoiceOverlayVisible.value) {
            voiceAssistant.startListening()
        } else {
            voiceAssistant.stopListening()
        }
    }

    fun closeVoiceOverlay() {
        _isVoiceOverlayVisible.value = false
        voiceAssistant.stopListening()
    }

    fun openOfflineDialog() {
        _isOfflineDialogOpen.value = true
    }

    fun closeOfflineDialog() {
        _isOfflineDialogOpen.value = false
    }

    fun openRouteOptimizer() {
        _isRouteOptimizerOpen.value = true
    }

    fun closeRouteOptimizer() {
        _isRouteOptimizerOpen.value = false
    }

    fun open360Panorama() {
        _is360PanoramaOpen.value = true
    }

    fun close360Panorama() {
        _is360PanoramaOpen.value = false
    }

    fun openAppsCenter() {
        _isAppsCenterOpen.value = true
    }

    fun closeAppsCenter() {
        _isAppsCenterOpen.value = false
    }

    fun openBluetoothDialog() {
        _isBluetoothDialogOpen.value = true
    }

    fun closeBluetoothDialog() {
        _isBluetoothDialogOpen.value = false
    }

    fun openPhoneDialog() {
        _isPhoneDialogOpen.value = true
    }

    fun closePhoneDialog() {
        _isPhoneDialogOpen.value = false
    }

    fun openVehicleSettings() {
        _isVehicleSettingsOpen.value = true
    }

    fun closeVehicleSettings() {
        _isVehicleSettingsOpen.value = false
    }

    fun openObdCluster() {
        _isObdClusterOpen.value = true
    }

    fun closeObdCluster() {
        _isObdClusterOpen.value = false
    }

    fun openLocalMusicDialog() {
        _isLocalMusicDialogOpen.value = true
        mediaManager.scanLocalAudioFiles()
    }

    fun closeLocalMusicDialog() {
        _isLocalMusicDialogOpen.value = false
    }

    fun playLocalTrack(track: LocalAudioTrack) {
        mediaManager.playLocalTrack(track)
    }

    fun playAudioUri(uri: android.net.Uri, displayName: String) {
        mediaManager.playAudioUri(uri, displayName)
    }

    fun rescanLocalMedia() {
        mediaManager.scanLocalAudioFiles()
    }

    fun enableRealGps() {
        mapEngine.enableRealGps()
    }

    fun selectCity(city: CityPreset) {
        mapEngine.selectCity(city)
    }

    fun setCustomLocation(name: String, lat: Double, lng: Double) {
        mapEngine.setCustomLocation(name, lat, lng)
    }

    fun startNavigationTo(destination: String) {
        mapEngine.calculateAndStartRoute(destination)
        voiceAssistant.speak("$destination için en hızlı rota hesaplandı. Navigasyon başlatılıyor.")
    }

    fun panMap(dx: Float, dy: Float) {
        mapEngine.panMap(dx, dy)
    }

    fun recenterMap() {
        mapEngine.recenterMap()
    }

    fun setVolume(vol: Float) {
        mediaManager.setVolume(vol)
    }

    fun seekToPosition(positionSec: Int) {
        mediaManager.seekToPosition(positionSec)
    }

    fun playDtmfTone(char: Char) {
        mediaManager.playDtmfTone(char)
    }

    private fun handleVoiceCommand(action: String, arg: String) {
        when (action) {
            "THEME" -> {
                if (arg == "NIGHT") {
                    _themeMode.value = AutoDrivingThemeMode.AMBER_HUD_NIGHT
                    mapEngine.setMapTheme(MapStyleTheme.NIGHT_AMBER_HUD)
                } else {
                    _themeMode.value = AutoDrivingThemeMode.DAY_HIGH_CONTRAST
                    mapEngine.setMapTheme(MapStyleTheme.DAY_HIGH_CONTRAST)
                }
            }
            "MEDIA_SOURCE" -> {
                if (arg == "RADIO_FM") mediaManager.setSource(MediaSource.RADIO_FM)
                else mediaManager.setSource(MediaSource.LOCAL_STORAGE)
            }
            "MEDIA_CONTROL" -> {
                when (arg) {
                    "PLAY" -> if (!mediaManager.isPlaying.value) mediaManager.play()
                    "PAUSE" -> if (mediaManager.isPlaying.value) mediaManager.pause()
                    "NEXT" -> mediaManager.nextTrack()
                    "PREV" -> mediaManager.prevTrack()
                }
            }
            "RADIO_SELECT" -> {
                val freq = arg.toDoubleOrNull() ?: 100.0
                mediaManager.setSource(MediaSource.RADIO_FM)
                mediaManager.selectStation(freq)
            }
            "MAP_ZOOM" -> {
                if (arg == "IN") mapEngine.zoomIn() else mapEngine.zoomOut()
            }
            "MAP_TRAFFIC" -> {
                mapEngine.toggleTraffic()
            }
            "MAP_ORIENTATION" -> {
                mapEngine.toggleOrientationMode()
            }
            "OBD_SCAN" -> {
                obdManager.scanDtcCodes()
            }
            "POWER_MODE" -> {
                _isEcoMode.value = (arg == "ECO")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        obdManager.stop()
        mapEngine.stop()
        mediaManager.stop()
        voiceAssistant.destroy()
    }
}
