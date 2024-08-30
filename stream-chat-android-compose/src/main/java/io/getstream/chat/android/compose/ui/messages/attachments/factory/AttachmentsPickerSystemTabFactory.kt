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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * Holds the information required to add support for "files" tab in the attachment picker.
 */
public class AttachmentsPickerSystemTabFactory(private val otherFactories: List<AttachmentsPickerTabFactory>) :
    AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    override val attachmentsPickerMode: AttachmentsPickerMode
        get() = Files

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
    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {

        val context = LocalContext.current
        val attachmentFilter = AttachmentFilter()
        val storageHelper: StorageHelperWrapper = remember {
            StorageHelperWrapper(context, StorageHelper(), attachmentFilter)
        }

        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the file URI
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val attachmentMetadata = storageHelper.getAttachmentsMetadataFromUris(listOf(uri))
                    onAttachmentsSubmitted(attachmentMetadata)
                }
            }
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            // Handle the image URI
            uri?.let {
                val attachmentMetadata = storageHelper.getAttachmentsMetadataFromUris(listOf(uri))
                onAttachmentsSubmitted(attachmentMetadata)
            }
        }

        InnerContent(
            onAttachmentItemSelected = onAttachmentItemSelected,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentsSubmitted = onAttachmentsSubmitted,
            attachments = attachments,
            onAttachmentPickerAction = onAttachmentPickerAction,
            otherFactories = otherFactories,
            onFilesClick = {
                // Start file picker
                val filePickerIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*" // General type to include multiple types
                    putExtra(Intent.EXTRA_MIME_TYPES, attachmentFilter.getSupportedMimeTypes().toTypedArray())
                    addCategory(Intent.CATEGORY_OPENABLE)
                }

                filePickerLauncher.launch(filePickerIntent)
            },
            onImagesClick = {
                // Start photo picker
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }
        )
    }
}

@Composable
private fun InnerContent(
    otherFactories: List<AttachmentsPickerTabFactory>,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    onFilesClick: () -> Unit,
    onImagesClick: () -> Unit,
) {
    val pollsFactory = remember {
        otherFactories.firstOrNull { it.attachmentsPickerMode == Poll }
    }
    val mediaCaptureTabFactory = remember {
        otherFactories.firstOrNull { it.attachmentsPickerMode == MediaCapture }
    }

    var pollSelected by remember {
        mutableStateOf(false)
    }
    var mediaSelected by remember {
        mutableStateOf(false)
    }

    if (mediaSelected) {
        mediaCaptureTabFactory?.PickerTabContent(
            onAttachmentPickerAction = onAttachmentPickerAction,
            attachments = attachments,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentItemSelected = onAttachmentItemSelected,
            onAttachmentsSubmitted = onAttachmentsSubmitted
        )
    }

    if (pollSelected) {
        Dialog(properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), onDismissRequest = { pollSelected = false }) {
            Box(
                modifier = Modifier
                    .background(ChatTheme.colors.appBackground)
                    .fillMaxWidth()
                    .fillMaxHeight() // Ensure the dialog fills the height
            ) {
                pollsFactory?.PickerTabContent(
                    onAttachmentPickerAction = onAttachmentPickerAction,
                    attachments = attachments,
                    onAttachmentsChanged = onAttachmentsChanged,
                    onAttachmentItemSelected = onAttachmentItemSelected,
                    onAttachmentsSubmitted = onAttachmentsSubmitted
                )
            }
        }
    }

    val buttons = listOf<@Composable () -> Unit>(
        {
            RoundedIconButton(
                onClick = onFilesClick,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_file_picker),
                contentDescription = stringResource(id = R.string.stream_compose_files_option),
                text = stringResource(id = R.string.stream_compose_files_option)
            )
        },
        {
            RoundedIconButton(
                onClick = onImagesClick,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_image_picker),
                contentDescription = stringResource(id = R.string.stream_compose_images_option),
                text = stringResource(id = R.string.stream_compose_images_option)
            )
        }
    ) + listOf<(@Composable () -> Unit)>(
        {
            RoundedIconButton(
                onClick = { mediaSelected = !mediaSelected },
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_media_picker),
                contentDescription = stringResource(id = R.string.stream_ui_message_composer_capture_media_take_photo),
                text = stringResource(id = R.string.stream_ui_message_composer_capture_media_take_photo)
            )
        }, {
            RoundedIconButton(
                onClick = { pollSelected = !pollSelected },
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_poll),
                contentDescription = stringResource(id = R.string.stream_compose_poll_option),
                text = stringResource(id = R.string.stream_compose_poll_option)
            )
        }
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { button ->
            button()
        }
    }
}

@Composable
private fun RoundedIconButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    contentDescription: String,
    text: String,
    iconTint: Color = ChatTheme.colors.overlayDark,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Card(
            shape = CircleShape,
            elevation = 4.dp,
            backgroundColor = ChatTheme.colors.barsBackground,
            modifier = Modifier
                .clip(CircleShape)
                .size(72.dp)
                .padding(12.dp)
                .clickable(onClick = onClick)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}