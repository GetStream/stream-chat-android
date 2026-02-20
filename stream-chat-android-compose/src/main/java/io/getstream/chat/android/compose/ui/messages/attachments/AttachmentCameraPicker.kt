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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CaptureMode
import io.getstream.chat.android.compose.ui.messages.attachments.media.rememberCaptureMediaLauncher
import io.getstream.chat.android.compose.ui.messages.attachments.permission.RequiredCameraPermission
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun AttachmentCameraPicker(
    pickerMode: CameraPickerMode,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit = {},
) {
    val context = LocalContext.current

    val captureMediaMode = pickerMode.toCaptureMediaMode()
    val captureMediaLauncher = rememberCaptureMediaLauncher(captureMediaMode) { file ->
        val attachments = listOf(AttachmentMetaData(context, file))
        onAttachmentsSubmitted(attachments)
    }
    // Handling camera permission flow is only required if the host application has declared the permission.
    val requiresCameraPermission = context.isPermissionDeclared(Manifest.permission.CAMERA)
    val cameraPermissionState = if (requiresCameraPermission) {
        rememberPermissionState(Manifest.permission.CAMERA)
    } else {
        null
    }
    var showRequiredCameraPermission by remember { mutableStateOf(false) }
    LaunchedEffect(cameraPermissionState?.status) {
        if (cameraPermissionState == null || cameraPermissionState.status.isGranted) {
            showRequiredCameraPermission = false
            captureMediaLauncher.launch(Unit)
        } else if (cameraPermissionState.status.shouldShowRationale) {
            showRequiredCameraPermission = true
        } else {
            showRequiredCameraPermission = false
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (showRequiredCameraPermission) {
        RequiredCameraPermission()
    } else {
        AttachmentCameraPickerContent { captureMediaLauncher?.launch(Unit) }
    }
}

@Composable
private fun AttachmentCameraPickerContent(
    onCaptureClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacing2xl,
                end = StreamTokens.spacing2xl,
                bottom = StreamTokens.spacing3xl,
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs, Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_attachment_camera_picker),
                contentDescription = null,
                tint = ChatTheme.colors.textTertiary,
            )
            Text(
                text = stringResource(id = R.string.stream_compose_attachment_camera_picker_content),
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            onClick = onCaptureClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ChatTheme.colors.buttonSecondaryText,
            ),
        ) {
            Text(text = stringResource(id = R.string.stream_compose_attachment_camera_picker))
        }
    }
}

internal fun CameraPickerMode.toCaptureMediaMode(): CaptureMediaContract.Mode =
    when (captureMode) {
        CaptureMode.Photo -> CaptureMediaContract.Mode.PHOTO
        CaptureMode.Video -> CaptureMediaContract.Mode.VIDEO
        CaptureMode.PhotoAndVideo -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
    }

@Preview(showBackground = true)
@Composable
private fun AttachmentCameraPickerPreview() {
    ChatTheme {
        AttachmentCameraPicker()
    }
}

@Composable
internal fun AttachmentCameraPicker() {
    AttachmentCameraPicker(
        pickerMode = CameraPickerMode(),
    )
}
