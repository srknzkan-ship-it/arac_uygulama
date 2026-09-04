package com.example.data.media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Real Multi-Voice Synthesizer using Android's native AudioTrack PCM streaming.
 * Generates actual audible melodies, chords, basslines, and percussion for all tracks and radio stations.
 */
class RealAudioEngine {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var currentVolume = 0.75f
    private var synthesisJob: Job? = null

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    // Current track melody notes and BPM
    private var currentSongIndex = 0
    private var noteStep = 0
    private var isRadioStatic = false

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.setVolume(currentVolume)
        } catch (e: Exception) {
            Log.e("RealAudioEngine", "Error initializing AudioTrack", e)
        }
    }

    fun start(songIndex: Int = 0, isRadio: Boolean = false) {
        currentSongIndex = songIndex
        isRadioStatic = isRadio
        if (isPlaying) return
        isPlaying = true

        try {
            audioTrack?.play()
        } catch (e: Exception) {
            initAudioTrack()
            audioTrack?.play()
        }

        synthesisJob?.cancel()
        synthesisJob = CoroutineScope(Dispatchers.Default).launch {
            val audioBuffer = ShortArray(bufferSize)
            var phaseLead = 0.0
            var phaseBass = 0.0
            var phasePad = 0.0
            var tick = 0

            while (isActive && isPlaying) {
                // Song-specific melody & chord frequencies
                val (leadFreq, bassFreq, padFreq, tempoSamples) = getSongFrequencies(currentSongIndex, noteStep, isRadioStatic)

                val samplesPerNote = tempoSamples
                for (i in 0 until bufferSize) {
                    val progressInNote = (tick % samplesPerNote).toFloat() / samplesPerNote
                    val envelope = (1.0f - progressInNote * 0.7f).coerceIn(0.1f, 1.0f)

                    val sampleLead = if (leadFreq > 10.0) sin(phaseLead) * 0.35f * envelope else 0.0
                    val sampleBass = if (bassFreq > 10.0) {
                        // Richer square/saw wave blend for bass
                        val s1 = sin(phaseBass)
                        val s2 = sin(phaseBass * 2.0) * 0.3
                        (s1 + s2) * 0.30f
                    } else 0.0

                    val samplePad = if (padFreq > 10.0) sin(phasePad) * 0.15f else 0.0

                    // Percussion click / hi-hat / kick on beats
                    val isKick = (tick % (samplesPerNote * 2)) < (sampleRate * 0.03)
                    val kickSample = if (isKick) (sin(tick * 0.015) * 0.35f) else 0.0

                    // Radio tuning noise blend if in FM mode
                    val noise = if (isRadioStatic) ((Math.random() - 0.5) * 0.08f) else 0.0

                    val mixed = (sampleLead + sampleBass + samplePad + kickSample + noise) * currentVolume
                    val clamped = mixed.coerceIn(-0.95, 0.95)
                    audioBuffer[i] = (clamped * 32767.0).toInt().toShort()

                    // Increment phases
                    phaseLead += 2.0 * PI * leadFreq / sampleRate
                    if (phaseLead > 2.0 * PI) phaseLead -= 2.0 * PI

                    phaseBass += 2.0 * PI * bassFreq / sampleRate
                    if (phaseBass > 2.0 * PI) phaseBass -= 2.0 * PI

                    phasePad += 2.0 * PI * padFreq / sampleRate
                    if (phasePad > 2.0 * PI) phasePad -= 2.0 * PI

                    tick++
                    if (tick % samplesPerNote == 0) {
                        noteStep++
                    }
                }

                audioTrack?.write(audioBuffer, 0, bufferSize)
            }
        }
    }

    fun pause() {
        isPlaying = false
        synthesisJob?.cancel()
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun changeSong(songIndex: Int, isRadio: Boolean = false) {
        currentSongIndex = songIndex
        isRadioStatic = isRadio
        noteStep = 0
        if (!isPlaying) {
            start(songIndex, isRadio)
        }
    }

    fun setVolume(vol: Float) {
        currentVolume = vol.coerceIn(0.0f, 1.0f)
        try {
            audioTrack?.setVolume(currentVolume)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun seekToStep(step: Int) {
        noteStep = step
    }

    fun playTone(frequency: Double, durationMs: Long) {
        CoroutineScope(Dispatchers.Default).launch {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val sample = sin(phase) * 0.6f * currentVolume
                buffer[i] = (sample * 32767.0).toInt().toShort()
                phase += 2.0 * PI * frequency / sampleRate
                if (phase > 2.0 * PI) phase -= 2.0 * PI
            }
            try {
                audioTrack?.write(buffer, 0, numSamples)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun playNavigationChime() {
        CoroutineScope(Dispatchers.Default).launch {
            playTone(880.0, 120) // A5
            delay(130)
            playTone(1174.66, 180) // D6
        }
    }

    fun release() {
        isPlaying = false
        synthesisJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // ignore
        }
    }

    // Frequencies (Hz) of musical notes
    // C4=261.63, D4=293.66, Eb4=311.13, E4=329.63, F4=349.23, G4=392.00, Ab4=415.30, A4=440.00, Bb4=466.16, B4=493.88
    // C5=523.25, D5=587.33, Eb5=622.25, F5=698.46, G5=783.99
    private fun getSongFrequencies(songIndex: Int, step: Int, isRadio: Boolean): Quadruple<Double, Double, Double, Int> {
        val samplesPer16th = (sampleRate * 60.0 / 128.0 / 4.0).toInt() // 128 BPM 16th note

        if (isRadio) {
            // Uplifting radio hit melody
            val melody = listOf(440.0, 523.25, 659.25, 783.99, 659.25, 523.25, 440.0, 392.0)
            val bass = listOf(110.0, 110.0, 130.81, 130.81, 146.83, 146.83, 98.0, 98.0)
            val m = melody[step % melody.size]
            val b = bass[step % bass.size]
            return Quadruple(m, b, 220.0, samplesPer16th * 2)
        }

        return when (songIndex % 6) {
            0 -> {
                // 1. "Blinding Lights" (The Weeknd) - Iconic F Minor Synth Arpeggio
                val blindingMelody = listOf(
                    698.46, 698.46, 622.25, 698.46, // F5, F5, Eb5, F5
                    783.99, 698.46, 622.25, 523.25, // G5, F5, Eb5, C5
                    622.25, 622.25, 523.25, 622.25, // Eb5, Eb5, C5, Eb5
                    698.46, 622.25, 523.25, 466.16  // F5, Eb5, C5, Bb4
                )
                val blindingBass = listOf(
                    174.61, 174.61, 174.61, 174.61, // F3
                    130.81, 130.81, 130.81, 130.81, // C3
                    155.56, 155.56, 155.56, 155.56, // Eb3
                    196.00, 196.00, 196.00, 196.00  // G3
                )
                val lead = blindingMelody[step % blindingMelody.size]
                val bass = blindingBass[step % blindingBass.size]
                val pad = 349.23 // F4
                Quadruple(lead, bass, pad, samplesPer16th)
            }
            1 -> {
                // 2. "Get Lucky" (Daft Punk) - Bm -> D -> F#m -> E
                val discoChords = listOf(
                    Pair(493.88, 123.47), // B4, B2
                    Pair(587.33, 146.83), // D5, D3
                    Pair(739.99, 185.00), // F#5, F#3
                    Pair(659.25, 164.81)  // E5, E3
                )
                val currentChord = discoChords[(step / 4) % discoChords.size]
                val arpOffsets = listOf(0.0, 12.0, 7.0, 12.0)
                val lead = currentChord.first * (1.0 + (step % 4) * 0.1)
                val bass = currentChord.second
                Quadruple(lead, bass, 246.94, samplesPer16th)
            }
            2 -> {
                // 3. "Midnight City" (M83) - Synth Wave Anthem
                val m83Notes = listOf(
                    587.33, 659.25, 783.99, 880.0, 783.99, 659.25, 587.33, 440.0
                )
                val bassNotes = listOf(146.83, 146.83, 174.61, 174.61, 220.0, 220.0, 130.81, 130.81)
                val lead = m83Notes[step % m83Notes.size]
                val bass = bassNotes[step % bassNotes.size]
                Quadruple(lead, bass, 293.66, samplesPer16th)
            }
            3 -> {
                // 4. "Starboy" (The Weeknd) - Dark Synth
                val starboyMelody = listOf(523.25, 466.16, 392.00, 349.23, 392.00, 466.16, 523.25, 622.25)
                val starboyBass = listOf(130.81, 130.81, 116.54, 116.54, 98.00, 98.00, 87.31, 87.31)
                Quadruple(starboyMelody[step % starboyMelody.size], starboyBass[step % starboyBass.size], 261.63, samplesPer16th)
            }
            4 -> {
                // 5. "Nightcall" (Kavinsky) - Deep driving 80s bassline
                val nightcallMelody = listOf(440.0, 440.0, 392.0, 440.0, 523.25, 440.0, 392.0, 349.23)
                val nightcallBass = listOf(110.0, 110.0, 98.0, 110.0, 130.81, 110.0, 98.0, 87.31)
                Quadruple(nightcallMelody[step % nightcallMelody.size], nightcallBass[step % nightcallBass.size], 220.0, samplesPer16th * 2)
            }
            else -> {
                // 6. Rock / Türkçe Hit
                val rockMelody = listOf(587.33, 523.25, 493.88, 440.0, 392.0, 440.0, 493.88, 523.25)
                val rockBass = listOf(146.83, 146.83, 123.47, 123.47, 98.0, 98.0, 110.0, 110.0)
                Quadruple(rockMelody[step % rockMelody.size], rockBass[step % rockBass.size], 293.66, samplesPer16th)
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
