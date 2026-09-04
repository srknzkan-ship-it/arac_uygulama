package com.example.data.media

import android.content.ContentUris
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.AutoRepository
import com.example.data.db.RadioStationEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class MediaManager(
    private val context: Context,
    private val repository: AutoRepository
) {
    val audioEngine = RealAudioEngine()

    private var mediaPlayer: MediaPlayer? = null

    private val _currentSource = MutableStateFlow(MediaSource.LOCAL_STORAGE)
    val currentSource: StateFlow<MediaSource> = _currentSource.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _localAudioTracks = MutableStateFlow<List<LocalAudioTrack>>(emptyList())
    val localAudioTracks: StateFlow<List<LocalAudioTrack>> = _localAudioTracks.asStateFlow()

    private val _isScanningLocalMedia = MutableStateFlow(false)
    val isScanningLocalMedia: StateFlow<Boolean> = _isScanningLocalMedia.asStateFlow()

    private val _currentTrack = MutableStateFlow(
        TrackInfo(
            title = "Cihazdaki Müzikler",
            artist = "Yerel Depolama / Müzik Kitaplığı",
            album = "Yerel Müzik Çalar",
            durationSeconds = 0,
            currentPositionSeconds = 0,
            stationName = null,
            frequency = null,
            isLocalFile = false
        )
    )
    val currentTrack: StateFlow<TrackInfo> = _currentTrack.asStateFlow()

    private val _equalizerBands = MutableStateFlow(List(16) { 0.5f })
    val equalizerBands: StateFlow<List<Float>> = _equalizerBands.asStateFlow()

    private val _radioStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val radioStations: StateFlow<List<RadioStation>> = _radioStations.asStateFlow()

    private val _currentFrequency = MutableStateFlow(100.0)
    val currentFrequency: StateFlow<Double> = _currentFrequency.asStateFlow()

    private var playbackJob: Job? = null
    private var equalizerJob: Job? = null
    private var eqPhase = 0.0

    private val predefinedStations = listOf(
        RadioStation(100.0, "Power FM", "Hits / Pop", "Blinding Lights", "The Weeknd", true),
        RadioStation(101.4, "Virgin Radio", "Rock / Indie", "Do I Wanna Know?", "Arctic Monkeys", true),
        RadioStation(94.7, "Kral Pop", "Türkçe Pop", "Aşkın Olayım", "Simge", true),
        RadioStation(100.4, "Radyo Fenomen", "Dance / Club", "Titanium", "David Guetta & Sia", true),
        RadioStation(89.0, "Joy FM", "Acoustic / Chill", "Shallow", "Lady Gaga & Bradley Cooper", false),
        RadioStation(99.0, "Metro FM", "Top 40", "Flowers", "Miley Cyrus", false),
        RadioStation(95.6, "TRT Radyo 1", "Haber / Kültür", "Gündem ve Yol Durumu", "TRT Haber", false),
        RadioStation(103.8, "Radyo D", "Türkçe Hit", "Antidepresan", "Mert Demir & Mabel Matiz", false)
    )

    private val fallbackPlaylist = listOf(
        TrackInfo("Blinding Lights", "The Weeknd", "After Hours", 200, 0),
        TrackInfo("Get Lucky", "Daft Punk ft. Pharrell", "Random Access Memories", 248, 0),
        TrackInfo("Midnight City", "M83", "Hurry Up, We're Dreaming", 243, 0),
        TrackInfo("Starboy", "The Weeknd & Daft Punk", "Starboy", 230, 0),
        TrackInfo("Nightcall", "Kavinsky", "Drive Soundtrack", 259, 0),
        TrackInfo("Yalan", "Mor ve Ötesi", "Dünya Yalan Söylüyor", 265, 0),
        TrackInfo("Resimdeki Gözyaşları", "Cem Karaca", "Klasikler", 215, 0)
    )
    private var fallbackIndex = 0
    private var currentLocalTrackIndex = -1

    init {
        _radioStations.value = predefinedStations
        scanLocalAudioFiles()
    }

    fun start() {
        startPlaybackTimer()
        startEqualizerAnimation()
    }

    fun stop() {
        playbackJob?.cancel()
        equalizerJob?.cancel()
        releaseMediaPlayer()
        audioEngine.release()
    }

    private fun extractAlbumArtBytes(uri: Uri): ByteArray? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val art = retriever.embeddedPicture
            retriever.release()
            art
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Scans real local audio files stored on device / SD card via Android MediaStore
     */
    fun scanLocalAudioFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            _isScanningLocalMedia.value = true
            val tracks = mutableListOf<LocalAudioTrack>()
            try {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE
                )

                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                val cursor = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )

                cursor?.use { c ->
                    val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                    while (c.moveToNext()) {
                        val id = c.getLong(idCol)
                        val title = c.getString(titleCol) ?: "Bilinmeyen Parça"
                        val artist = c.getString(artistCol) ?: "Bilinmeyen Sanatçı"
                        val album = c.getString(albumCol) ?: "Bilinmeyen Albüm"
                        val albumId = c.getLong(albumIdCol)
                        val duration = c.getLong(durationCol)
                        val dataPath = c.getString(dataCol) ?: ""
                        val size = c.getLong(sizeCol)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        val albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

                        tracks.add(
                            LocalAudioTrack(
                                id = id,
                                uri = contentUri,
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                dataPath = dataPath,
                                albumId = albumId,
                                albumArtUri = albumArtUri,
                                sizeBytes = size
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaManager", "Error querying local audio files", e)
            } finally {
                _localAudioTracks.value = tracks
                _isScanningLocalMedia.value = false

                // If tracks found and currently default, set the first local track
                if (tracks.isNotEmpty() && currentLocalTrackIndex == -1 && !_currentTrack.value.isLocalFile) {
                    val first = tracks.first()
                    val embeddedArt = extractAlbumArtBytes(first.uri)
                    _currentTrack.value = TrackInfo(
                        title = first.title,
                        artist = first.artist,
                        album = first.album,
                        durationSeconds = (first.durationMs / 1000).toInt(),
                        currentPositionSeconds = 0,
                        albumId = first.albumId,
                        albumArtUri = first.albumArtUri,
                        albumArtBytes = embeddedArt,
                        uri = first.uri,
                        isLocalFile = true,
                        filePath = first.dataPath
                    )
                    currentLocalTrackIndex = 0
                }
            }
        }
    }

    /**
     * Plays a specific local audio track using Android's native MediaPlayer
     */
    fun playLocalTrack(track: LocalAudioTrack) {
        val index = _localAudioTracks.value.indexOfFirst { it.id == track.id }
        if (index != -1) {
            currentLocalTrackIndex = index
        }
        _currentSource.value = MediaSource.LOCAL_STORAGE
        audioEngine.pause()

        try {
            releaseMediaPlayer()
            val embeddedArt = extractAlbumArtBytes(track.uri)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, track.uri)
                setVolume(_volume.value, _volume.value)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    val durationSec = (mp.duration / 1000).coerceAtLeast(1)
                    _currentTrack.value = TrackInfo(
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        durationSeconds = durationSec,
                        currentPositionSeconds = 0,
                        albumId = track.albumId,
                        albumArtUri = track.albumArtUri,
                        albumArtBytes = embeddedArt,
                        uri = track.uri,
                        isLocalFile = true,
                        filePath = track.dataPath
                    )
                }
                setOnCompletionListener {
                    nextTrack()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MediaManager", "MediaPlayer error what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "Failed to play local track: ${track.title}", e)
        }
    }

    /**
     * Plays an audio URI selected by the user via file picker / document opener
     */
    fun playAudioUri(uri: Uri, displayName: String) {
        _currentSource.value = MediaSource.LOCAL_STORAGE
        audioEngine.pause()

        try {
            releaseMediaPlayer()
            val embeddedArt = extractAlbumArtBytes(uri)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                setVolume(_volume.value, _volume.value)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    val durationSec = (mp.duration / 1000).coerceAtLeast(1)
                    _currentTrack.value = TrackInfo(
                        title = displayName,
                        artist = "Seçilen Yerel Dosya",
                        album = "Cihaz Depolama",
                        durationSeconds = durationSec,
                        currentPositionSeconds = 0,
                        albumArtBytes = embeddedArt,
                        uri = uri,
                        isLocalFile = true,
                        filePath = uri.path
                    )
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MediaManager", "MediaPlayer custom uri error what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "Failed to play custom uri: $displayName", e)
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (_isPlaying.value) {
                    val mp = mediaPlayer
                    if (mp != null && mp.isPlaying) {
                        try {
                            val curSec = mp.currentPosition / 1000
                            val durSec = (mp.duration / 1000).coerceAtLeast(1)
                            _currentTrack.value = _currentTrack.value.copy(
                                currentPositionSeconds = curSec,
                                durationSeconds = durSec
                            )
                        } catch (e: Exception) {
                            // ignore
                        }
                    } else if (mp == null) {
                        // Synthetic / radio track simulation timer
                        val track = _currentTrack.value
                        val nextSec = if (track.durationSeconds > 0 && track.currentPositionSeconds >= track.durationSeconds) 0 else track.currentPositionSeconds + 1
                        if (nextSec == 0 && _currentSource.value != MediaSource.RADIO_FM) {
                            nextTrack()
                        } else {
                            _currentTrack.value = track.copy(currentPositionSeconds = nextSec)
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun startEqualizerAnimation() {
        equalizerJob?.cancel()
        equalizerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (_isPlaying.value) {
                    eqPhase += 0.2
                    val newBands = (0 until 16).map { i ->
                        val base = (sin(eqPhase + i * 0.45) * 0.4 + 0.5).toFloat()
                        val noise = (Random.nextFloat() * 0.2f)
                        (base + noise).coerceIn(0.1f, 1.0f)
                    }
                    _equalizerBands.value = newBands
                } else {
                    _equalizerBands.value = List(16) { 0.05f }
                }
                delay(70)
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.start()
                _isPlaying.value = true
            } catch (e: Exception) {
                Log.e("MediaManager", "Error resuming MediaPlayer", e)
            }
        } else {
            val localTracks = _localAudioTracks.value
            if (_currentSource.value == MediaSource.LOCAL_STORAGE && localTracks.isNotEmpty()) {
                val idx = if (currentLocalTrackIndex in localTracks.indices) currentLocalTrackIndex else 0
                playLocalTrack(localTracks[idx])
            } else {
                _isPlaying.value = true
                val isRadio = _currentSource.value == MediaSource.RADIO_FM
                audioEngine.start(fallbackIndex, isRadio)
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            // ignore
        }
        audioEngine.pause()
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            // ignore
        }
        audioEngine.setVolume(clamped)
    }

    fun seekToPosition(positionSec: Int) {
        val clamped = positionSec.coerceIn(0, _currentTrack.value.durationSeconds)
        _currentTrack.value = _currentTrack.value.copy(currentPositionSeconds = clamped)

        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.seekTo(clamped * 1000)
            } catch (e: Exception) {
                // ignore
            }
        } else {
            audioEngine.seekToStep(clamped * 8)
        }
    }

    fun setSource(source: MediaSource) {
        _currentSource.value = source
        if (source == MediaSource.RADIO_FM) {
            releaseMediaPlayer()
            selectStation(_currentFrequency.value)
        } else if (source == MediaSource.LOCAL_STORAGE) {
            val localList = _localAudioTracks.value
            if (localList.isNotEmpty()) {
                val idx = if (currentLocalTrackIndex in localList.indices) currentLocalTrackIndex else 0
                playLocalTrack(localList[idx])
            } else {
                val track = fallbackPlaylist[fallbackIndex]
                _currentTrack.value = track.copy(stationName = null, frequency = null, isLocalFile = false)
                if (_isPlaying.value) {
                    audioEngine.changeSong(fallbackIndex, false)
                }
            }
        } else {
            val track = fallbackPlaylist[fallbackIndex]
            _currentTrack.value = track.copy(stationName = null, frequency = null, isLocalFile = false)
            if (_isPlaying.value) {
                audioEngine.changeSong(fallbackIndex, false)
            }
        }
    }

    fun selectStation(frequency: Double) {
        _currentFrequency.value = frequency
        releaseMediaPlayer()

        val station = _radioStations.value.find { abs(it.frequency - frequency) < 0.05 }
            ?: RadioStation(frequency, "FM ${String.format("%.1f", frequency)}", "Genel Müzik", "Canlı Yayın", "Radyo Yayını")

        _currentTrack.value = TrackInfo(
            title = station.currentSong,
            artist = station.artist,
            album = station.genre,
            durationSeconds = 240,
            currentPositionSeconds = Random.nextInt(10, 180),
            stationName = station.name,
            frequency = station.frequency,
            isLocalFile = false
        )

        if (_isPlaying.value) {
            audioEngine.changeSong(0, true)
        }
    }

    fun tuneFrequency(delta: Double) {
        val newFreq = (_currentFrequency.value + delta).coerceIn(87.5, 108.0)
        val rounded = (newFreq * 10).toInt() / 10.0
        selectStation(rounded)
    }

    fun nextTrack() {
        if (_currentSource.value == MediaSource.RADIO_FM) {
            val currentIndex = _radioStations.value.indexOfFirst { abs(it.frequency - _currentFrequency.value) < 0.05 }
            val nextIdx = (currentIndex + 1) % _radioStations.value.size
            selectStation(_radioStations.value[nextIdx].frequency)
        } else {
            val localTracks = _localAudioTracks.value
            if (localTracks.isNotEmpty()) {
                currentLocalTrackIndex = (currentLocalTrackIndex + 1) % localTracks.size
                playLocalTrack(localTracks[currentLocalTrackIndex])
            } else {
                fallbackIndex = (fallbackIndex + 1) % fallbackPlaylist.size
                _currentTrack.value = fallbackPlaylist[fallbackIndex].copy(currentPositionSeconds = 0, isLocalFile = false)
                if (_isPlaying.value) {
                    audioEngine.changeSong(fallbackIndex, false)
                }
            }
        }
    }

    fun prevTrack() {
        if (_currentSource.value == MediaSource.RADIO_FM) {
            val currentIndex = _radioStations.value.indexOfFirst { abs(it.frequency - _currentFrequency.value) < 0.05 }
            val prevIdx = if (currentIndex <= 0) _radioStations.value.size - 1 else currentIndex - 1
            selectStation(_radioStations.value[prevIdx].frequency)
        } else {
            val localTracks = _localAudioTracks.value
            if (localTracks.isNotEmpty()) {
                currentLocalTrackIndex = if (currentLocalTrackIndex <= 0) localTracks.size - 1 else currentLocalTrackIndex - 1
                playLocalTrack(localTracks[currentLocalTrackIndex])
            } else {
                fallbackIndex = if (fallbackIndex <= 0) fallbackPlaylist.size - 1 else fallbackIndex - 1
                _currentTrack.value = fallbackPlaylist[fallbackIndex].copy(currentPositionSeconds = 0, isLocalFile = false)
                if (_isPlaying.value) {
                    audioEngine.changeSong(fallbackIndex, false)
                }
            }
        }
    }

    fun playDtmfTone(char: Char) {
        val freq = when (char) {
            '1' -> 697.0
            '2' -> 770.0
            '3' -> 852.0
            '4' -> 697.0
            '5' -> 770.0
            '6' -> 852.0
            '7' -> 697.0
            '8' -> 770.0
            '9' -> 852.0
            '0' -> 941.0
            '*' -> 941.0
            '#' -> 941.0
            else -> 800.0
        }
        audioEngine.playTone(freq, 90)
    }

    fun toggleFavorite(station: RadioStation) {
        _radioStations.value = _radioStations.value.map {
            if (abs(it.frequency - station.frequency) < 0.05) it.copy(isFavorite = !it.isFavorite) else it
        }
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateRadioFavorite(
                RadioStationEntity(
                    frequency = station.frequency,
                    name = station.name,
                    genre = station.genre,
                    isFavorite = !station.isFavorite
                )
            )
        }
    }
}
