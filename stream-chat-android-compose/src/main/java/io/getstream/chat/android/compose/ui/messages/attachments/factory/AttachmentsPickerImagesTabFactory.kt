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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.ui.components.attachments.images.ImagesPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.ui.common.permissions.Permissions
import io.getstream.chat.android.ui.common.permissions.VisualMediaAccess
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.uiutils.util.openSystemSettings
import kotlinx.coroutines.flow.collectLatest

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
            modifier = Modifier.testTag("Stream_AttachmentPickerImagesTab"),
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
    @Composable
    override fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        val permissions = Permissions.visualMediaPermissions()
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val processingViewModel = viewModel<AttachmentsProcessingViewModel>(
            factory = AttachmentsProcessingViewModelFactory(StorageHelperWrapper(context)),
        )
        LaunchedEffect(processingViewModel) {
            processingViewModel.mediaMetadata.collectLatest { metaData ->
                val items = metaData.map { AttachmentPickerItemState(it, false) }
                onAttachmentsChanged(items)
            }
        }
        val mediaAccess by visualMediaAccessAsState(context, lifecycleOwner) { value ->
            if (value != VisualMediaAccess.DENIED) {
                processingViewModel.getMediaAsync()
            }
        }

        var showPermanentlyDeniedSnackBar by remember { mutableStateOf(false) }

        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (Permissions.isPermanentlyDenied(context, result)) {
                    showPermanentlyDeniedSnackBar = true
                }
            }

        // Content
        VisualMediaAccessContent(
            visualMediaAccess = mediaAccess,
            attachments = attachments,
            onAttachmentItemSelected = onAttachmentItemSelected,
            onRequestAccessClick = {
                permissionLauncher.launch(permissions)
            },
        )

        // Access permanently denied snackbar
        val snackBarHostState = remember { SnackbarHostState() }
        PermissionPermanentlyDeniedSnackBar(
            hostState = snackBarHostState,
            onActionClick = { context.openSystemSettings() },
        )
        val snackbarMessage = stringResource(id = R.string.stream_ui_message_composer_permission_setting_message)
        val snackbarAction = stringResource(id = R.string.stream_ui_message_composer_permissions_setting_button)
        LaunchedEffect(showPermanentlyDeniedSnackBar) {
            if (showPermanentlyDeniedSnackBar) {
                snackBarHostState.showSnackbar(snackbarMessage, snackbarAction, duration = SnackbarDuration.Short)
                showPermanentlyDeniedSnackBar = false
            }
        }
    }

    /**
     * Renders the visual media content based on the [VisualMediaAccess] state.
     *
     * @param visualMediaAccess The current state of the visual media access.
     * @param attachments The list of attachments to display.
     * @param onAttachmentItemSelected Action invoked when the user selects an attachment.
     * @param onRequestAccessClick Action invoked when the user taps on the "Give permission" button.
     */
    @Composable
    private fun VisualMediaAccessContent(
        visualMediaAccess: VisualMediaAccess,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onRequestAccessClick: () -> Unit,
    ) {
        when (visualMediaAccess) {
            VisualMediaAccess.FULL -> {
                ImagesPicker(
                    modifier = Modifier.padding(top = 16.dp, start = 2.dp, end = 2.dp, bottom = 2.dp),
                    images = attachments,
                    onImageSelected = onAttachmentItemSelected,
                    showAddMore = false,
                )
            }

            VisualMediaAccess.PARTIAL -> {
                ImagesPicker(
                    modifier = Modifier.padding(top = 16.dp, start = 2.dp, end = 2.dp, bottom = 2.dp),
                    images = attachments,
                    onImageSelected = onAttachmentItemSelected,
                    showAddMore = true,
                    onAddMoreClick = onRequestAccessClick,
                )
            }

            VisualMediaAccess.DENIED -> {
                NoStorageAccessContent(onRequestAccessClick = onRequestAccessClick)
            }
        }
    }
}
