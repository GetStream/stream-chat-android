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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import android.Manifest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.delay

/**
 * Wrapper around Accompanist's [rememberPermissionState].
 *
 * In preview / Paparazzi environments (where there is no Activity context) this returns a
 * granted [AudioRecordingPermission].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun rememberAudioRecordingPermission(): AudioRecordingPermission {
    if (LocalInspectionMode.current) {
        return remember {
            AudioRecordingPermission(
                status = PermissionStatus.Granted,
                launchPermissionRequest = {},
                showRationale = {},
            )
        }
    }

    var showRationale by remember { mutableStateOf(false) }
    if (showRationale) {
        AudioRecordingPermissionRationale(
            onDismissRequest = { showRationale = false },
        )
    }

    var showDenied by remember { mutableStateOf(false) }
    val state = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { granted ->
        showDenied = !granted
    }
    if (showDenied) {
        SimpleDialog(
            title = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_title),
            message = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_message),
            onDismiss = { showDenied = false },
            onPositiveAction = { showDenied = false },
            showDismissButton = false,
        )
    }

    return remember(state) {
        AudioRecordingPermission(
            status = state.status,
            launchPermissionRequest = { state.launchPermissionRequest() },
            showRationale = { showRationale = true },
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
internal class AudioRecordingPermission(
    val status: PermissionStatus,
    val launchPermissionRequest: () -> Unit,
    val showRationale: () -> Unit,
)

/**
 * Returns `true` if the recording can proceed (permission granted).
 * Otherwise, triggers the appropriate permission request or rationale dialog and returns `false`.
 */
@OptIn(ExperimentalPermissionsApi::class)
internal fun AudioRecordingPermission.gateRecording(): Boolean = when {
    status.shouldShowRationale -> {
        showRationale()
        false
    }
    !status.isGranted -> {
        launchPermissionRequest()
        false
    }
    else -> true
}

/**
 * A popup anchored at [Alignment.BottomCenter] that auto-dismisses after [dismissTimeoutMs].
 */
@Composable
private fun TimedPopup(
    offsetY: Int,
    dismissTimeoutMs: Long = 1000L,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(dismissTimeoutMs)
        onDismissRequest()
    }
    Popup(
        onDismissRequest = onDismissRequest,
        offset = IntOffset(0, -offsetY),
        alignment = Alignment.BottomCenter,
    ) {
        content()
    }
}

@Composable
private fun AudioRecordingPermissionRationale(
    onDismissRequest: () -> Unit,
) {
    val theme = ChatTheme.messageComposerTheme.audioRecording.permissionRationale
    val offsetY = with(LocalDensity.current) { theme.containerBottomOffset.toPx().toInt() }
    TimedPopup(offsetY = offsetY, onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(theme.containerPadding),
            elevation = CardDefaults.cardElevation(defaultElevation = theme.containerElevation),
            shape = theme.containerShape,
            colors = CardDefaults.cardColors(containerColor = theme.containerColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(theme.contentHeight)
                    .padding(theme.contentPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    style = theme.textStyle,
                    text = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_message),
                )
                Spacer(modifier = Modifier.width(theme.contentSpace))
                val context = LocalContext.current
                TextButton(
                    modifier = Modifier,
                    onClick = { context.openSystemSettings() },
                ) {
                    Text(
                        style = theme.buttonTextStyle,
                        text = stringResource(id = R.string.stream_ui_message_composer_permissions_setting_button)
                            .uppercase(),
                    )
                }
            }
        }
    }
}
