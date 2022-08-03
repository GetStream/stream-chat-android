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

package io.getstream.chat.android.compose.ui.messages.attachments.facotry

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.ui.components.attachments.files.FilesPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Holds the information required to add support for "files" tab in the attachment picker.
 */
public class AttachmentsPickerFilesTabFactory : AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = Files

    /**
     * Emits a file icon for this tab.
     */
    @Composable
    override fun pickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_file_picker),
            contentDescription = stringResource(id = R.string.stream_compose_files_option),
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    /**
     * Emits a content for this tab.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun pickerTabContent(
        attachments: List<AttachmentPickerItemState>,
        onAttachmentSelected: (AttachmentPickerItemState) -> Unit,
        onStart: () -> Unit,
        onAttachmentsSelected: (List<AttachmentMetaData>) -> Unit,
        helper: (List<Uri>) -> List<AttachmentMetaData>,
    ) {
        var storagePermissionRequested by rememberSaveable { mutableStateOf(false) }
        val storagePermissionState =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
                storagePermissionRequested = true
            }

        val context = LocalContext.current

        if (storagePermissionState != null) {
            when (storagePermissionState.status) {
                PermissionStatus.Granted -> {

                    FilesPicker(
                        files = attachments,
                        onItemSelected = onAttachmentSelected,
                        onBrowseFilesResult = { uris ->
                            val attachments = helper(uris)

                            // Check if some of the files were filtered out due to upload config
                            if (uris.size != attachments.size) {
                                Toast.makeText(
                                    context,
                                    R.string.stream_compose_message_composer_file_not_supported,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            onAttachmentsSelected(attachments)
                        }
                    )
                }
                is PermissionStatus.Denied -> MissingPermissionContent(storagePermissionState)
            }
        }

        val hasPermission = storagePermissionState?.status?.isGranted ?: true

        LaunchedEffect(storagePermissionState.status.isGranted) {
            if (storagePermissionState.status.isGranted) {

                onStart()
            }
        }

        LaunchedEffect(Unit) {
            if (!hasPermission && !storagePermissionRequested) {
                storagePermissionState.launchPermissionRequest()
            }
        }
    }
}
