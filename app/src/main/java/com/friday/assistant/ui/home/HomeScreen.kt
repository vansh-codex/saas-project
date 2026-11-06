package com.friday.assistant.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.friday.assistant.R
import com.friday.assistant.di.AppContainer
import com.friday.assistant.voice.VoiceEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val recognizer = remember(container) { container.speechRecognizerManager }
    val textToSpeech = remember(container) { container.textToSpeechManager }

    // The recognizer is app-scoped; releasing on dispose lets the next session re-create it.
    DisposableEffect(recognizer) {
        onDispose { recognizer.release() }
    }

    LaunchedEffect(recognizer) {
        recognizer.events.collect { event ->
            when (event) {
                VoiceEvent.Ready -> viewModel.onListeningStarted()
                is VoiceEvent.PartialTranscript -> viewModel.onPartialTranscript(event.text)
                is VoiceEvent.FinalTranscript -> viewModel.submit(event.text)
                is VoiceEvent.Failed -> viewModel.onVoiceFailure(event.message)
                VoiceEvent.Finished -> viewModel.onListeningStopped()
                null -> Unit
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.speakRequests.collect { reply -> textToSpeech.speak(reply) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onListeningStarted()
            recognizer.startListening()
        } else {
            viewModel.onVoiceFailure(MICROPHONE_PERMISSION_REQUIRED)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.assistant_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.assistant_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!uiState.hasApiKey) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_key_missing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        TextButton(onClick = onOpenSettings) {
                            Text(text = stringResource(R.string.settings_title))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MessageCard(
                title = stringResource(
                    if (uiState.isListening) R.string.assistant_listening else R.string.assistant_idle,
                ),
                body = uiState.transcript,
            )

            Spacer(modifier = Modifier.height(12.dp))

            MessageCard(
                title = stringResource(
                    if (uiState.isThinking) R.string.assistant_thinking else R.string.assistant_title,
                ),
                body = uiState.reply,
                trailing = if (uiState.isThinking) {
                    { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
                } else {
                    null
                },
            )

            uiState.error?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (uiState.isListening) {
                        recognizer.stopListening()
                        viewModel.onListeningStopped()
                    } else if (recognizer.hasMicrophonePermission) {
                        viewModel.onListeningStarted()
                        recognizer.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(
                        if (uiState.isListening) R.string.assistant_stop else R.string.assistant_start,
                    ),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

private const val MICROPHONE_PERMISSION_REQUIRED = "Microphone permission is required."

@Composable
private fun MessageCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                trailing?.invoke()
            }
            if (body.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
