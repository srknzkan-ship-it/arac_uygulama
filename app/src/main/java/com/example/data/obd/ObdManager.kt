package com.example.data.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class ObdManager(private val context: Context) {

    private val _telemetry = MutableStateFlow(ObdTelemetry())
    val telemetry: StateFlow<ObdTelemetry> = _telemetry.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<BluetoothObdDevice>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothObdDevice>> = _availableDevices.asStateFlow()

    private val _scanState = MutableStateFlow("Hazır")
    val scanState: StateFlow<String> = _scanState.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? = try {
        BluetoothAdapter.getDefaultAdapter()
    } catch (e: Exception) {
        null
    }

    private var obdSocket: BluetoothSocket? = null
    private var isRunning = false
    private var job: Job? = null
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Simulation states
    private var targetSpeed = 65f
    private var simTime = 0.0
    private var isSimulatingHardDrive = false

    fun start() {
        if (isRunning) return
        isRunning = true
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                if (_telemetry.value.isBluetoothConnected && obdSocket != null && obdSocket!!.isConnected) {
                    readRealObdData()
                } else {
                    updateSimulatedPhysics()
                }
                delay(100) // 10Hz fast telemetry refresh rate (optimized for energy)
            }
        }
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        disconnect()
    }

    @SuppressLint("MissingPermission")
    fun scanPairedDevices() {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                _scanState.value = "Bluetooth Kapalı"
                _availableDevices.value = listOf(
                    BluetoothObdDevice("OBDII (Simülatör)", "00:11:22:33:44:55", true),
                    BluetoothObdDevice("ELM327 v1.5 Bluetooth", "AA:BB:CC:DD:EE:FF", true),
                    BluetoothObdDevice("Veepeak OBDCheck BLE", "11:22:33:44:55:66", true),
                    BluetoothObdDevice("Vgate iCar Pro OBD", "22:33:44:55:66:77", false)
                )
                return
            }

            val paired = bluetoothAdapter.bondedDevices
            val list = mutableListOf<BluetoothObdDevice>()
            list.add(BluetoothObdDevice("OBDII (Simülatör)", "00:11:22:33:44:55", true))
            paired?.forEach { device ->
                list.add(BluetoothObdDevice(device.name ?: "Bilinmeyen Cihaz", device.address, true))
            }
            _availableDevices.value = list
            _scanState.value = "${list.size} Cihaz Bulundu"
        } catch (e: Exception) {
            _scanState.value = "Tarama Hatası: ${e.message}"
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothObdDevice) {
        if (device.address == "00:11:22:33:44:55") {
            // Simulator connection
            _telemetry.value = _telemetry.value.copy(
                isBluetoothConnected = true,
                connectedDeviceName = "OBDII Simülatör (Aktif)",
                isSimulated = true
            )
            _scanState.value = "Simülatöre Bağlandı"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                _scanState.value = "${device.name} Bağlanıyor..."
                val btDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(device.address)
                if (btDevice == null) {
                    _scanState.value = "Cihaz bulunamadı"
                    return@launch
                }

                obdSocket?.close()
                obdSocket = btDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter.cancelDiscovery()
                obdSocket?.connect()

                if (obdSocket?.isConnected == true) {
                    // Send ELM327 initialization commands
                    val out = obdSocket!!.outputStream
                    out.write("ATZ\r".toByteArray())
                    delay(300)
                    out.write("ATE0\r".toByteArray())
                    delay(100)
                    out.write("ATH0\r".toByteArray())
                    delay(100)
                    out.write("ATSP0\r".toByteArray())
                    delay(100)

                    _telemetry.value = _telemetry.value.copy(
                        isBluetoothConnected = true,
                        connectedDeviceName = device.name,
                        isSimulated = false
                    )
                    _scanState.value = "${device.name} Bağlandı"
                }
            } catch (e: Exception) {
                Log.e("ObdManager", "Connection error", e)
                _scanState.value = "Bağlantı Başarısız: Simülasyon moduna geçildi"
                _telemetry.value = _telemetry.value.copy(
                    isBluetoothConnected = true,
                    connectedDeviceName = "${device.name} (Simüle)",
                    isSimulated = true
                )
            }
        }
    }

    fun disconnect() {
        try {
            obdSocket?.close()
        } catch (e: Exception) {
            // ignore
        }
        obdSocket = null
        _telemetry.value = _telemetry.value.copy(
            isBluetoothConnected = false,
            connectedDeviceName = null,
            isSimulated = true
        )
        _scanState.value = "Bağlantı Kesildi"
    }

    fun triggerDynamicAcceleration(throttle: Float) {
        targetSpeed = min(180f, max(0f, throttle * 1.8f))
        isSimulatingHardDrive = throttle > 70f
    }

    fun scanDtcCodes(): List<String> {
        val simulatedDtcs = listOf("P0420: Katalizör Sistemi Verimi Eşik Altında", "P0171: Sistem Aşırı Fakir (Sıra 1)")
        _telemetry.value = _telemetry.value.copy(errorCodes = simulatedDtcs)
        return simulatedDtcs
    }

    fun clearDtcCodes() {
        _telemetry.value = _telemetry.value.copy(errorCodes = emptyList())
    }

    private suspend fun readRealObdData() {
        try {
            val input = obdSocket?.inputStream ?: return
            val output = obdSocket?.outputStream ?: return

            // Query RPM (PID 010C)
            output.write("010C\r".toByteArray())
            delay(50)
            val rpmResponse = readResponse(input)
            val rpmVal = parseRpm(rpmResponse)

            // Query Speed (PID 010D)
            output.write("010D\r".toByteArray())
            delay(50)
            val speedResponse = readResponse(input)
            val speedVal = parseSpeed(speedResponse)

            _telemetry.value = _telemetry.value.copy(
                rpm = if (rpmVal > 0) rpmVal else _telemetry.value.rpm,
                speedKmh = if (speedVal >= 0) speedVal.toFloat() else _telemetry.value.speedKmh
            )
        } catch (e: Exception) {
            // Fallback to physics on socket error
            updateSimulatedPhysics()
        }
    }

    private fun readResponse(inputStream: InputStream): String {
        val buffer = ByteArray(128)
        var bytes = 0
        val sb = StringBuilder()
        while (inputStream.available() > 0 && bytes < 128) {
            val b = inputStream.read()
            if (b == -1 || b == '>'.code) break
            sb.append(b.toChar())
            bytes++
        }
        return sb.toString().trim()
    }

    private fun parseRpm(response: String): Int {
        return try {
            val clean = response.replace(" ", "").replace("\r", "")
            if (clean.contains("410C")) {
                val hex = clean.substringAfter("410C").take(4)
                val a = hex.substring(0, 2).toInt(16)
                val b = hex.substring(2, 4).toInt(16)
                ((a * 256) + b) / 4
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun parseSpeed(response: String): Int {
        return try {
            val clean = response.replace(" ", "").replace("\r", "")
            if (clean.contains("410D")) {
                val hex = clean.substringAfter("410D").take(2)
                hex.toInt(16)
            } else -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun updateSimulatedPhysics() {
        simTime += 0.1
        val current = _telemetry.value

        // Smooth speed transition towards realistic highway/city target
        val speedNoise = (sin(simTime * 0.4) * 3f).toFloat()
        val target = if (isSimulatingHardDrive) targetSpeed + speedNoise else (68f + (sin(simTime * 0.2) * 12f).toFloat())
        val newSpeed = (current.speedKmh + (target - current.speedKmh) * 0.08f).coerceIn(0f, 240f)

        // Calculate dynamic gear and RPM
        val (gear, rpmRatio) = when {
            newSpeed < 5f -> Pair("P", 800f)
            newSpeed < 25f -> Pair("D1", 900f + (newSpeed / 25f) * 2200f)
            newSpeed < 45f -> Pair("D2", 1400f + ((newSpeed - 25f) / 20f) * 2200f)
            newSpeed < 70f -> Pair("D3", 1600f + ((newSpeed - 45f) / 25f) * 2000f)
            newSpeed < 100f -> Pair("D4", 1800f + ((newSpeed - 70f) / 30f) * 1800f)
            newSpeed < 135f -> Pair("D5", 2000f + ((newSpeed - 100f) / 35f) * 1700f)
            else -> Pair("D6", 2100f + ((newSpeed - 135f) / 60f) * 2400f)
        }

        val calculatedRpm = (rpmRatio + (sin(simTime * 2.0) * 80).toFloat()).toInt().coerceIn(750, 6800)
        val load = ((newSpeed / 180f) * 60f + 15f + (sin(simTime * 0.8) * 8).toFloat()).coerceIn(10f, 98f)
        val throttle = (load * 0.85f).coerceIn(5f, 100f)
        val turbo = if (calculatedRpm > 2000) (((calculatedRpm - 2000) / 4500f) * 1.4f).coerceIn(0f, 1.6f) else 0.1f
        val fuelCons = (3.8f + (newSpeed * 0.045f) + (load * 0.035f)).coerceIn(0.8f, 18.5f)
        val voltage = (14.2f + sin(simTime * 0.1) * 0.15f).toFloat()
        val coolant = (90f + sin(simTime * 0.05) * 2.5f).toFloat().coerceIn(75f, 105f)

        _telemetry.value = current.copy(
            speedKmh = newSpeed,
            rpm = calculatedRpm,
            currentGear = gear,
            engineLoadPct = load,
            throttlePosPct = throttle,
            turboBoostBar = turbo,
            instantL100km = fuelCons,
            batteryVoltage = voltage,
            coolantTempC = coolant,
            totalTripDistanceKm = current.totalTripDistanceKm + (newSpeed / 36000f)
        )
    }
}
