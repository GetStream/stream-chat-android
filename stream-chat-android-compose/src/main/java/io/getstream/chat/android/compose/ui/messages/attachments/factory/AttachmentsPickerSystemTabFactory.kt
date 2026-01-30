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
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.System
import io.getstream.chat.android.compose.ui.messages.attachments.RequiredCameraPermission
import io.getstream.chat.android.compose.ui.messages.attachments.media.rememberCaptureMediaLauncher
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.permissions.SystemAttachmentsPickerConfig
import io.getstream.chat.android.ui.common.permissions.toContractVisualMediaType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared

/**
 * Holds the information required to add support for "files" tab in the attachment picker.
 *
 * @param config The configuration for the system attachment picker.
 */
public class AttachmentsPickerSystemTabFactory(
    public val config: SystemAttachmentsPickerConfig,
) : AttachmentsPickerTabFactory {

    /** Returns whether file attachments are allowed. */
    @Deprecated(message = "Use config.filesAllowed instead.", level = DeprecationLevel.WARNING)
    public val filesAllowed: Boolean = config.filesAllowed

    /** Returns whether visual media attachments are allowed. */
    @Deprecated(message = "Use config.visualMediaAllowed instead.", level = DeprecationLevel.WARNING)
    public val mediaAllowed: Boolean = config.visualMediaAllowed

    /** Returns whether capturing images is allowed. */
    @Deprecated(message = "Use config.captureImageAllowed instead.", level = DeprecationLevel.WARNING)
    public val captureImageAllowed: Boolean = config.captureImageAllowed

    /** Returns whether capturing videos is allowed. */
    @Deprecated(message = "Use config.captureVideoAllowed instead.", level = DeprecationLevel.WARNING)
    public val captureVideoAllowed: Boolean = config.captureVideoAllowed

    /** Returns whether poll attachments are allowed. */
    @Deprecated(message = "Use config.pollAllowed instead.", level = DeprecationLevel.WARNING)
    public val pollAllowed: Boolean = config.pollAllowed

    private val pollFactory by lazy { AttachmentsPickerPollTabFactory() }

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = System

    /**
     * Emits a file icon for this tab.
     *
     * @param isEnabled If the tab is enabled.
     * @param isSelected If the tab is selected.
     */
    @Composable
    override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_attachments),
            contentDescription = stringResource(id = R.string.stream_compose_attachments),
            tint = when {
                isSelected -> ChatTheme.colors.primaryAccent
                isEnabled -> ChatTheme.colors.textLowEmphasis
                else -> ChatTheme.colors.disabled
            },
        )
    }

    /**
     * Emits content that allows users to pick files in this tab.
     *
     * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
     * @param attachments The list of attachments to display.
     * @param onAttachmentsChanged Handler to set the loaded list of attachments to display.
     * @param onAttachmentItemSelected Handler when the item selection state changes.
     * @param onAttachmentsSubmitted Handler to submit the selected attachments to the message composer.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Suppress("LongMethod")
    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
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

        val imagePickerLauncher =
            rememberVisualMediaPickerLauncher(config.visualMediaAllowMultiple) { uris ->
                processingViewModel.getAttachmentsMetadataFromUrisAsync(uris) { metadata ->
                    showErrorIfNeeded(context, metadata)
                    onAttachmentsSubmitted(metadata.attachmentsMetadata)
                }
            }

        val captureLauncher = rememberCaptureMediaLauncher(
            photo = config.captureImageAllowed,
            video = config.captureVideoAllowed,
        ) { file ->
            onAttachmentsSubmitted(listOf(AttachmentMetaData(context, file)))
        }
        var cameraRationaleShown by remember { mutableStateOf(false) }
        val cameraPermissionRequired = context.isPermissionDeclared(Manifest.permission.CAMERA)
        val cameraPermissionState = if (cameraPermissionRequired) {
            rememberPermissionState(Manifest.permission.CAMERA) {
                if (it) captureLauncher?.launch(Unit)
            }
        } else {
            null
        }

        var pollShown by remember { mutableStateOf(false) }

        val buttonsConfig = ButtonsConfig(
            filesAllowed = config.filesAllowed,
            mediaAllowed = config.visualMediaAllowed,
            captureAllowed = captureLauncher != null,
            pollAllowed = config.pollAllowed,
        )
        val visualMediaType = config.visualMediaType.toContractVisualMediaType()
        val buttonActions = ButtonActions(
            onFilesClick = { filePickerLauncher.launch(filePickerIntent()) },
            onMediaClick = {
                imagePickerLauncher.launch(PickVisualMediaRequest(visualMediaType))
            },
            onCaptureClick = {
                // Permission grant is needed only if CAMERA is declared in the Manifest and is not yet granted
                if (!cameraPermissionRequired || cameraPermissionState?.status?.isGranted == true) {
                    captureLauncher?.launch(Unit)
                } else if (cameraPermissionState?.status?.shouldShowRationale == true) {
                    cameraRationaleShown = true
                } else {
                    cameraPermissionState?.launchPermissionRequest()
                }
            },
            onPollClick = { pollShown = true },
        )

        ButtonRow(config = buttonsConfig, actions = buttonActions)

        if (pollShown) {
            PollDialog(
                factory = pollFactory,
                attachments = attachments,
                actions = PollDialogActions(
                    onAttachmentPickerAction = onAttachmentPickerAction,
                    onAttachmentsChanged = onAttachmentsChanged,
                    onAttachmentItemSelected = onAttachmentItemSelected,
                    onAttachmentsSubmitted = onAttachmentsSubmitted,
                    onDismissPollDialog = { pollShown = false },
                ),
            )
        }

        if (cameraRationaleShown && cameraPermissionState != null) {
            CameraPermissionDialog(
                permissionState = cameraPermissionState,
                onDismiss = { cameraRationaleShown = false },
            )
        }

        LaunchedEffect(cameraPermissionState?.status) {
            cameraRationaleShown = false
        }
    }

    @Composable
    private fun rememberFilePickerLauncher(onResult: (Uri) -> Unit) =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let(onResult)
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
}

@Composable
private fun ButtonRow(
    config: ButtonsConfig,
    actions: ButtonActions,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (config.filesAllowed) {
            item {
                FilesButton(actions.onFilesClick)
            }
        }
        if (config.mediaAllowed) {
            item {
                MediaButton(actions.onMediaClick)
            }
        }
        if (config.captureAllowed) {
            item {
                CaptureButton(actions.onCaptureClick)
            }
        }
        if (config.pollAllowed) {
            item {
                PollButton(actions.onPollClick)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermissionDialog(
    permissionState: PermissionState,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(ChatTheme.colors.barsBackground, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            RequiredCameraPermission()
        }
    }
}

@Composable
private fun PollDialog(
    factory: AttachmentsPickerPollTabFactory,
    attachments: List<AttachmentPickerItemState>,
    actions: PollDialogActions,
) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = actions.onDismissPollDialog,
    ) {
        Box(
            modifier = Modifier
                .background(ChatTheme.colors.appBackground)
                .fillMaxWidth()
                .fillMaxHeight(), // Ensure the dialog fills the height
        ) {
            factory.PickerTabContent(
                onAttachmentPickerAction = actions.onAttachmentPickerAction,
                attachments = attachments,
                onAttachmentsChanged = actions.onAttachmentsChanged,
                onAttachmentItemSelected = actions.onAttachmentItemSelected,
                onAttachmentsSubmitted = actions.onAttachmentsSubmitted,
            )
        }
    }
}

@Composable
private fun FilesButton(onClick: () -> Unit) =
    RoundedIconButton(
        onClick = onClick,
        iconPainter = painterResource(id = R.drawable.stream_compose_ic_file_picker),
        contentDescription = stringResource(id = R.string.stream_compose_files_option),
        text = stringResource(id = R.string.stream_compose_files_option),
    )

@Composable
private fun MediaButton(onClick: () -> Unit) =
    RoundedIconButton(
        onClick = onClick,
        iconPainter = painterResource(id = R.drawable.stream_compose_ic_image_picker),
        contentDescription = stringResource(id = R.string.stream_compose_images_option),
        text = stringResource(id = R.string.stream_compose_images_option),
    )

@Composable
private fun CaptureButton(onClick: () -> Unit) =
    RoundedIconButton(
        onClick = onClick,
        iconPainter = painterResource(id = R.drawable.stream_compose_ic_media_picker),
        contentDescription = stringResource(id = R.string.stream_ui_message_composer_capture_media_take_photo),
        text = stringResource(id = R.string.stream_ui_message_composer_capture_media_take_photo),
    )

@Composable
private fun PollButton(onClick: () -> Unit) =
    RoundedIconButton(
        onClick = onClick,
        iconPainter = painterResource(id = R.drawable.stream_compose_ic_poll),
        contentDescription = stringResource(id = R.string.stream_compose_poll_option),
        text = stringResource(id = R.string.stream_compose_poll_option),
    )

@Composable
private fun RoundedIconButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    contentDescription: String,
    text: String,
    contentColor: Color = ChatTheme.attachmentPickerTheme.contentColor,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp),
    ) {
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.barsBackground),
            modifier = Modifier
                .clip(CircleShape)
                .size(72.dp)
                .padding(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .clickable(onClick = onClick),
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = ChatTheme.typography.footnote,
            color = contentColor,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

//  Data classes to combine parameters.

private data class ButtonsConfig(
    val filesAllowed: Boolean,
    val mediaAllowed: Boolean,
    val captureAllowed: Boolean,
    val pollAllowed: Boolean,
)

private data class ButtonActions(
    val onFilesClick: () -> Unit,
    val onMediaClick: () -> Unit,
    val onCaptureClick: () -> Unit,
    val onPollClick: () -> Unit,
)

private data class PollDialogActions(
    val onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
    val onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    val onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    val onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    val onDismissPollDialog: () -> Unit,
)
