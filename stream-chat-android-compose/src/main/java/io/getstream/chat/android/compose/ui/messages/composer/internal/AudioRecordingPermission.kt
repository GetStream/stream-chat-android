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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                statusProvider = { PermissionStatus.Granted },
                launchPermissionRequest = {},
                showRationale = {},
                rationaleSnackbarHostState = SnackbarHostState(),
            )
        }
    }

    val rationaleState = rememberPermissionRationale()

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

    return remember(state, rationaleState) {
        AudioRecordingPermission(
            statusProvider = { state.status },
            launchPermissionRequest = { state.launchPermissionRequest() },
            showRationale = { rationaleState.show() },
            rationaleSnackbarHostState = rationaleState.snackbarHostState,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
internal class AudioRecordingPermission(
    private val statusProvider: () -> PermissionStatus,
    val launchPermissionRequest: () -> Unit,
    val showRationale: () -> Unit,
    val rationaleSnackbarHostState: SnackbarHostState,
) {
    /** Current permission status, read fresh on every access. */
    val status: PermissionStatus get() = statusProvider()
}

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

/** State holder for the permission rationale snackbar. */
private class PermissionRationaleState(
    val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val message: String,
    private val actionLabel: String,
    private val onAction: () -> Unit,
) {
    fun show() {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction()
            }
        }
    }
}

@Composable
private fun rememberPermissionRationale(): PermissionRationaleState {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val message = stringResource(R.string.stream_ui_message_composer_permission_audio_record_message)
    val actionLabel = stringResource(R.string.stream_ui_message_composer_permissions_setting_button)
    return remember(snackbarHostState, scope, context, message, actionLabel) {
        PermissionRationaleState(
            snackbarHostState = snackbarHostState,
            scope = scope,
            message = message,
            actionLabel = actionLabel,
            onAction = { context.openSystemSettings() },
        )
    }
}
