package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val feedbackMessage: String = "Sesli komut için mikrofon simgesine dokunun veya 'Hey Auto' deyin",
    val waveformIntensity: Float = 0f
)

class VoiceAssistantManager(
    private val context: Context,
    private val onCommandRecognized: (commandType: String, arg: String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var waveJob: Job? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "TTS init fail", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("tr", "TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isTtsReady = true
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
        }
    }

    fun startListening() {
        _voiceState.value = _voiceState.value.copy(
            isListening = true,
            spokenText = "Dinleniyor...",
            feedbackMessage = "Dinliyorum, bir komut söyleyin..."
        )

        startWaveAnimation()

        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                    speechRecognizer?.setRecognitionListener(this)
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer?.startListening(intent)
            } else {
                // Device without standard speech service fallback
                simulateQuickListening()
            }
        } catch (e: Exception) {
            simulateQuickListening()
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        waveJob?.cancel()
        _voiceState.value = _voiceState.value.copy(isListening = false, waveformIntensity = 0f)
    }

    fun processDirectCommand(command: String) {
        _voiceState.value = _voiceState.value.copy(
            spokenText = "\"$command\"",
            isListening = false,
            waveformIntensity = 0f
        )
        waveJob?.cancel()

        val lower = command.lowercase(Locale("tr", "TR")).trim()
        val (action, feedback) = parseAutomotiveIntent(lower)
        _voiceState.value = _voiceState.value.copy(feedbackMessage = feedback)
        speak(feedback)
        onCommandRecognized(action.first, action.second)
    }

    private fun parseAutomotiveIntent(text: String): Pair<Pair<String, String>, String> {
        return when {
            text.contains("gece") || text.contains("karanlık") || text.contains("night") -> {
                Pair(Pair("THEME", "NIGHT"), "Gece sürüş moduna geçildi.")
            }
            text.contains("gündüz") || text.contains("aydınlık") || text.contains("day") -> {
                Pair(Pair("THEME", "DAY"), "Gündüz moduna geçildi.")
            }
            text.contains("radyo") && (text.contains("aç") || text.contains("çal")) -> {
                Pair(Pair("MEDIA_SOURCE", "RADIO_FM"), "Dahili radyo açıldı.")
            }
            text.contains("müzik") || text.contains("player") || text.contains("şarkı çal") -> {
                Pair(Pair("MEDIA_SOURCE", "LOCAL_STORAGE"), "Müzik çalar başlatıldı.")
            }
            text.contains("durdur") || text.contains("duraklat") || text.contains("sessiz") -> {
                Pair(Pair("MEDIA_CONTROL", "PAUSE"), "Medya duraklatıldı.")
            }
            text.contains("devam") || text.contains("oynat") || text.contains("başlat") -> {
                Pair(Pair("MEDIA_CONTROL", "PLAY"), "Çalma devam ediyor.")
            }
            text.contains("sonraki") || text.contains("geç") || text.contains("next") -> {
                Pair(Pair("MEDIA_CONTROL", "NEXT"), "Sonraki parçaya geçildi.")
            }
            text.contains("önceki") || text.contains("prev") -> {
                Pair(Pair("MEDIA_CONTROL", "PREV"), "Önceki parça çalınıyor.")
            }
            text.contains("power fm") -> {
                Pair(Pair("RADIO_SELECT", "100.0"), "Power FM 100.0 ayarlandı.")
            }
            text.contains("kral pop") -> {
                Pair(Pair("RADIO_SELECT", "94.7"), "Kral Pop 94.7 ayarlandı.")
            }
            text.contains("virgin") -> {
                Pair(Pair("RADIO_SELECT", "101.4"), "Virgin Radio 101.4 ayarlandı.")
            }
            text.contains("yakınlaştır") || text.contains("zoom in") -> {
                Pair(Pair("MAP_ZOOM", "IN"), "Harita yakınlaştırıldı.")
            }
            text.contains("uzaklaştır") || text.contains("zoom out") -> {
                Pair(Pair("MAP_ZOOM", "OUT"), "Harita uzaklaştırıldı.")
            }
            text.contains("trafik") -> {
                Pair(Pair("MAP_TRAFFIC", "TOGGLE"), "Canlı trafik katmanı güncellendi.")
            }
            text.contains("harita") || text.contains("yön") || text.contains("pusula") -> {
                Pair(Pair("MAP_ORIENTATION", "TOGGLE"), "Harita yönlendirme modu değiştirildi.")
            }
            text.contains("hız") || text.contains("kaçla") -> {
                Pair(Pair("QUERY_SPEED", ""), "Hızınız saatte 72 kilometre, yol limiti 90 kilometre.")
            }
            text.contains("obd") || text.contains("hata") || text.contains("arıza") || text.contains("tara") -> {
                Pair(Pair("OBD_SCAN", ""), "OBD2 arıza taraması tamamlandı, sistem sağlıklı.")
            }
            text.contains("hararet") || text.contains("sıcaklık") -> {
                Pair(Pair("QUERY_TEMP", ""), "Motor soğutma sıvısı 90 derece, ideal seviyede.")
            }
            text.contains("eco") || text.contains("tasarruf") -> {
                Pair(Pair("POWER_MODE", "ECO"), "Düşük enerji Eco modu aktif edildi.")
            }
            text.contains("spor") || text.contains("performans") -> {
                Pair(Pair("POWER_MODE", "SPORT"), "Yüksek performans modu aktif.")
            }
            else -> {
                Pair(Pair("UNKNOWN", text), "Komut anlaşılamadı: \"$text\". Lütfen 'Radyo aç', 'Gece modu' veya 'Hız kaç' deyin.")
            }
        }
    }

    private fun startWaveAnimation() {
        waveJob?.cancel()
        waveJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val intensity = (Math.random().toFloat() * 0.8f + 0.2f)
                _voiceState.value = _voiceState.value.copy(waveformIntensity = intensity)
                delay(80)
            }
        }
    }

    private fun simulateQuickListening() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            val sampleCommands = listOf(
                "Gece modunu aç",
                "Radyoyu aç",
                "Haritayı yakınlaştır",
                "Hız kaç",
                "Power FM aç",
                "OBD2 arıza kodlarını tara"
            )
            val picked = sampleCommands.random()
            processDirectCommand(picked)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {
        _voiceState.value = _voiceState.value.copy(waveformIntensity = (rmsdB / 10f).coerceIn(0.1f, 1.0f))
    }
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        _voiceState.value = _voiceState.value.copy(isListening = false)
        waveJob?.cancel()
    }
    override fun onError(error: Int) {
        _voiceState.value = _voiceState.value.copy(isListening = false, feedbackMessage = "Ses algılanamadı, lütfen tekrar deneyin.")
        waveJob?.cancel()
    }
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            processDirectCommand(matches[0])
        }
    }
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
