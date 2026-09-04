package com.example.data.obd

data class ObdTelemetry(
    val speedKmh: Float = 0f,
    val rpm: Int = 800,
    val coolantTempC: Float = 90f,
    val oilTempC: Float = 95f,
    val fuelLevelPct: Float = 68f,
    val batteryVoltage: Float = 14.2f,
    val turboBoostBar: Float = 0.4f,
    val engineLoadPct: Float = 22f,
    val throttlePosPct: Float = 15f,
    val instantL100km: Float = 6.4f,
    val intakeAirTempC: Float = 24f,
    val currentGear: String = "D4",
    val isEngineRunning: Boolean = true,
    val isBluetoothConnected: Boolean = false,
    val connectedDeviceName: String? = null,
    val isSimulated: Boolean = true,
    val errorCodes: List<String> = emptyList(),
    val totalTripDistanceKm: Float = 142.6f
)

data class BluetoothObdDevice(
    val name: String,
    val address: String,
    val isBonded: Boolean
)
