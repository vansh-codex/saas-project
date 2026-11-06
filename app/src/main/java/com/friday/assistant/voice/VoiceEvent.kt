package com.friday.assistant.voice

/** Lifecycle events emitted while capturing a spoken utterance. */
sealed interface VoiceEvent {

    /** The engine is ready and audio capture has started. */
    data object Ready : VoiceEvent

    /** Interim hypothesis; useful for live UI feedback. */
    data class PartialTranscript(val text: String) : VoiceEvent

    /** The engine's final hypothesis for the utterance. */
    data class FinalTranscript(val text: String) : VoiceEvent

    /** Recognition failed; [message] is already user-facing. */
    data class Failed(val message: String) : VoiceEvent

    /** The session ended without a usable transcript. */
    data object Finished : VoiceEvent
}
