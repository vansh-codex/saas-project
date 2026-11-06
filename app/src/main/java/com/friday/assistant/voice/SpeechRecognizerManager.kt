package com.friday.assistant.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Wraps the platform [SpeechRecognizer] and exposes its callbacks as a [StateFlow] of
 * [VoiceEvent]s.
 *
 * All engine interaction is marshalled onto the main thread because [SpeechRecognizer] is not
 * thread safe.
 */
class SpeechRecognizerManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _events = MutableStateFlow<VoiceEvent?>(null)
    val events: StateFlow<VoiceEvent?> = _events.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    val hasMicrophonePermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

    private val listener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            _events.value = VoiceEvent.Ready
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            _events.value = VoiceEvent.Failed(errorMessage(error))
        }

        override fun onResults(results: Bundle?) {
            val text = results.bestTranscript()
            _events.value = if (text.isNullOrBlank()) VoiceEvent.Finished else VoiceEvent.FinalTranscript(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults.bestTranscript()?.takeIf { it.isNotBlank() }?.let {
                _events.value = VoiceEvent.PartialTranscript(it)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    /** Starts a fresh recognition session. */
    fun startListening(languageTag: String = Locale.getDefault().toLanguageTag()) {
        mainHandler.post {
            if (!isAvailable) {
                _events.value = VoiceEvent.Failed("Speech recognition is not available on this device.")
                return@post
            }
            if (!hasMicrophonePermission) {
                _events.value = VoiceEvent.Failed("Microphone permission is required.")
                return@post
            }

            val engine = recognizer ?: createRecognizer().also { recognizer = it }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            engine.startListening(intent)
        }
    }

    /** Stops capturing and lets the engine deliver its final hypothesis. */
    fun stopListening() {
        mainHandler.post { recognizer?.stopListening() }
    }

    /** Aborts the session without producing a result. */
    fun cancel() {
        mainHandler.post { recognizer?.cancel() }
    }

    /** Releases the engine. Call from `onDispose`. */
    fun release() {
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
        }
    }

    private fun createRecognizer(): SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }

    private fun Bundle?.bestTranscript(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
        SpeechRecognizer.ERROR_CLIENT -> "The recognizer was interrupted."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK -> "Network error while recognizing speech."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition timed out."
        SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "The recognizer is busy. Try again in a moment."
        SpeechRecognizer.ERROR_SERVER -> "The speech service reported an error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything."
        else -> "Speech recognition failed (code $error)."
    }
}
