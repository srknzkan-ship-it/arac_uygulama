package com.example.data.media

import android.net.Uri

enum class MediaSource {
    RADIO_FM,
    BLUETOOTH_AUDIO,
    LOCAL_STORAGE,
    SPOTIFY_STREAM
}

data class TrackInfo(
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val currentPositionSeconds: Int,
    val stationName: String? = null,
    val frequency: Double? = null,
    val albumArtRes: String? = null,
    val albumId: Long? = null,
    val albumArtUri: Uri? = null,
    val albumArtBytes: ByteArray? = null,
    val uri: Uri? = null,
    val isLocalFile: Boolean = false,
    val filePath: String? = null
)

data class LocalAudioTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dataPath: String,
    val albumId: Long? = null,
    val albumArtUri: Uri? = null,
    val sizeBytes: Long = 0L
)

data class RadioStation(
    val frequency: Double,
    val name: String,
    val genre: String,
    val currentSong: String,
    val artist: String,
    val isFavorite: Boolean = false
)

