package com.example.data.db

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface MapDao {
    fun getTilesByRegion(region: String): Flow<List<MapTileEntity>>
    fun getCachedTileCount(): Flow<Int>
    suspend fun insertTiles(tiles: List<MapTileEntity>)
    suspend fun clearTileCache()
    fun getAllOfflineRoutes(): Flow<List<OfflineRouteEntity>>
    suspend fun insertRoute(route: OfflineRouteEntity)
    fun getPois(category: String): Flow<List<OfflinePoiEntity>>
    suspend fun insertPois(pois: List<OfflinePoiEntity>)
}

interface RadioDao {
    fun getAllStations(): Flow<List<RadioStationEntity>>
    fun getFavorites(): Flow<List<RadioStationEntity>>
    suspend fun insertStations(stations: List<RadioStationEntity>)
    suspend fun updateStation(station: RadioStationEntity)
}

interface ObdDao {
    fun getRecentTrips(): Flow<List<TripLogEntity>>
    suspend fun insertTrip(trip: TripLogEntity)
    fun getActiveDtcCodes(): Flow<List<DtcCodeEntity>>
    suspend fun insertDtcCode(code: DtcCodeEntity)
    suspend fun clearAllDtcCodes()
}

abstract class AppDatabase {
    abstract fun mapDao(): MapDao
    abstract fun radioDao(): RadioDao
    abstract fun obdDao(): ObdDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabaseImpl(context.applicationContext)
                INSTANCE = db
                db
            }
        }
    }
}

class AppDatabaseImpl(private val context: Context) : AppDatabase() {

    private val mapDaoImpl = object : MapDao {
        private val tilesFlow = MutableStateFlow<List<MapTileEntity>>(emptyList())
        private val routesFlow = MutableStateFlow<List<OfflineRouteEntity>>(emptyList())
        private val poisFlow = MutableStateFlow<List<OfflinePoiEntity>>(emptyList())

        override fun getTilesByRegion(region: String): Flow<List<MapTileEntity>> {
            return tilesFlow.map { list -> list.filter { it.regionName == region } }
        }

        override fun getCachedTileCount(): Flow<Int> {
            return tilesFlow.map { it.size }
        }

        override suspend fun insertTiles(tiles: List<MapTileEntity>) {
            val currentMap = tilesFlow.value.associateBy { it.tileKey }.toMutableMap()
            tiles.forEach { currentMap[it.tileKey] = it }
            tilesFlow.value = currentMap.values.toList()
        }

        override suspend fun clearTileCache() {
            tilesFlow.value = emptyList()
        }

        override fun getAllOfflineRoutes(): Flow<List<OfflineRouteEntity>> {
            return routesFlow.asStateFlow()
        }

        override suspend fun insertRoute(route: OfflineRouteEntity) {
            routesFlow.value = listOf(route) + routesFlow.value
        }

        override fun getPois(category: String): Flow<List<OfflinePoiEntity>> {
            return poisFlow.map { list ->
                if (category == "ALL") list else list.filter { it.category == category }
            }
        }

        override suspend fun insertPois(pois: List<OfflinePoiEntity>) {
            val currentMap = poisFlow.value.associateBy { it.name }.toMutableMap()
            pois.forEach { currentMap[it.name] = it }
            poisFlow.value = currentMap.values.toList()
        }
    }

    private val radioDaoImpl = object : RadioDao {
        private val stationsFlow = MutableStateFlow<List<RadioStationEntity>>(emptyList())

        override fun getAllStations(): Flow<List<RadioStationEntity>> {
            return stationsFlow.map { list -> list.sortedBy { it.frequency } }
        }

        override fun getFavorites(): Flow<List<RadioStationEntity>> {
            return stationsFlow.map { list -> list.filter { it.isFavorite }.sortedBy { it.frequency } }
        }

        override suspend fun insertStations(stations: List<RadioStationEntity>) {
            val currentMap = stationsFlow.value.associateBy { it.frequency }.toMutableMap()
            stations.forEach { currentMap[it.frequency] = it }
            stationsFlow.value = currentMap.values.sortedBy { it.frequency }
        }

        override suspend fun updateStation(station: RadioStationEntity) {
            val current = stationsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.frequency == station.frequency }
            if (index >= 0) {
                current[index] = station
            } else {
                current.add(station)
            }
            stationsFlow.value = current.sortedBy { it.frequency }
        }
    }

    private val obdDaoImpl = object : ObdDao {
        private val tripsFlow = MutableStateFlow<List<TripLogEntity>>(emptyList())
        private val dtcCodesFlow = MutableStateFlow<List<DtcCodeEntity>>(emptyList())

        override fun getRecentTrips(): Flow<List<TripLogEntity>> {
            return tripsFlow.asStateFlow()
        }

        override suspend fun insertTrip(trip: TripLogEntity) {
            val updated = (listOf(trip) + tripsFlow.value).take(20)
            tripsFlow.value = updated
        }

        override fun getActiveDtcCodes(): Flow<List<DtcCodeEntity>> {
            return dtcCodesFlow.map { list -> list.filter { !it.isCleared } }
        }

        override suspend fun insertDtcCode(code: DtcCodeEntity) {
            val current = dtcCodesFlow.value.toMutableList()
            val index = current.indexOfFirst { it.code == code.code }
            if (index >= 0) {
                current[index] = code
            } else {
                current.add(code)
            }
            dtcCodesFlow.value = current
        }

        override suspend fun clearAllDtcCodes() {
            dtcCodesFlow.value = dtcCodesFlow.value.map { it.copy(isCleared = true) }
        }
    }

    override fun mapDao(): MapDao = mapDaoImpl
    override fun radioDao(): RadioDao = radioDaoImpl
    override fun obdDao(): ObdDao = obdDaoImpl
}

