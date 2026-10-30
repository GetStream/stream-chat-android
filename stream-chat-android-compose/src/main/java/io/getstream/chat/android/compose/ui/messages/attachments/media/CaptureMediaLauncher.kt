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

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.log.taggedLogger
import java.io.File

/**
 * Launches a camera app to capture a photo or a video.
 *
 * When several camera apps can capture the media allowed by the mode, [launch] asks the user to pick
 * one through [CaptureMediaTargetSheet], which must be placed in the composition next to the launcher.
 */
@Stable
internal class CaptureMediaLauncher(
    val contract: CaptureMediaContract,
    private val activityLauncher: ManagedActivityResultLauncher<CaptureMediaContract.Target, File?>,
    private val context: Context,
    private val pickingTarget: MutableState<Boolean>,
) {

    private val logger by taggedLogger("Chat:CaptureMediaLauncher")

    var isPickingTarget: Boolean
        get() = pickingTarget.value
        set(value) {
            pickingTarget.value = value
        }

    /**
     * Opens the camera app, or asks the user to pick one when several are available.
     */
    fun launch() {
        val targets = contract.targets(context)
        when (targets.size) {
            0 -> logger.w { "[launch] No camera app can capture the requested media" }
            1 -> launch(targets.single())
            else -> isPickingTarget = true
        }
    }

    fun launch(target: CaptureMediaContract.Target) {
        isPickingTarget = false
        activityLauncher.launch(target)
    }
}

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
 * @return A [CaptureMediaLauncher] to launch the media capture activity.
 *
 * @see CaptureMediaContract
 */
@Composable
internal fun rememberCaptureMediaLauncher(
    mode: CaptureMediaContract.Mode,
    onResult: (File) -> Unit,
): CaptureMediaLauncher {
    val context = LocalContext.current
    val contract = rememberSaveable(mode, saver = captureMediaContractSaver(mode)) {
        CaptureMediaContract(mode)
    }
    val activityLauncher = rememberLauncherForActivityResult(contract) { file ->
        file?.let(onResult)
    }
    val pickingTarget = rememberSaveable { mutableStateOf(false) }
    return remember(contract, activityLauncher, context) {
        CaptureMediaLauncher(contract, activityLauncher, context, pickingTarget)
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
