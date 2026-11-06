package com.friday.assistant.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.friday.assistant.R
import com.friday.assistant.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container)),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var draftKey by rememberSaveable { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.settings_back),
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
        ) {
            Text(
                text = stringResource(R.string.settings_api_key_label),
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_api_key_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = draftKey,
                onValueChange = { draftKey = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(R.string.settings_api_key_label)) },
                placeholder = { Text(text = stringResource(R.string.settings_api_key_hint)) },
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                trailingIcon = {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(
                            text = stringResource(
                                if (showKey) R.string.settings_hide_key else R.string.settings_show_key,
                            ),
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(statusText(settings.hasGeminiApiKey)),
                style = MaterialTheme.typography.bodySmall,
                color = if (settings.hasGeminiApiKey) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        viewModel.saveGeminiApiKey(draftKey)
                        draftKey = ""
                    },
                    enabled = draftKey.isNotBlank(),
                ) {
                    Text(text = stringResource(R.string.settings_save))
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.clearGeminiApiKey()
                        draftKey = ""
                    },
                    enabled = settings.hasGeminiApiKey,
                ) {
                    Text(text = stringResource(R.string.settings_clear))
                }
            }
        }
    }
}

@StringRes
private fun statusText(hasKey: Boolean): Int =
    if (hasKey) R.string.settings_key_configured else R.string.settings_key_missing
