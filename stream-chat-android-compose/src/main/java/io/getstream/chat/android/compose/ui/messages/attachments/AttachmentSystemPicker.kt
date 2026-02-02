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
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.Commands
import io.getstream.chat.android.compose.state.messages.attachments.CustomPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.compose.state.messages.attachments.System
import io.getstream.chat.android.compose.ui.components.FullscreenDialog
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerCreatePollClick
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsMetadataFromUris
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsProcessingViewModel
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsProcessingViewModelFactory
import io.getstream.chat.android.compose.ui.messages.attachments.media.rememberCaptureMediaLauncher
import io.getstream.chat.android.compose.ui.messages.attachments.poll.CreatePollScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.permissions.SystemAttachmentsPickerConfig
import io.getstream.chat.android.ui.common.permissions.toContractVisualMediaType
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AttachmentSystemPickerContent(
    config: SystemAttachmentsPickerConfig = SystemAttachmentsPickerConfig(),
    channel: Channel,
    messageMode: MessageMode,
    commands: List<Command>,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
) {
    val context = LocalContext.current

    val processingViewModel = viewModel<AttachmentsProcessingViewModel>(
        factory = AttachmentsProcessingViewModelFactory(StorageHelperWrapper(context.applicationContext)),
    )

    val filePickerLauncher = rememberFilePickerLauncher { uri ->
        val uris = listOf(uri)
        processingViewModel.getAttachmentsMetadataFromUrisAsync(uris) { metadata ->
            showErrorIfNeeded(context, metadata)
            onAttachmentsSubmitted(metadata.attachmentsMetadata)
        }
    }

    val mediaPickerLauncher = rememberVisualMediaPickerLauncher(config.visualMediaAllowMultiple) { uris ->
        processingViewModel.getAttachmentsMetadataFromUrisAsync(uris) { metadata ->
            showErrorIfNeeded(context, metadata)
            onAttachmentsSubmitted(metadata.attachmentsMetadata)
        }
    }

    val captureMediaLauncher = rememberCaptureMediaLauncher(
        photo = config.captureImageAllowed,
        video = config.captureVideoAllowed,
    ) { file ->
        val attachments = listOf(AttachmentMetaData(context, file))
        onAttachmentsSubmitted(attachments)
    }
    // Handling camera permission flow is only required if the host application has declared the permission.
    val requiresCameraPermission = context.isPermissionDeclared(Manifest.permission.CAMERA)
    val cameraPermissionState = if (requiresCameraPermission) {
        rememberPermissionState(Manifest.permission.CAMERA) { granted ->
            if (granted) captureMediaLauncher?.launch(Unit)
        }
    } else {
        null
    }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    LaunchedEffect(cameraPermissionState?.status) {
        showCameraPermissionDialog = false
    }

    var showCreatePollDialog by remember { mutableStateOf(false) }

    var showCommandsPickerDialog by remember { mutableStateOf(false) }

    val visualMediaType = config.visualMediaType.toContractVisualMediaType()

    AttachmentTypeSystemPicker(
        channel = channel,
        messageMode = messageMode,
        onPickerTypeClick = { attachmentPickerMode ->
            when (attachmentPickerMode) {
                is Images -> mediaPickerLauncher.launch(PickVisualMediaRequest(visualMediaType))

                is MediaCapture -> {
                    if (cameraPermissionState == null || cameraPermissionState.status.isGranted) {
                        captureMediaLauncher?.launch(Unit)
                    } else if (cameraPermissionState.status.shouldShowRationale) {
                        showCameraPermissionDialog = true
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                is Files -> filePickerLauncher.launch(filePickerIntent())

                is Poll -> {
                    showCreatePollDialog = true
                    onAttachmentPickerAction(AttachmentPickerCreatePollClick)
                }

                is Commands -> showCommandsPickerDialog = true

                is System -> {
                    // no-op
                }

                is CustomPickerMode -> {
                    // no-op
                }
            }
        },
    )

    if (showCameraPermissionDialog) {
        CameraPermissionDialog(
            onDismiss = { showCameraPermissionDialog = false },
        )
    }

    if (showCreatePollDialog) {
        FullscreenDialog(onDismissRequest = { showCreatePollDialog = false }) {
            CreatePollScreen(
                onAttachmentPickerAction = { action ->
                    showCreatePollDialog = false
                    onAttachmentPickerAction(action)
                },
            )
        }
    }

    if (showCommandsPickerDialog) {
        ModalBottomSheet(onDismissRequest = { showCommandsPickerDialog = false }) {
            AttachmentCommandPicker(
                commands = commands,
            )
        }
    }
}

@Composable
private fun rememberFilePickerLauncher(onResult: (Uri) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let(onResult)
        }
    }

@Composable
private fun rememberVisualMediaPickerLauncher(allowMultiple: Boolean, onResult: (List<Uri>) -> Unit) =
    if (allowMultiple) {
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            onResult(uris)
        }
    } else {
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                onResult(listOf(uri))
            }
        }
    }

private fun filePickerIntent(): Intent {
    val attachmentFilter = AttachmentFilter()
    return Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*" // General type to include multiple types
        putExtra(Intent.EXTRA_MIME_TYPES, attachmentFilter.getSupportedMimeTypes().toTypedArray())
        addCategory(Intent.CATEGORY_OPENABLE)
    }
}

private fun showErrorIfNeeded(context: Context, metadata: AttachmentsMetadataFromUris) {
    if (metadata.uris.size != metadata.attachmentsMetadata.size) {
        Toast.makeText(
            context,
            R.string.stream_compose_message_composer_file_not_supported,
            Toast.LENGTH_SHORT,
        ).show()
    }
}

@Composable
private fun CameraPermissionDialog(
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.height(ChatTheme.dimens.attachmentsPickerHeight),
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.barsBackground),
        ) {
            RequiredCameraPermission()
        }
    }
}
