package com.friday.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Thin wrapper around the platform [TextToSpeech] engine used to speak Friday's replies.
 */
class TextToSpeechManager(context: Context) {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var engine: TextToSpeech? = null

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.getDefault()
                _isReady.value = true
            } else {
                _isReady.value = false
            }
        }
    }

    /** Speaks [text], interrupting anything already being spoken. */
    fun speak(text: String) {
        if (text.isBlank()) return
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        engine?.stop()
    }

    /** Releases the engine. Call when the owning component is destroyed. */
    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        _isReady.value = false
    }

    private companion object {
        const val UTTERANCE_ID = "friday-utterance"
    }
}
