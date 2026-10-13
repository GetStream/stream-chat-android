/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.ComposerInputTrailingContentParams
import io.getstream.chat.android.ai.compose.ui.component.SpeechToTextButton
import kotlinx.coroutines.launch

/**
 * Default implementation of the content inside the composer's input field: the speech-to-text
 * button, shown while no response is generating.
 */
@Composable
internal fun DefaultComposerInputTrailingContent(params: ComposerInputTrailingContentParams) {
    AnimatedContent(targetState = !params.isGenerating) { showVoiceButton ->
        if (showVoiceButton) {
            SpeechToTextButton(
                state = params.speechToTextState,
                onPermissionDenied = params.onPermissionDenied,
            )
        }
    }
}

/**
 * Shows a message explaining that the microphone permission is needed, with an action that opens
 * the app's settings.
 */
@Composable
internal fun rememberPermissionDeniedHandler(snackbarHostState: SnackbarHostState): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarMessage = stringResource(R.string.stream_ai_compose_composer_mic_permission_message)
    val actionLabel = stringResource(R.string.stream_ai_compose_composer_mic_permission_action)
    return {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = snackbarMessage,
                actionLabel = actionLabel,
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) {
                context.openSettings()
            }
        }
    }
}

private fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
