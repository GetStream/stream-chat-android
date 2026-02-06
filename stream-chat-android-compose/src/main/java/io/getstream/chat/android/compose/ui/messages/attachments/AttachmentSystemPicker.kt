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
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.MediaType
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.components.FullscreenDialog
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerCreatePollClick
import io.getstream.chat.android.compose.ui.messages.attachments.permission.RequiredCameraPermission
import io.getstream.chat.android.compose.ui.messages.attachments.poll.CreatePollScreen
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentProcessingViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentProcessingViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsMetadataFromUris
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.previewdata.PreviewCommandData
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared
import java.io.File

@Suppress("LongMethod")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun AttachmentSystemPicker(
    channel: Channel,
    messageMode: MessageMode,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit = {},
) {
    val context = LocalContext.current
    val pickerModes = ChatTheme.attachmentPickerConfig.modes

    val processingViewModelFactory = remember(context) {
        AttachmentProcessingViewModelFactory(StorageHelperWrapper(context.applicationContext))
    }
    val processingViewModel = viewModel<AttachmentProcessingViewModel>(
        factory = processingViewModelFactory,
    )

    val filePickerMode = remember(pickerModes) {
        pickerModes.filterIsInstance<FilePickerMode>().firstOrNull()
    }
    val filePickerLauncher = rememberFilePickerLauncher(filePickerMode) { uris ->
        if (uris.isNotEmpty()) {
            processingViewModel.getAttachmentsMetadataFromUrisAsync(uris) { metadata ->
                showErrorIfNeeded(context, metadata)
                onAttachmentsSubmitted(metadata.attachmentsMetadata)
            }
        }
    }

    val galleryPickerMode = remember(pickerModes) {
        pickerModes.filterIsInstance<GalleryPickerMode>().firstOrNull()
    }
    val mediaPickerLauncher = rememberVisualMediaPickerLauncher(galleryPickerMode) { uris ->
        if (uris.isNotEmpty()) {
            processingViewModel.getAttachmentsMetadataFromUrisAsync(uris) { metadata ->
                showErrorIfNeeded(context, metadata)
                onAttachmentsSubmitted(metadata.attachmentsMetadata)
            }
        }
    }

    val captureMediaMode = remember(pickerModes) {
        pickerModes.filterIsInstance<CameraPickerMode>()
            .map(CameraPickerMode::toCaptureMediaMode)
            .firstOrNull()
    }
    val captureMediaLauncher = rememberCaptureMediaLauncher(captureMediaMode) { file ->
        val attachments = listOf(AttachmentMetaData(context, file))
        onAttachmentsSubmitted(attachments)
    }
    // Handling camera permission flow is only required if the host application has declared the permission.
    val requiresCameraPermission = remember { context.isPermissionDeclared(Manifest.permission.CAMERA) }
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

    val fileTypes = remember { AttachmentFilter().getSupportedMimeTypes().toTypedArray() }

    ChatTheme.componentFactory.AttachmentTypeSystemPicker(
        channel = channel,
        messageMode = messageMode,
        onModeSelected = { attachmentPickerMode ->
            when (attachmentPickerMode) {
                is GalleryPickerMode -> {
                    val mediaType = attachmentPickerMode.mediaType.toVisualMediaType()
                    mediaPickerLauncher?.launch(PickVisualMediaRequest(mediaType))
                }

                is CameraPickerMode -> {
                    if (cameraPermissionState == null || cameraPermissionState.status.isGranted) {
                        captureMediaLauncher?.launch(Unit)
                    } else if (cameraPermissionState.status.shouldShowRationale) {
                        showCameraPermissionDialog = true
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                is FilePickerMode -> filePickerLauncher?.launch(fileTypes)

                is PollPickerMode -> {
                    showCreatePollDialog = true
                    onAttachmentPickerAction(AttachmentPickerCreatePollClick)
                }

                is CommandPickerMode -> showCommandsPickerDialog = true

                // Custom modes are handled by customers
                else -> Unit
            }
        },
        trailingContent = {},
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

    val commandPickerMode = remember(pickerModes) {
        pickerModes.filterIsInstance<CommandPickerMode>().firstOrNull()
    }
    val commands = channel.config.commands
    if (showCommandsPickerDialog && commandPickerMode != null) {
        ModalBottomSheet(onDismissRequest = { showCommandsPickerDialog = false }) {
            ChatTheme.componentFactory.AttachmentCommandPicker(
                pickerMode = commandPickerMode,
                commands = commands,
                onAttachmentPickerAction = { action ->
                    showCommandsPickerDialog = false
                    onAttachmentPickerAction(action)
                },
            )
        }
    }
}

private fun MediaType.toVisualMediaType(): PickVisualMedia.VisualMediaType =
    when (this) {
        MediaType.ImagesOnly -> PickVisualMedia.ImageOnly
        MediaType.VideosOnly -> PickVisualMedia.VideoOnly
        MediaType.ImagesAndVideos -> PickVisualMedia.ImageAndVideo
    }

@Composable
private fun rememberFilePickerLauncher(
    filePickerMode: FilePickerMode?,
    onResult: (List<Uri>) -> Unit,
) = filePickerMode?.let {
    if (filePickerMode.allowMultipleSelection) {
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            onResult(uris)
        }
    } else {
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                onResult(listOf(uri))
            }
        }
    }
}

@Composable
private fun rememberVisualMediaPickerLauncher(
    galleryPickerMode: GalleryPickerMode?,
    onResult: (List<Uri>) -> Unit,
) = galleryPickerMode?.let {
    if (galleryPickerMode.allowMultipleSelection) {
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            onResult(uris)
        }
    } else {
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                onResult(listOf(uri))
            }
        }
    }
}

@Composable
private fun rememberCaptureMediaLauncher(
    mode: CaptureMediaContract.Mode?,
    onResult: (File) -> Unit,
) = mode?.let {
    rememberLauncherForActivityResult(CaptureMediaContract(mode)) { file ->
        file?.let(onResult)
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

@Preview(showBackground = true)
@Composable
private fun AttachmentSystemPickerPreview() {
    ChatPreviewTheme {
        AttachmentSystemPicker()
    }
}

@Composable
internal fun AttachmentSystemPicker() {
    AttachmentSystemPicker(
        channel = Channel(),
        messageMode = MessageMode.Normal,
        attachments = emptyList(),
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentSystemPickerWithPollsPreview() {
    ChatPreviewTheme {
        AttachmentSystemPickerWithPolls()
    }
}

@Composable
internal fun AttachmentSystemPickerWithPolls() {
    AttachmentSystemPicker(
        channel = Channel(
            ownCapabilities = setOf(ChannelCapabilities.SEND_POLL),
            config = Config(pollsEnabled = true),
        ),
        messageMode = MessageMode.Normal,
        attachments = emptyList(),
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentSystemPickerWithCommandsPreview() {
    ChatPreviewTheme {
        AttachmentSystemPickerWithCommands()
    }
}

@Composable
internal fun AttachmentSystemPickerWithCommands() {
    AttachmentSystemPicker(
        channel = Channel(config = Config(commands = listOf(PreviewCommandData.command1))),
        messageMode = MessageMode.Normal,
        attachments = emptyList(),
    )
}
