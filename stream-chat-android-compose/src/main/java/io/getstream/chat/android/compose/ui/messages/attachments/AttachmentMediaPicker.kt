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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState.Selection
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.ui.components.attachments.images.ImagesPicker
import io.getstream.chat.android.compose.ui.messages.attachments.permission.PermanentlyDeniedPermissionSnackBar
import io.getstream.chat.android.compose.ui.messages.attachments.permission.RequiredStoragePermission
import io.getstream.chat.android.compose.ui.messages.attachments.permission.visualMediaAccessAsState
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentProcessingViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentProcessingViewModelFactory
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.permissions.Permissions
import io.getstream.chat.android.ui.common.permissions.VisualMediaAccess
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.openSystemSettings

@Composable
internal fun AttachmentMediaPicker(
    pickerMode: GalleryPickerMode,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit = {},
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val processingViewModelFactory = remember(context) {
        AttachmentProcessingViewModelFactory(StorageHelperWrapper(context.applicationContext))
    }
    val processingViewModel = viewModel<AttachmentProcessingViewModel>(factory = processingViewModelFactory)
    val permissions = Permissions.visualMediaPermissions()
    val mediaAccess by visualMediaAccessAsState(context, lifecycleOwner) { value ->
        if (value != VisualMediaAccess.DENIED) {
            processingViewModel.getMediaAsync { metadata ->
                val items = metadata.map(::AttachmentPickerItemState)
                onAttachmentsChanged(items)
            }
        }
    }
    var showPermanentlyDeniedSnackBar by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (Permissions.isPermanentlyDenied(context, result)) {
                showPermanentlyDeniedSnackBar = true
            }
        }

    val snackBarHostState = remember { SnackbarHostState() }
    Box(contentAlignment = Alignment.Center) {
        // Content
        VisualMediaAccessContent(
            visualMediaAccess = mediaAccess,
            attachments = attachments,
            onAttachmentItemSelected = onAttachmentItemSelected,
            onGrantPermissionClick = { permissionLauncher.launch(permissions) },
            allowMultipleSelection = pickerMode.allowMultipleSelection,
        )
        // Access permanently denied snackbar
        PermanentlyDeniedPermissionSnackBar(
            hostState = snackBarHostState,
            onActionClick = context::openSystemSettings,
        )
    }

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
private fun VisualMediaAccessContent(
    visualMediaAccess: VisualMediaAccess,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    onGrantPermissionClick: () -> Unit,
    allowMultipleSelection: Boolean,
) {
    when (visualMediaAccess) {
        VisualMediaAccess.FULL -> {
            ImagesPicker(
                modifier = Modifier.padding(2.dp),
                images = attachments,
                onImageSelected = onAttachmentItemSelected,
                allowMultipleSelection = allowMultipleSelection,
                showAddMore = false,
            )
        }

        VisualMediaAccess.PARTIAL -> {
            ImagesPicker(
                modifier = Modifier.padding(2.dp),
                images = attachments,
                onImageSelected = onAttachmentItemSelected,
                allowMultipleSelection = allowMultipleSelection,
                showAddMore = true,
                onAddMoreClick = onGrantPermissionClick,
            )
        }

        VisualMediaAccess.DENIED -> {
            RequiredStoragePermission(
                onGrantPermissionClick = onGrantPermissionClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttachmentMediaPickerSingleSelectionPreview() {
    ChatPreviewTheme {
        AttachmentMediaPickerSingleSelection()
    }
}

@Suppress("MagicNumber")
@Composable
internal fun AttachmentMediaPickerSingleSelection() {
    AttachmentMediaPicker(
        pickerMode = GalleryPickerMode(allowMultipleSelection = false),
        attachments = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
                selection = Selection.Selected(position = 1),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(type = AttachmentType.VIDEO).apply {
                    videoLength = 1_000
                },
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun AttachmentMediaPickerMultipleSelectionPreview() {
    ChatPreviewTheme {
        AttachmentMediaPickerMultipleSelection()
    }
}

@Suppress("MagicNumber")
@Composable
internal fun AttachmentMediaPickerMultipleSelection() {
    AttachmentMediaPicker(
        pickerMode = GalleryPickerMode(),
        attachments = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
                selection = Selection.Selected(position = 1),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
                selection = Selection.Selected(position = 2),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(type = AttachmentType.VIDEO).apply {
                    videoLength = 1_000
                },
            ),
        ),
    )
}
