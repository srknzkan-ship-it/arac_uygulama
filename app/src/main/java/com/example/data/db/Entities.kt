package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_tiles")
data class MapTileEntity(
    @PrimaryKey val tileKey: String, // e.g. "14_9450_6230"
    val zoom: Int,
    val x: Int,
    val y: Int,
    val regionName: String,
    val roadsJson: String,
    val poisJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_routes")
data class OfflineRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeName: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val stepsJson: String,
    val pointsJson: String,
    val trafficLevel: String = "NORMAL", // NORMAL, MODERATE, HEAVY
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_pois")
data class OfflinePoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // GAS, CHARGER, SPEED_CAM, PARKING, SERVICE
    val lat: Double,
    val lng: Double,
    val details: String = "",
    val speedLimit: Int = 0
)

@Entity(tableName = "radio_stations")
data class RadioStationEntity(
    @PrimaryKey val frequency: Double, // e.g. 100.0
    val name: String,
    val genre: String,
    val isFavorite: Boolean = false,
    val streamUrl: String = ""
)

@Entity(tableName = "obd_trip_logs")
data class TripLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val avgFuelL100km: Double,
    val maxRpm: Int,
    val notes: String = ""
)

@Entity(tableName = "dtc_error_codes")
data class DtcCodeEntity(
    @PrimaryKey val code: String, // e.g. "P0300"
    val system: String, // Engine, Transmission, Electrical, Exhaust
    val description: String,
    val severity: String, // LOW, MEDIUM, CRITICAL
    val detectedAt: Long = System.currentTimeMillis(),
    val isCleared: Boolean = false
)
