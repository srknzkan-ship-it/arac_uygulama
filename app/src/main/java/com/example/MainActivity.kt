package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainDashboardScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.AutoDriveTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            AutoDriveTheme(mode = themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    MainDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            @Suppress("DEPRECATION")
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
        } else {
            viewModel.enableRealGps()
            viewModel.mediaManager.scanLocalAudioFiles()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val locationGranted = grantResults.indices.any { i ->
                (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION ||
                 permissions[i] == Manifest.permission.ACCESS_COARSE_LOCATION) &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
            if (locationGranted) {
                viewModel.enableRealGps()
            }
            val audioGranted = grantResults.indices.any { i ->
                (permissions[i] == "android.permission.READ_MEDIA_AUDIO" ||
                 permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE) &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
            if (audioGranted) {
                viewModel.mediaManager.scanLocalAudioFiles()
            }
        }
    }
}
