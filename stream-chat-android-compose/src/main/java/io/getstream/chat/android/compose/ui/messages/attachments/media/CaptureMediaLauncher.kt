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

package io.getstream.chat.android.compose.ui.messages.attachments.media

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import java.io.File

/**
 * Creates and remembers a process-death-safe launcher for capturing media (photo and/or video)
 * using the device camera.
 *
 * Destination file paths are persisted via [rememberSaveable]. When the hosting activity is
 * destroyed and recreated (e.g. under "Don't keep activities"), the paths are restored on the
 * [CaptureMediaContract] before the pending result is delivered, ensuring the captured file is
 * correctly resolved.
 *
 * @param photo If `true`, enables photo capture capability. When both [photo] and [video] are
 * `true`, the user will be able to capture both types of media.
 * @param video If `true`, enables video capture capability. When both [photo] and [video] are
 * `true`, the user will be able to capture both types of media.
 * @param onResult Callback invoked when media capture completes successfully. Receives a [File]
 * representing the captured media. This callback is only invoked if the user successfully captures
 * media; it is not called if the user cancels the capture.
 *
 * @return A [ManagedActivityResultLauncher] that can be used to launch the media capture activity,
 * or `null` if both [photo] and [video] are `false` (no valid capture mode). The launcher accepts
 * `Unit` as input and produces a nullable [File] as output.
 *
 * @see CaptureMediaContract
 *
 * Example usage:
 * ```kotlin
 * val captureMediaLauncher = rememberCaptureMediaLauncher(
 *     photo = true,
 *     video = true,
 *     onResult = { file ->
 *         // Handle captured media file
 *     }
 * )
 *
 * // Launch the camera
 * captureMediaLauncher?.launch(Unit)
 * ```
 *
 * Note: This function doesn't check for camera permissions. Ensure that the necessary permissions
 * are granted before invoking the launcher.
 */
@Composable
public fun rememberCaptureMediaLauncher(
    photo: Boolean,
    video: Boolean,
    onResult: (File) -> Unit,
): ManagedActivityResultLauncher<Unit, File?>? =
    rememberCaptureMediaLauncherInternal(photo, video) { file ->
        file?.let(onResult)
    }

/**
 * Internal cancel-aware variant of [rememberCaptureMediaLauncher].
 *
 * Unlike the public API, this variant invokes [onResult] with `null` when the user cancels the
 * capture, allowing callers to react to cancellation (e.g. dismiss the picker).
 */
@Composable
internal fun rememberCancelAwareCaptureMediaLauncher(
    photo: Boolean,
    video: Boolean,
    onResult: (File?) -> Unit,
): ManagedActivityResultLauncher<Unit, File?>? =
    rememberCaptureMediaLauncherInternal(photo, video, onResult)

@Composable
private fun rememberCaptureMediaLauncherInternal(
    photo: Boolean,
    video: Boolean,
    onResult: (File?) -> Unit,
): ManagedActivityResultLauncher<Unit, File?>? {
    val mode = resolveMediaPickerMode(photo, video) ?: return null

    var pictureFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    var videoFilePath by rememberSaveable { mutableStateOf<String?>(null) }

    val contract = remember(mode) {
        CaptureMediaContract(mode) { createdPicture, createdVideo ->
            pictureFilePath = createdPicture?.absolutePath
            videoFilePath = createdVideo?.absolutePath
        }
    }

    // Restore file references on the contract after process death.
    // Runs during composition, before rememberLauncherForActivityResult re-registers
    // in its DisposableEffect and dispatches pending results.
    pictureFilePath?.let { contract.pictureFile = File(it) }
    videoFilePath?.let { contract.videoFile = File(it) }

    return rememberLauncherForActivityResult(contract) { file ->
        onResult(file)
        pictureFilePath = null
        videoFilePath = null
    }
}

private fun resolveMediaPickerMode(photo: Boolean, video: Boolean): CaptureMediaContract.Mode? =
    when {
        photo && video -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
        photo -> CaptureMediaContract.Mode.PHOTO
        video -> CaptureMediaContract.Mode.VIDEO
        else -> null
    }
