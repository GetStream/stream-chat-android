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

package io.getstream.chat.android.compose.ui.messages.attachments

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import io.getstream.chat.android.compose.ui.messages.attachments.media.rememberCaptureMediaLauncher
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun AttachmentCameraPicker(
    pickerMediaMode: PickerMediaMode,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
) {
    val context = LocalContext.current

    // Handling camera permission flow is required if the host application has declared the permission.
    val requiresCameraPermission = context.isPermissionDeclared(Manifest.permission.CAMERA)

    val cameraPermissionState = if (requiresCameraPermission) {
        rememberPermissionState(permission = Manifest.permission.CAMERA)
    } else {
        null
    }

    val mediaCaptureResultLauncher = rememberCaptureMediaLauncher(
        photo = pickerMediaMode == PickerMediaMode.PHOTO || pickerMediaMode == PickerMediaMode.PHOTO_AND_VIDEO,
        video = pickerMediaMode == PickerMediaMode.VIDEO || pickerMediaMode == PickerMediaMode.PHOTO_AND_VIDEO,
    ) { file ->
        val attachments = listOf(AttachmentMetaData(context, file))
        onAttachmentsSubmitted(attachments)
    }

    if (cameraPermissionState == null || cameraPermissionState.status == PermissionStatus.Granted) {
        Box(modifier = Modifier.fillMaxSize()) {
            LaunchedEffect(Unit) {
                mediaCaptureResultLauncher?.launch(Unit)
            }
        }
    } else if (cameraPermissionState.status is PermissionStatus.Denied) {
        RequiredCameraPermission()
    }
}

/**
 * Define which media type will be allowed.
 */
public enum class PickerMediaMode {
    PHOTO,
    VIDEO,
    PHOTO_AND_VIDEO,
}

/**
 * Map [PickerMediaMode] into [CaptureMediaContract.Mode]
 */
private val PickerMediaMode.mode: CaptureMediaContract.Mode
    get() = when (this) {
        PickerMediaMode.PHOTO -> CaptureMediaContract.Mode.PHOTO
        PickerMediaMode.VIDEO -> CaptureMediaContract.Mode.VIDEO
        PickerMediaMode.PHOTO_AND_VIDEO -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
    }
