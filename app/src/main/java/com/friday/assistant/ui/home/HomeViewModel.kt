package com.friday.assistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.friday.assistant.ai.AiMessage
import com.friday.assistant.ai.AiRole
import com.friday.assistant.ai.AssistantEngine
import com.friday.assistant.data.SettingsRepository
import com.friday.assistant.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val transcript: String = "",
    val reply: String = "",
    val error: String? = null,
    val hasApiKey: Boolean = false,
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val assistantEngine: AssistantEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Replies that should be spoken aloud. Consumed once by the screen. */
    private val _speakRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val speakRequests: SharedFlow<String> = _speakRequests.asSharedFlow()

    private val history = mutableListOf<AiMessage>()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { snapshot ->
                _uiState.update { it.copy(hasApiKey = snapshot.hasGeminiApiKey) }
            }
        }
    }

    fun onListeningStarted() {
        _uiState.update { it.copy(isListening = true, error = null, transcript = "") }
    }

    fun onListeningStopped() {
        _uiState.update { it.copy(isListening = false) }
    }

    fun onPartialTranscript(text: String) {
        _uiState.update { it.copy(transcript = text) }
    }

    fun onVoiceFailure(message: String) {
        _uiState.update { it.copy(isListening = false, error = message) }
    }

    /** Sends [prompt] to the assistant and speaks the answer once it arrives. */
    fun submit(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        _uiState.update {
            it.copy(
                transcript = trimmed,
                isListening = false,
                isThinking = true,
                error = null,
            )
        }

        viewModelScope.launch {
            assistantEngine.ask(trimmed, history.toList()).fold(
                onSuccess = { reply ->
                    history += AiMessage(AiRole.USER, trimmed)
                    history += AiMessage(AiRole.ASSISTANT, reply)
                    _uiState.update { it.copy(reply = reply, isThinking = false) }
                    _speakRequests.tryEmit(reply)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isThinking = false,
                            error = throwable.message ?: "Something went wrong.",
                        )
                    }
                },
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    settingsRepository = container.settingsRepository,
                    assistantEngine = container.assistantEngine,
                )
            }
        }
    }
}
