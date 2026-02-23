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
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import java.io.File

/**
 * Creates and remembers a launcher for capturing media (photo and/or video) using the device camera.
 *
 * The contract's destination file paths are persisted via [rememberSaveable] so that
 * captured media can be recovered after process death (e.g. "Don't keep activities").
 *
 * @param mode The capture mode determining what media types can be captured.
 * @param onResult Callback invoked when media capture completes successfully. Receives a [File]
 * representing the captured media. Not called if the user cancels the capture.
 *
 * @return A [ManagedActivityResultLauncher] to launch the media capture activity.
 *
 * @see CaptureMediaContract
 */
@Composable
public fun rememberCaptureMediaLauncher(
    mode: CaptureMediaContract.Mode,
    onResult: (File) -> Unit,
): ManagedActivityResultLauncher<Unit, File?> {
    val contract = rememberSaveable(mode, saver = captureMediaContractSaver(mode)) {
        CaptureMediaContract(mode)
    }
    return rememberLauncherForActivityResult(contract) { file ->
        file?.let(onResult)
    }
}

private fun captureMediaContractSaver(mode: CaptureMediaContract.Mode) = mapSaver(
    save = { contract ->
        buildMap {
            contract.pictureFile?.absolutePath?.let { put(KeyPicturePath, it) }
            contract.videoFile?.absolutePath?.let { put(KeyVideoPath, it) }
        }
    },
    restore = { map ->
        CaptureMediaContract(
            mode = mode,
            pictureFile = (map[KeyPicturePath] as? String)?.let(::File),
            videoFile = (map[KeyVideoPath] as? String)?.let(::File),
        )
    },
)

private const val KeyPicturePath = "picture_path"
private const val KeyVideoPath = "video_path"
