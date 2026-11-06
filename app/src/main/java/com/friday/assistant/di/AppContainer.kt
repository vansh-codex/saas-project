package com.friday.assistant.di

import android.content.Context
import com.friday.assistant.ai.AssistantEngine
import com.friday.assistant.ai.GeminiClient
import com.friday.assistant.data.SecureKeyStore
import com.friday.assistant.data.SettingsRepository
import com.friday.assistant.tools.ToolRegistry
import com.friday.assistant.voice.SpeechRecognizerManager
import com.friday.assistant.voice.TextToSpeechManager

/**
 * Manual dependency container. Keeps construction of the app's collaborators in one place
 * without pulling in a DI framework.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val secureKeyStore: SecureKeyStore = SecureKeyStore(appContext)

    val settingsRepository: SettingsRepository = SettingsRepository(secureKeyStore)

    /**
     * The key is resolved lazily on every request so edits made on the Settings screen take
     * effect immediately and no secret is ever held in a long-lived field.
     */
    val geminiClient: GeminiClient = GeminiClient(apiKeyProvider = { settingsRepository.geminiApiKey })

    val toolRegistry: ToolRegistry = ToolRegistry.builtIn()

    val assistantEngine: AssistantEngine = AssistantEngine(geminiClient, toolRegistry)

    val textToSpeechManager: TextToSpeechManager = TextToSpeechManager(appContext)

    val speechRecognizerManager: SpeechRecognizerManager = SpeechRecognizerManager(appContext)
}
