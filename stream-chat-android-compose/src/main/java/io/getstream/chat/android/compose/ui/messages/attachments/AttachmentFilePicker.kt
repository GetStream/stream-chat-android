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

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.components.attachments.files.FilesPicker
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsProcessingViewModel
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsProcessingViewModelFactory
import io.getstream.chat.android.compose.ui.messages.attachments.factory.NoStorageAccessContent
import io.getstream.chat.android.compose.ui.messages.attachments.factory.PermissionPermanentlyDeniedSnackBar
import io.getstream.chat.android.compose.ui.messages.attachments.factory.filesAccessAsState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.ui.common.permissions.FilesAccess
import io.getstream.chat.android.ui.common.permissions.Permissions
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AttachmentFilePicker(
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val processingViewModel = viewModel<AttachmentsProcessingViewModel>(
        factory = AttachmentsProcessingViewModelFactory(StorageHelperWrapper(context.applicationContext)),
    )
    LaunchedEffect(processingViewModel) {
        processingViewModel.attachmentsMetadataFromUris.collectLatest { metadata ->
            // Check if some of the files were filtered out due to upload config
            if (metadata.uris.size != metadata.attachmentsMetadata.size) {
                Toast.makeText(
                    context,
                    R.string.stream_compose_message_composer_file_not_supported,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            onAttachmentsSubmitted(metadata.attachmentsMetadata)
        }
    }
    LaunchedEffect(processingViewModel) {
        processingViewModel.filesMetadata.collectLatest { metaData ->
            val items = metaData.map { AttachmentPickerItemState(attachmentMetaData = it) }
            onAttachmentsChanged(items)
        }
    }
    var showPermanentlyDeniedSnackBar by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (Permissions.isPermanentlyDenied(context, result)) {
                showPermanentlyDeniedSnackBar = true
            }
        }
    val filesAccess by filesAccessAsState(context, lifecycleOwner) { value ->
        if (value != FilesAccess.DENIED) {
            processingViewModel.getFilesAsync()
        }
    }

    // Content
    FilesAccessContent(
        filesAccess = filesAccess,
        onRequestFilesAccess = { permissionLauncher.launch(Permissions.filesPermissions()) },
        onRequestVisualMediaAccess = { permissionLauncher.launch(Permissions.visualMediaPermissions()) },
        onRequestAudioAccess = { permissionLauncher.launch(Permissions.audioPermissions()) },
        filePicker = {
            FilesPicker(
                files = attachments,
                onItemSelected = onAttachmentItemSelected,
                onBrowseFilesResult = { uris ->
                    processingViewModel.getAttachmentsMetadataFromUrisAsync(uris)
                },
            )
        },
    )

    // Access permanently denied snackbar
    val snackBarHostState = remember { SnackbarHostState() }
    PermissionPermanentlyDeniedSnackBar(
        hostState = snackBarHostState,
        onActionClick = context::openSystemSettings,
    )
    val snackbarMessage = stringResource(id = R.string.stream_ui_message_composer_permission_setting_message)
    val snackbarAction = stringResource(id = R.string.stream_ui_message_composer_permissions_setting_button)
    LaunchedEffect(showPermanentlyDeniedSnackBar) {
        if (showPermanentlyDeniedSnackBar) {
            snackBarHostState.showSnackbar(
                message = snackbarMessage,
                actionLabel = snackbarAction,
                duration = SnackbarDuration.Short,
            )
            showPermanentlyDeniedSnackBar = false
        }
    }
}

@Composable
private fun FilesAccessContent(
    filesAccess: FilesAccess,
    filePicker: @Composable () -> Unit,
    onRequestFilesAccess: () -> Unit,
    onRequestVisualMediaAccess: () -> Unit,
    onRequestAudioAccess: () -> Unit,
) {
    when (filesAccess) {
        FilesAccess.DENIED -> {
            NoStorageAccessContent(onRequestAccessClick = onRequestFilesAccess)
        }

        FilesAccess.PARTIAL_VISUAL -> {
            Column {
                GrantAudioAccessButton(onClick = onRequestAudioAccess)
                AllowMoreVisualMediaButton(onClick = onRequestVisualMediaAccess)
                filePicker()
            }
        }

        FilesAccess.FULL_VISUAL -> {
            Column {
                GrantAudioAccessButton(onClick = onRequestAudioAccess)
                filePicker()
            }
        }

        FilesAccess.AUDIO -> {
            Column {
                GrantVisualMediaAccessButton(onClick = onRequestVisualMediaAccess)
                filePicker()
            }
        }

        FilesAccess.AUDIO_AND_PARTIAL_VISUAL -> {
            Column {
                AllowMoreVisualMediaButton(onClick = onRequestVisualMediaAccess)
                filePicker()
            }
        }

        FilesAccess.AUDIO_AND_FULL_VISUAL -> {
            filePicker()
        }
    }
}

@Composable
private fun GrantVisualMediaAccessButton(onClick: () -> Unit) {
    RequestAdditionalAccessButton(
        textId = R.string.stream_ui_message_composer_permissions_files_allow_visual_media_access,
        contentDescriptionId = R.string.stream_ui_message_composer_permissions_files_allow_visual_media_access,
        onClick = onClick,
    )
}

@Composable
private fun AllowMoreVisualMediaButton(onClick: () -> Unit) {
    RequestAdditionalAccessButton(
        textId = R.string.stream_ui_message_composer_permissions_files_allow_more_visual_media,
        contentDescriptionId = R.string.stream_ui_message_composer_permissions_files_allow_more_visual_media,
        onClick = onClick,
    )
}

@Composable
private fun GrantAudioAccessButton(onClick: () -> Unit) {
    RequestAdditionalAccessButton(
        textId = R.string.stream_ui_message_composer_permissions_files_allow_audio_access,
        contentDescriptionId = R.string.stream_ui_message_composer_permissions_files_allow_audio_access,
        onClick = onClick,
    )
}

@Composable
private fun RequestAdditionalAccessButton(
    @StringRes textId: Int,
    @StringRes contentDescriptionId: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            text = stringResource(id = textId),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
        )

        IconButton(
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_more_files),
                    contentDescription = stringResource(id = contentDescriptionId),
                    tint = ChatTheme.colors.primaryAccent,
                )
            },
            onClick = onClick,
        )
    }
}
