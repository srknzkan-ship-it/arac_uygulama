package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MapDao {
    @Query("SELECT * FROM cached_tiles WHERE regionName = :region")
    fun getTilesByRegion(region: String): Flow<List<MapTileEntity>>

    @Query("SELECT COUNT(*) FROM cached_tiles")
    fun getCachedTileCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiles(tiles: List<MapTileEntity>)

    @Query("DELETE FROM cached_tiles")
    suspend fun clearTileCache()

    @Query("SELECT * FROM offline_routes ORDER BY lastUpdated DESC")
    fun getAllOfflineRoutes(): Flow<List<OfflineRouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: OfflineRouteEntity)

    @Query("SELECT * FROM offline_pois WHERE category = :category OR :category = 'ALL'")
    fun getPois(category: String): Flow<List<OfflinePoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<OfflinePoiEntity>)
}

@Dao
interface RadioDao {
    @Query("SELECT * FROM radio_stations ORDER BY frequency ASC")
    fun getAllStations(): Flow<List<RadioStationEntity>>

    @Query("SELECT * FROM radio_stations WHERE isFavorite = 1 ORDER BY frequency ASC")
    fun getFavorites(): Flow<List<RadioStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<RadioStationEntity>)

    @Update
    suspend fun updateStation(station: RadioStationEntity)
}

@Dao
interface ObdDao {
    @Query("SELECT * FROM obd_trip_logs ORDER BY startTime DESC LIMIT 20")
    fun getRecentTrips(): Flow<List<TripLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripLogEntity)

    @Query("SELECT * FROM dtc_error_codes WHERE isCleared = 0")
    fun getActiveDtcCodes(): Flow<List<DtcCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDtcCode(code: DtcCodeEntity)

    @Query("UPDATE dtc_error_codes SET isCleared = 1")
    suspend fun clearAllDtcCodes()
}

@Database(
    entities = [
        MapTileEntity::class,
        OfflineRouteEntity::class,
        OfflinePoiEntity::class,
        RadioStationEntity::class,
        TripLogEntity::class,
        DtcCodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao
    abstract fun radioDao(): RadioDao
    abstract fun obdDao(): ObdDao
}
