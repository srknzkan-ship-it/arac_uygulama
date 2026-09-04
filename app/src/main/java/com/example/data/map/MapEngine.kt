package com.example.data.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.db.AutoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.*

data class ActiveRoute(
    val destinationName: String,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val waypoints: List<Pair<Double, Double>>,
    val streetSegments: List<String>,
    val maneuverSteps: List<RouteManeuverStep>
)

data class RouteManeuverStep(
    val instruction: String,
    val streetName: String,
    val turnAction: TurnAction,
    val waypointIndex: Int
)

class MapEngine(
    private val context: Context,
    private val repository: AutoRepository
) : LocationListener, TextToSpeech.OnInitListener {

    val cityPresets = listOf(
        CityPreset(
            id = "ankara",
            name = "Ankara",
            province = "İç Anadolu",
            lat = 39.9208,
            lng = 32.8541,
            defaultStreet = "Atatürk Bulvarı & Kızılay",
            popularDestinations = listOf("Kızılay Meydanı", "Anıtkabir & Çankaya", "Tunalı Hilmi Caddesi", "Esenboğa Havalimanı", "Bilkent & Çayyolu", "Armada & Söğütözü")
        ),
        CityPreset(
            id = "izmir",
            name = "İzmir",
            province = "Ege",
            lat = 38.4192,
            lng = 27.1287,
            defaultStreet = "Mustafa Kemal Sahil Bulvarı & Konak",
            popularDestinations = listOf("Alsancak Kordon & Pasaport", "Konak Meydanı & Saat Kulesi", "Karşıyaka Çarşı & Sahil", "Adnan Menderes Havalimanı", "Bornova Forum", "Urla & Çeşme Otoyolu")
        ),
        CityPreset(
            id = "bursa",
            name = "Bursa",
            province = "Marmara",
            lat = 40.1885,
            lng = 29.0610,
            defaultStreet = "Fevzi Çakmak Caddesi & Heykel",
            popularDestinations = listOf("Heykel & Ulucami", "Fatih Sultan Mehmet (FSM) Bulvarı", "Nilüfer & Görükle", "Mudanya Sahil Yolu", "Uludağ Milli Parkı", "Yenişehir Havalimanı")
        ),
        CityPreset(
            id = "antalya",
            name = "Antalya",
            province = "Akdeniz",
            lat = 36.8841,
            lng = 30.7056,
            defaultStreet = "Konyaaltı Sahil Caddesi & Akdeniz Blv",
            popularDestinations = listOf("Kaleiçi & Marina", "Konyaaltı Kent Meydanı", "Lara & Düden Şelalesi", "Antalya Havalimanı (AYT)", "Kemer Sahil Yolu", "Belek & Manavgat")
        ),
        CityPreset(
            id = "adana",
            name = "Adana",
            province = "Akdeniz",
            lat = 37.0000,
            lng = 35.3213,
            defaultStreet = "Ziyapaşa Bulvarı & Seyhan",
            popularDestinations = listOf("Ziyapaşa & Gazipaşa", "Seyhan Baraj Gölü", "Çukurova Üniversitesi", "Şakirpaşa Havalimanı", "Taşköprü & Sabancı Camii")
        ),
        CityPreset(
            id = "konya",
            name = "Konya",
            province = "İç Anadolu",
            lat = 37.8746,
            lng = 32.4932,
            defaultStreet = "Mevlana Caddesi & Alaaddin Tepesi",
            popularDestinations = listOf("Mevlana Türbesi & Meydanı", "Alaaddin Tepesi", "Nalçacı Caddesi", "Selçuklu Kongre Merkezi", "Konya Havalimanı")
        ),
        CityPreset(
            id = "gaziantep",
            name = "Gaziantep",
            province = "Güneydoğu Anadolu",
            lat = 37.0662,
            lng = 37.3833,
            defaultStreet = "İpekyolu Bulvarı & Şehitkamil",
            popularDestinations = listOf("Gaziantep Kalesi & Bakırcılar", "Şehitkamil Kültür Merkezi", "Gaziantep Üniversitesi", "Gaziantep Havalimanı (GZT)", "Sankopark AVM")
        ),
        CityPreset(
            id = "kocaeli",
            name = "Kocaeli / İzmit",
            province = "Marmara",
            lat = 40.7654,
            lng = 29.9408,
            defaultStreet = "Yürüyüş Yolu & D-100 Körfez Yolu",
            popularDestinations = listOf("İzmit Saat Kulesi", "Yahya Kaptan & Arasta Park", "Seka Park & Sahil", "Kartepe Kayak Merkezi", "Gebze & Bilişim Vadisi")
        ),
        CityPreset(
            id = "mersin",
            name = "Mersin",
            province = "Akdeniz",
            lat = 36.8121,
            lng = 34.6415,
            defaultStreet = "İsmet İnönü Bulvarı & Pozcu",
            popularDestinations = listOf("Mersin Marina & Sahil", "Pozcu Kushimoto Sokağı", "Forum Mersin", "Mezitli Sahil Yolu", "Tarsus Şelalesi")
        ),
        CityPreset(
            id = "diyarbakir",
            name = "Diyarbakır",
            province = "Güneydoğu Anadolu",
            lat = 37.9144,
            lng = 40.2110,
            defaultStreet = "Şanlıurfa Bulvarı & Diclekent",
            popularDestinations = listOf("Sur İçi & Ulu Cami", "Diclekent Bulvarı", "Ceylan Karavil AVM", "Diyarbakır Havalimanı", "On Gözlü Köprü")
        ),
        CityPreset(
            id = "kayseri",
            name = "Kayseri",
            province = "İç Anadolu",
            lat = 38.7205,
            lng = 35.4826,
            defaultStreet = "Sivas Caddesi & Cumhuriyet Meydanı",
            popularDestinations = listOf("Cumhuriyet Meydanı", "Erciyes Kayak Merkezi", "Talas Tarihi Sokaklar", "Kayseri Havalimanı (ASR)", "Kayseri Park")
        ),
        CityPreset(
            id = "eskisehir",
            name = "Eskişehir",
            province = "İç Anadolu",
            lat = 39.7767,
            lng = 30.5206,
            defaultStreet = "İsmet İnönü Caddesi (Doktorlar) & Porsuk",
            popularDestinations = listOf("Porsuk Çayı & Adalar", "Odunpazarı Tarihi Evleri", "Sazova Bilim Kültür Parkı", "Espark AVM & Üniversite", "Kentpark Sahil")
        ),
        CityPreset(
            id = "samsun",
            name = "Samsun",
            province = "Karadeniz",
            lat = 41.2867,
            lng = 36.3300,
            defaultStreet = "Atatürk Bulvarı & Sahil Yolu",
            popularDestinations = listOf("Atakum Sahil & Marina", "Cumhuriyet Meydanı", "Bandırma Vapuru Müzesi", "Samsun Çarşamba Havalimanı", "Piazza AVM")
        ),
        CityPreset(
            id = "trabzon",
            name = "Trabzon",
            province = "Karadeniz",
            lat = 41.0027,
            lng = 39.7168,
            defaultStreet = "Kahramanmaraş Caddesi & Meydan",
            popularDestinations = listOf("Meydan Parkı & Uzun Sokak", "Ayasofya & Sahil Yolu", "Boztepe Seyir Terası", "Trabzon Havalimanı (TZX)", "Uzungöl & Maçka")
        ),
        CityPreset(
            id = "denizli",
            name = "Denizli",
            province = "Ege",
            lat = 37.7765,
            lng = 29.0864,
            defaultStreet = "Gazi Mustafa Kemal Bulvarı & Çamlık",
            popularDestinations = listOf("Çamlık Parkı & Bulvarı", "Bayramyeri Meydanı", "Pamukkale Travertenleri", "Forum Çamlık", "Denizli Teleferik")
        ),
        CityPreset(
            id = "sanliurfa",
            name = "Şanlıurfa",
            province = "Güneydoğu Anadolu",
            lat = 37.1674,
            lng = 38.7955,
            defaultStreet = "Atatürk Bulvarı & Balıklıgöl Yolu",
            popularDestinations = listOf("Balıklıgöl & Dergah", "Göbeklitepe Ören Yeri", "Karaköprü & Fuar Alanı", "GAP Havalimanı", "Piazza AVM")
        ),
        CityPreset(
            id = "mugla",
            name = "Muğla / Bodrum / Fethiye",
            province = "Ege",
            lat = 37.0344,
            lng = 27.4305,
            defaultStreet = "Neyzen Tevfik Caddesi & Bodrum Marina",
            popularDestinations = listOf("Bodrum Marina & Kalesi", "Fethiye Kordon & Ölüdeniz", "Marmaris Sahil Kordonu", "Milas-Bodrum Havalimanı (BJV)", "Dalaman Havalimanı (DLM)")
        ),
        CityPreset(
            id = "istanbul",
            name = "İstanbul",
            province = "Marmara",
            lat = 41.0082,
            lng = 28.9784,
            defaultStreet = "Kennedy Caddesi (Sahil Yolu)",
            popularDestinations = listOf("Kadıköy Rıhtım & Moda Sahili", "Beşiktaş Meydan & Barbaros Bulvarı", "Levent 199 Finans Kuleleri", "İstanbul Havalimanı (IST)", "Sabiha Gökçen Havalimanı (SAW)", "Maslak & Zincirlikuyu")
        )
    )

    private val _locationSourceMode = MutableStateFlow(LocationSourceMode.REAL_GPS)
    val locationSourceMode: StateFlow<LocationSourceMode> = _locationSourceMode.asStateFlow()

    private val _selectedCity = MutableStateFlow(cityPresets.first())

    private val _currentLocation = MutableStateFlow(
        MapLocation(
            lat = 39.9208,
            lng = 32.8541,
            heading = 0f,
            speedKmh = 0f,
            speedLimitKmh = 50,
            cityName = "Canlı GPS Bekleniyor",
            districtName = "Konum Tespiti",
            streetName = "GPS Uydusu Aranıyor...",
            nextStreetName = "Rota Belirlenmedi",
            distanceToNextTurnMeters = 0,
            nextTurnAction = TurnAction.STRAIGHT,
            isRealGpsFix = false
        )
    )
    val currentLocation: StateFlow<MapLocation> = _currentLocation.asStateFlow()

    private val _orientationMode = MutableStateFlow(MapOrientationMode.COURSE_UP)
    val orientationMode: StateFlow<MapOrientationMode> = _orientationMode.asStateFlow()

    private val _zoomLevel = MutableStateFlow(16.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _panOffsetX = MutableStateFlow(0f)
    val panOffsetX: StateFlow<Float> = _panOffsetX.asStateFlow()

    private val _panOffsetY = MutableStateFlow(0f)
    val panOffsetY: StateFlow<Float> = _panOffsetY.asStateFlow()

    private val _isUserPanning = MutableStateFlow(false)
    val isUserPanning: StateFlow<Boolean> = _isUserPanning.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isTrafficVisible = MutableStateFlow(true)
    val isTrafficVisible: StateFlow<Boolean> = _isTrafficVisible.asStateFlow()

    private val _mapTheme = MutableStateFlow(MapStyleTheme.NIGHT_STEALTH_CYAN)
    val mapTheme: StateFlow<MapStyleTheme> = _mapTheme.asStateFlow()

    private val _cachedPacks = MutableStateFlow<List<OfflineRegionPack>>(emptyList())
    val cachedPacks: StateFlow<List<OfflineRegionPack>> = _cachedPacks.asStateFlow()

    private val _activeRoute = MutableStateFlow<ActiveRoute?>(null)
    val activeRoute: StateFlow<ActiveRoute?> = _activeRoute.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private var locationManager: LocationManager? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var geocodeJob: Job? = null
    private var gpsPollingJob: Job? = null
    private var lastSpokenTurnIndex = -1

    init {
        _cachedPacks.value = listOf(
            OfflineRegionPack("icanadolu", "İç Anadolu & Ankara Çevresi", 280, 1150, true),
            OfflineRegionPack("ege", "Ege & İzmir Sahil Yolu", 340, 1420, false),
            OfflineRegionPack("akdeniz", "Akdeniz & Antalya - Muğla", 310, 1300, false),
            OfflineRegionPack("marmara", "Marmara & İstanbul - Bursa", 420, 1850, false)
        )

        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("MapEngine", "TTS init error", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("tr", "TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isTtsReady = true
        }
    }

    private fun speakGuidance(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "NAV_GUIDANCE")
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        startRealGpsListener()
        fetchInitialLastKnownLocation()
        fetchIpBasedInitialLocation()
        startGpsWatcherJob()
    }

    private fun fetchIpBasedInitialLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url("http://ip-api.com/json")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        if (json.optString("status") == "success") {
                            val lat = json.optDouble("lat")
                            val lon = json.optDouble("lon")
                            val city = json.optString("city")
                            val region = json.optString("regionName")

                            // Only use IP location if we haven't received a real GPS fix yet
                            if (!_currentLocation.value.isRealGpsFix && lat != 0.0 && lon != 0.0) {
                                withContext(Dispatchers.Main) {
                                    _currentLocation.value = _currentLocation.value.copy(
                                        lat = lat,
                                        lng = lon,
                                        cityName = city.ifEmpty { "Canlı Konum" },
                                        districtName = region,
                                        streetName = "$city, $region"
                                    )
                                    reverseGeocodeCoordinates(lat, lon)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("MapEngine", "IP location fetch fallback: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLastKnownLocation() {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.let { lm ->
                val gpsLoc = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
                val netLoc = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
                val passLoc = try { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) { null }

                val bestLoc = listOfNotNull(gpsLoc, netLoc, passLoc).maxByOrNull { it.time }
                if (bestLoc != null) {
                    processRealLocationUpdate(bestLoc)
                }
            }
        } catch (e: Exception) {
            Log.w("MapEngine", "Could not fetch last known location: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startRealGpsListener() {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.let { lm ->
                val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGpsEnabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 0.5f, this)
                }
                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1.0f, this)
                }
                try {
                    lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000L, 1.0f, this)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w("MapEngine", "Location permissions or service unavailable: ${e.message}")
        }
    }

    private fun startGpsWatcherJob() {
        gpsPollingJob?.cancel()
        gpsPollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (_locationSourceMode.value == LocationSourceMode.REAL_GPS && !_currentLocation.value.isRealGpsFix) {
                    fetchInitialLastKnownLocation()
                }
                delay(3000)
            }
        }
    }

    fun stop() {
        gpsPollingJob?.cancel()
        geocodeJob?.cancel()
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            // ignore
        }
        tts?.stop()
        tts?.shutdown()
    }

    fun enableRealGps() {
        _locationSourceMode.value = LocationSourceMode.REAL_GPS
        fetchInitialLastKnownLocation()
        startRealGpsListener()
        speakGuidance("Canlı GPS konumu devrede.")
    }

    fun selectCity(city: CityPreset) {
        _selectedCity.value = city
        _locationSourceMode.value = LocationSourceMode.MANUAL_CITY
        _currentLocation.value = _currentLocation.value.copy(
            lat = city.lat,
            lng = city.lng,
            cityName = city.name,
            districtName = city.province,
            streetName = city.defaultStreet,
            nextStreetName = city.popularDestinations.firstOrNull() ?: "Şehir Merkezi",
            speedKmh = 0f,
            isRealGpsFix = false
        )
        val dest = city.popularDestinations.firstOrNull() ?: "Merkez"
        calculateAndStartRoute(dest)
        speakGuidance("${city.name} seçildi. Konum: ${city.defaultStreet}")
    }

    fun setCustomLocation(name: String, lat: Double, lng: Double) {
        _locationSourceMode.value = LocationSourceMode.MANUAL_CITY
        _currentLocation.value = _currentLocation.value.copy(
            lat = lat,
            lng = lng,
            cityName = name,
            districtName = "Konum",
            streetName = name,
            nextStreetName = "Rota Hedefi",
            speedKmh = 0f,
            isRealGpsFix = false
        )
        reverseGeocodeCoordinates(lat, lng)
    }

    private fun processRealLocationUpdate(location: Location) {
        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f).coerceAtLeast(0f) else _currentLocation.value.speedKmh
        val heading = if (location.hasBearing()) location.bearing else _currentLocation.value.heading

        // Update real position without running fake simulation
        _currentLocation.value = _currentLocation.value.copy(
            lat = location.latitude,
            lng = location.longitude,
            heading = heading,
            speedKmh = speedKmh,
            altitudeMeters = location.altitude,
            isRealGpsFix = true,
            gpsAccuracyMeters = location.accuracy
        )

        // If a route is active, calculate distance and upcoming turn from real location
        val route = _activeRoute.value
        if (route != null && route.waypoints.isNotEmpty()) {
            updateRouteGuidanceForRealLocation(location.latitude, location.longitude, route)
        }

        // Trigger reverse geocoding to resolve street name
        reverseGeocodeCoordinates(location.latitude, location.longitude)
    }

    private fun updateRouteGuidanceForRealLocation(curLat: Double, curLng: Double, route: ActiveRoute) {
        val waypoints = route.waypoints
        var nearestIdx = 0
        var minDistance = Float.MAX_VALUE

        val results = FloatArray(1)
        for (i in waypoints.indices) {
            val wp = waypoints[i]
            Location.distanceBetween(curLat, curLng, wp.first, wp.second, results)
            if (results[0] < minDistance) {
                minDistance = results[0]
                nearestIdx = i
            }
        }

        val nextIdx = (nearestIdx + 1).coerceAtMost(waypoints.size - 1)
        val nextWp = waypoints[nextIdx]
        Location.distanceBetween(curLat, curLng, nextWp.first, nextWp.second, results)
        val distToTurn = results[0].toInt().coerceAtLeast(10)

        val step = route.maneuverSteps.getOrElse(nextIdx) {
            RouteManeuverStep("Düz devam edin", route.destinationName, TurnAction.STRAIGHT, nextIdx)
        }
        val nextStreet = route.streetSegments.getOrElse(nextIdx) { route.destinationName }

        _currentLocation.value = _currentLocation.value.copy(
            nextStreetName = nextStreet,
            distanceToNextTurnMeters = distToTurn,
            nextTurnAction = step.turnAction
        )

        // Announce turn once approaching
        if (distToTurn in 80..160 && lastSpokenTurnIndex != nextIdx) {
            lastSpokenTurnIndex = nextIdx
            speakGuidance("${distToTurn} metre sonra: ${step.instruction}")
        }
    }

    private fun reverseGeocodeCoordinates(lat: Double, lng: Double) {
        geocodeJob?.cancel()
        geocodeJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale("tr", "TR"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            handleAddressResult(addresses.firstOrNull(), lat, lng)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        handleAddressResult(addresses?.firstOrNull(), lat, lng)
                    }
                } else {
                    fallbackCityDetermination(lat, lng)
                }
            } catch (e: Exception) {
                Log.w("MapEngine", "Reverse geocode error: ${e.message}")
                fallbackCityDetermination(lat, lng)
            }
        }
    }

    private fun handleAddressResult(address: Address?, lat: Double, lng: Double) {
        if (address != null) {
            val street = address.thoroughfare ?: address.subThoroughfare ?: address.featureName ?: "Ana Arter"
            val district = address.subAdminArea ?: address.subLocality ?: address.locality ?: ""
            val city = address.adminArea ?: address.locality ?: "Türkiye"

            _currentLocation.value = _currentLocation.value.copy(
                streetName = if (district.isNotEmpty()) "$street, $district" else street,
                districtName = district,
                cityName = city
            )
        } else {
            fallbackCityDetermination(lat, lng)
        }
    }

    private fun fallbackCityDetermination(lat: Double, lng: Double) {
        val nearest = cityPresets.minByOrNull { city ->
            val dLat = city.lat - lat
            val dLng = city.lng - lng
            dLat * dLat + dLng * dLng
        }
        if (nearest != null) {
            _currentLocation.value = _currentLocation.value.copy(
                cityName = nearest.name,
                districtName = nearest.province,
                streetName = nearest.defaultStreet
            )
        }
    }

    fun calculateAndStartRoute(destination: String, targetLat: Double? = null, targetLng: Double? = null) {
        val curLat = _currentLocation.value.lat
        val curLng = _currentLocation.value.lng
        val curStreet = _currentLocation.value.streetName

        // Initial provisional route
        val defaultDestLat = targetLat ?: (curLat + 0.035)
        val defaultDestLng = targetLng ?: (curLng + 0.024)

        val provisionalWaypoints = listOf(
            Pair(curLat, curLng),
            Pair(curLat + (defaultDestLat - curLat) * 0.25, curLng + (defaultDestLng - curLng) * 0.25),
            Pair(curLat + (defaultDestLat - curLat) * 0.50, curLng + (defaultDestLng - curLng) * 0.50),
            Pair(curLat + (defaultDestLat - curLat) * 0.75, curLng + (defaultDestLng - curLng) * 0.75),
            Pair(defaultDestLat, defaultDestLng)
        )

        val streetNames = listOf(
            curStreet,
            "Şehirlerarası Bağlantı Bulvarı",
            "Çevre Yolu & Kavşak Girişi",
            "Ana Arter Katılımı",
            destination
        )

        val steps = listOf(
            RouteManeuverStep("$curStreet boyunca 350 m ilerleyin", curStreet, TurnAction.STRAIGHT, 0),
            RouteManeuverStep("Kavşaktan sağa dönün", "Şehirlerarası Bağlantı Bulvarı", TurnAction.TURN_RIGHT, 1),
            RouteManeuverStep("Çevre Yolu yönünde düz devam edin", "Çevre Yolu & Kavşak", TurnAction.STRAIGHT, 2),
            RouteManeuverStep("Ayrım noktasından hafif sola girin", "Ana Arter Katılımı", TurnAction.SLIGHT_LEFT, 3),
            RouteManeuverStep("Hedefinize ulaştınız: $destination", destination, TurnAction.DESTINATION, 4)
        )

        _activeRoute.value = ActiveRoute(
            destinationName = destination,
            totalDistanceKm = 11.8,
            estimatedDurationMinutes = 15,
            waypoints = provisionalWaypoints,
            streetSegments = streetNames,
            maneuverSteps = steps
        )

        lastSpokenTurnIndex = -1
        _isNavigating.value = true

        _currentLocation.value = _currentLocation.value.copy(
            nextStreetName = streetNames[1],
            distanceToNextTurnMeters = 350,
            nextTurnAction = steps[0].turnAction
        )

        speakGuidance("$destination için rota hesaplanıyor.")

        // Asynchronously fetch real road geometry from OSRM
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var destCoordinateLat = defaultDestLat
                var destCoordinateLng = defaultDestLng

                // Try geocoding destination name if target coordinates were not passed
                if (targetLat == null || targetLng == null) {
                    try {
                        if (Geocoder.isPresent()) {
                            val geocoder = Geocoder(context, Locale("tr", "TR"))
                            @Suppress("DEPRECATION")
                            val results = geocoder.getFromLocationName("$destination, ${_currentLocation.value.cityName}", 1)
                            if (!results.isNullOrEmpty()) {
                                destCoordinateLat = results[0].latitude
                                destCoordinateLng = results[0].longitude
                            }
                        }
                    } catch (_: Exception) {}
                }

                val osrmUrl = "https://router.project-osrm.org/route/v1/driving/$curLng,$curLat;$destCoordinateLng,$destCoordinateLat?overview=full&geometries=geojson&steps=true"
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder().url(osrmUrl).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = org.json.JSONObject(body)
                        val routes = json.optJSONArray("routes")
                        if (routes != null && routes.length() > 0) {
                            val route0 = routes.getJSONObject(0)
                            val distanceM = route0.optDouble("distance", 12000.0)
                            val durationS = route0.optDouble("duration", 900.0)
                            val geometry = route0.optJSONObject("geometry")
                            val coordinates = geometry?.optJSONArray("coordinates")

                            val realWaypoints = mutableListOf<Pair<Double, Double>>()
                            if (coordinates != null) {
                                for (i in 0 until coordinates.length()) {
                                    val point = coordinates.getJSONArray(i)
                                    val lng = point.getDouble(0)
                                    val lat = point.getDouble(1)
                                    realWaypoints.add(Pair(lat, lng))
                                }
                            }

                            if (realWaypoints.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    _activeRoute.value = _activeRoute.value?.copy(
                                        totalDistanceKm = Math.round((distanceM / 1000.0) * 10.0) / 10.0,
                                        estimatedDurationMinutes = (durationS / 60.0).toInt().coerceAtLeast(1),
                                        waypoints = realWaypoints
                                    )
                                    speakGuidance("$destination için en hızlı yol bulundu. Rota başlatıldı.")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("MapEngine", "OSRM Route fetch fallback: ${e.message}")
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        processRealLocationUpdate(location)
    }

    override fun onProviderEnabled(provider: String) {
        fetchInitialLastKnownLocation()
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    fun panMap(deltaX: Float, deltaY: Float) {
        _isUserPanning.value = true
        _panOffsetX.value += deltaX
        _panOffsetY.value += deltaY
    }

    fun recenterMap() {
        _isUserPanning.value = false
        _panOffsetX.value = 0f
        _panOffsetY.value = 0f
    }

    fun toggleOrientationMode() {
        _orientationMode.value = if (_orientationMode.value == MapOrientationMode.COURSE_UP) {
            MapOrientationMode.NORTH_UP
        } else {
            MapOrientationMode.COURSE_UP
        }
    }

    fun toggleTraffic() {
        _isTrafficVisible.value = !_isTrafficVisible.value
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun setMapTheme(theme: MapStyleTheme) {
        _mapTheme.value = theme
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 0.5f).coerceAtMost(19f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 0.5f).coerceAtLeast(11f)
    }

    fun downloadPack(packId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _cachedPacks.value = _cachedPacks.value.map { pack ->
                if (pack.id == packId) pack.copy(downloadProgress = 0.1f) else pack
            }
            for (p in 2..10) {
                delay(150)
                _cachedPacks.value = _cachedPacks.value.map { pack ->
                    if (pack.id == packId) pack.copy(downloadProgress = p / 10f) else pack
                }
            }
            _cachedPacks.value = _cachedPacks.value.map { pack ->
                if (pack.id == packId) pack.copy(isDownloaded = true, downloadProgress = 1f) else pack
            }
            repository.downloadRegionPack(packId, 50)
        }
    }

    fun clearCache() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.clearMapCache()
            _cachedPacks.value = _cachedPacks.value.map { it.copy(isDownloaded = false) }
        }
    }
}
