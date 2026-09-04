package com.example.data.db

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AutoRepository(private val db: AppDatabase) {

    val allOfflineRoutes: Flow<List<OfflineRouteEntity>> = db.mapDao().getAllOfflineRoutes()
    val cachedTileCount: Flow<Int> = db.mapDao().getCachedTileCount()
    val allRadioStations: Flow<List<RadioStationEntity>> = db.radioDao().getAllStations()
    val activeDtcCodes: Flow<List<DtcCodeEntity>> = db.obdDao().getActiveDtcCodes()
    val recentTrips: Flow<List<TripLogEntity>> = db.obdDao().getRecentTrips()

    fun getPois(category: String): Flow<List<OfflinePoiEntity>> = db.mapDao().getPois(category)

    suspend fun insertInitialData() {
        // Initial radio presets
        val defaultStations = listOf(
            RadioStationEntity(100.0, "Power FM", "Hits / Pop", isFavorite = true),
            RadioStationEntity(101.4, "Virgin Radio", "Rock / Pop", isFavorite = true),
            RadioStationEntity(94.7, "Kral Pop", "Türkçe Pop", isFavorite = true),
            RadioStationEntity(100.4, "Radyo Fenomen", "Dance / Hits", isFavorite = true),
            RadioStationEntity(89.0, "Joy FM", "Easy Listening", isFavorite = false),
            RadioStationEntity(99.0, "Metro FM", "Foreign Pop", isFavorite = false),
            RadioStationEntity(95.6, "TRT Radyo 1", "News / Culture", isFavorite = false),
            RadioStationEntity(103.8, "Radyo D", "Türkçe Pop", isFavorite = false)
        )
        db.radioDao().insertStations(defaultStations)

        // Initial offline POIs
        val defaultPois = listOf(
            OfflinePoiEntity(name = "Opet Akaryakıt & Şarj", category = "GAS", lat = 41.015, lng = 28.979, details = "LPG, Dizel, 150kW DC Hızlı Şarj"),
            OfflinePoiEntity(name = "Shell V-Power & Otogaz", category = "GAS", lat = 41.025, lng = 28.989, details = "Market, Oto Yıkama"),
            OfflinePoiEntity(name = "ZES 180kW Ultra Hızlı Şarj", category = "CHARGER", lat = 41.010, lng = 28.960, details = "CCS2 Tip 2, 2 Soket"),
            OfflinePoiEntity(name = "EDS Hız Koridoru (80 km/h)", category = "SPEED_CAM", lat = 41.030, lng = 28.995, speedLimit = 80, details = "Aktif Radar Kamerası"),
            OfflinePoiEntity(name = "İspark Katlı Otopark", category = "PARKING", lat = 41.018, lng = 28.972, details = "450 Araç Kapasitesi, Boş: 124")
        )
        db.mapDao().insertPois(defaultPois)

        // Initial default tile cache pack for Marmara / Urban Navigation
        val defaultTiles = (1..24).map { idx ->
            MapTileEntity(
                tileKey = "tile_marmara_$idx",
                zoom = 14,
                x = 9450 + idx,
                y = 6230 + idx,
                regionName = "Marmara Bölgesi",
                roadsJson = "[]",
                poisJson = "[]"
            )
        }
        db.mapDao().insertTiles(defaultTiles)
    }

    suspend fun updateRadioFavorite(station: RadioStationEntity) {
        db.radioDao().updateStation(station.copy(isFavorite = !station.isFavorite))
    }

    suspend fun clearDtcCodes() {
        db.obdDao().clearAllDtcCodes()
    }

    suspend fun saveTrip(trip: TripLogEntity) {
        db.obdDao().insertTrip(trip)
    }

    suspend fun saveDtcCode(code: DtcCodeEntity) {
        db.obdDao().insertDtcCode(code)
    }

    suspend fun clearMapCache() {
        db.mapDao().clearTileCache()
    }

    suspend fun downloadRegionPack(regionName: String, count: Int) {
        val newTiles = (1..count).map { idx ->
            MapTileEntity(
                tileKey = "${regionName.lowercase().replace(" ", "_")}_$idx",
                zoom = 14,
                x = 9000 + idx,
                y = 6000 + idx,
                regionName = regionName,
                roadsJson = "[]",
                poisJson = "[]"
            )
        }
        db.mapDao().insertTiles(newTiles)
    }

    companion object {
        @Volatile
        private var INSTANCE: AutoRepository? = null

        fun getInstance(context: Context): AutoRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context.applicationContext)
                val repo = AutoRepository(db)
                INSTANCE = repo
                repo
            }
        }
    }
}
