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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared
import java.io.File

/**
 * Holds the information required to add support for "media capture" tab in the attachment picker.
 *
 * @property pickerMediaMode define which media type will be allowed.
 */
public class AttachmentsPickerMediaCaptureTabFactory(private val pickerMediaMode: PickerMediaMode) :
    AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = MediaCapture

    /**
     * Emits a camera icon for this tab.
     *
     * @param isEnabled If the tab is enabled.
     * @param isSelected If the tab is selected.
     */
    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            modifier = Modifier.testTag("Stream_AttachmentPickerMediaCaptureTab"),
            painter = painterResource(id = R.drawable.stream_compose_ic_media_picker),
            contentDescription = stringResource(id = R.string.stream_compose_capture_option),
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    /**
     * Emits content that allows users to start media capture.
     *
     * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
     * @param attachments The list of attachments to display.
     * @param onAttachmentsChanged Handler to set the loaded list of attachments to display.
     * @param onAttachmentItemSelected Handler when the item selection state changes.
     * @param onAttachmentsSubmitted Handler to submit the selected attachments to the message composer.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        val context = LocalContext.current

        val requiresCameraPermission = context.isPermissionDeclared(Manifest.permission.CAMERA)

        val cameraPermissionState =
            if (requiresCameraPermission) rememberPermissionState(permission = Manifest.permission.CAMERA) else null

        val contract = remember { CaptureMediaContract(pickerMediaMode.mode) }
        val mediaCaptureResultLauncher =
            rememberLauncherForActivityResult(contract = contract) { file: File? ->
                val attachments = if (file == null) {
                    emptyList()
                } else {
                    listOf(AttachmentMetaData(context, file))
                }

                onAttachmentsSubmitted(attachments)
            }

        if (cameraPermissionState == null || cameraPermissionState.status == PermissionStatus.Granted) {
            Box(modifier = Modifier.fillMaxSize()) {
                LaunchedEffect(Unit) {
                    mediaCaptureResultLauncher.launch(Unit)
                }
            }
        } else if (cameraPermissionState.status is PermissionStatus.Denied) {
            MissingPermissionContent(cameraPermissionState)
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
}
