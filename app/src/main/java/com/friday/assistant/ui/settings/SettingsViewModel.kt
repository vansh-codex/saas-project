package com.friday.assistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.friday.assistant.data.SettingsRepository
import com.friday.assistant.data.SettingsSnapshot
import com.friday.assistant.di.AppContainer
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<SettingsSnapshot> = settingsRepository.settings

    fun saveGeminiApiKey(apiKey: String) {
        if (apiKey.isBlank()) return
        settingsRepository.saveGeminiApiKey(apiKey)
    }

    fun clearGeminiApiKey() {
        settingsRepository.clearGeminiApiKey()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container.settingsRepository) }
        }
    }
}
