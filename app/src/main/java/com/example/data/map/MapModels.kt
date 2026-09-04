package com.example.data.map

data class MapLocation(
    val lat: Double = 41.0082,
    val lng: Double = 28.9784,
    val heading: Float = 42f, // Degrees (0-360)
    val speedKmh: Float = 62f,
    val altitudeMeters: Double = 65.0,
    val cityName: String = "İstanbul",
    val districtName: String = "Fatih",
    val streetName: String = "Kennedy Caddesi",
    val nextStreetName: String = "Galata Köprüsü Yönü",
    val distanceToNextTurnMeters: Int = 380,
    val nextTurnAction: TurnAction = TurnAction.SLIGHT_RIGHT,
    val speedLimitKmh: Int = 50,
    val isRealGpsFix: Boolean = false,
    val gpsAccuracyMeters: Float = 0f
)

data class CityPreset(
    val id: String,
    val name: String,
    val province: String,
    val lat: Double,
    val lng: Double,
    val defaultStreet: String,
    val popularDestinations: List<String>
)

enum class LocationSourceMode {
    REAL_GPS,
    MANUAL_CITY
}

enum class TurnAction {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    ROUNDABOUT,
    U_TURN,
    DESTINATION
}

enum class MapOrientationMode {
    COURSE_UP, // Map rotates so vehicle forward is always UP
    NORTH_UP   // Map is static facing North, vehicle triangle rotates
}

enum class MapStyleTheme {
    AUTO,
    DAY_HIGH_CONTRAST,
    NIGHT_STEALTH_CYAN,
    NIGHT_AMBER_HUD
}

data class TrafficSegment(
    val id: String,
    val roadName: String,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val status: TrafficStatus, // FLOWING, MODERATE, CONGESTED
    val avgSpeed: Int
)

enum class TrafficStatus {
    FLOWING,    // Green / Cyan
    MODERATE,   // Amber / Yellow
    CONGESTED   // Crimson / Red
}

data class VectorRoad(
    val id: String,
    val type: RoadType,
    val points: List<MapPoint>,
    val name: String,
    val traffic: TrafficStatus = TrafficStatus.FLOWING
)

enum class RoadType {
    HIGHWAY,
    PRIMARY_ARTERIAL,
    SECONDARY_STREET,
    BRIDGE_TUNNEL,
    WATER_BODY,
    PARK_GREEN
}

data class MapPoint(
    val x: Float,
    val y: Float
)

data class MapPoiMarker(
    val id: String,
    val name: String,
    val category: String,
    val x: Float,
    val y: Float,
    val iconType: String
)

data class OfflineRegionPack(
    val id: String,
    val name: String,
    val sizeMb: Int,
    val tileCount: Int,
    val isDownloaded: Boolean,
    val downloadProgress: Float = 1f
)
