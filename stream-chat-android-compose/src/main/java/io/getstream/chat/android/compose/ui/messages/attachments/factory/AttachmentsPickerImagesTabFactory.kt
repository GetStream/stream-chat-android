/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.components.attachments.images.ImagesPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * Holds the information required to add support for "images" tab in the attachment picker.
 */
public class AttachmentsPickerImagesTabFactory : AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = Images

    /**
     * Emits an image icon for this tab.
     *
     * @param isEnabled If the tab is enabled.
     * @param isSelected If the tab is selected.
     */
    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_image_picker),
            contentDescription = stringResource(id = R.string.stream_compose_images_option),
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    /**
     * Emits content that allows users to pick images in this tab.
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
        var storagePermissionRequested by rememberSaveable { mutableStateOf(false) }
        val storagePermissionState =
            rememberMultiplePermissionsState(
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                    )
                } else {
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                },
            ) {
                storagePermissionRequested = true
            }

        val context = LocalContext.current
        val storageHelper: StorageHelperWrapper =
            remember { StorageHelperWrapper(context, StorageHelper(), AttachmentFilter()) }

        when (storagePermissionState.allPermissionsGranted) {
            true -> {
                ImagesPicker(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 2.dp,
                        end = 2.dp,
                        bottom = 2.dp,
                    ),
                    images = attachments,
                    onImageSelected = onAttachmentItemSelected,
                )
            }

            else -> {
                val revokedPermissionState = storagePermissionState.revokedPermissions.first()
                MissingPermissionContent(revokedPermissionState)
            }
        }

        val hasPermission = storagePermissionState.allPermissionsGranted

        LaunchedEffect(storagePermissionState.allPermissionsGranted) {
            if (storagePermissionState.allPermissionsGranted) {
                onAttachmentsChanged(
                    storageHelper.getMedia().map { AttachmentPickerItemState(it, false) },
                )
            }
        }

        LaunchedEffect(Unit) {
            if (!hasPermission && !storagePermissionRequested) {
                storagePermissionState.launchMultiplePermissionRequest()
            }
        }
    }
}
