package io.getstream.chat.android.compose.ui.messages.attachments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.CaptureMediaContract
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import java.io.File

/**
 * Represents the bottom bar UI that allows users to pick attachments. It allows for different options
 * and enables/disables options based on currently selected items.
 *
 * @param attachmentsPickerViewModel - ViewModel that loads the images or files and persists which
 * items have been selected.
 * @param modifier - Modifier for styling.
 * @param onAttachmentsSelected - Handler when attachments are selected and confirmed by the user.
 * @param onDismiss - Handler when the user dismisses the UI.
 * */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun AttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storagePermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val mediaCaptureResultLauncher =
        rememberLauncherForActivityResult(contract = CaptureMediaContract()) { file: File? ->
            val attachments =
                if (file == null) {
                    emptyList()
                } else {
                    listOf(AttachmentMetaData(context, file))
                }

            onAttachmentsSelected(attachmentsPickerViewModel.getAttachmentsFromMetaData(attachments))
        }

    LaunchedEffect(Unit) {
        attachmentsPickerViewModel.start()
    }

    Box( // TODO we need a nicer way of allowing dismissing, where we don't intercept clicks on the parent and the child
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Card(
            modifier = modifier.clickable(
                indication = null,
                onClick = { },
                interactionSource = remember { MutableInteractionSource() }
            ),
            elevation = 4.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            backgroundColor = ChatTheme.colors.inputBackground,
        ) {
            Column {
                AttachmentPickerOptions(
                    hasPickedFiles = attachmentsPickerViewModel.hasPickedFiles,
                    hasPickedImages = attachmentsPickerViewModel.hasPickedImages,
                    onOptionClick = {
                        attachmentsPickerViewModel.onAttachmentsModeSelected(it)
                    },
                    onSendAttachmentsClick = {
                        onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = ChatTheme.colors.barsBackground,
                ) {
                    val pickerMode = attachmentsPickerViewModel.attachmentsPickerMode
                    val permissionState = when (pickerMode) {
                        Images, Files -> storagePermissionState
                        MediaCapture -> cameraPermissionState
                    }

                    val hasPermission = permissionState.hasPermission

                    val content = @Composable {
                        when (pickerMode) {
                            Files -> FilesPicker(
                                files = attachmentsPickerViewModel.files,
                                onItemSelected = attachmentsPickerViewModel::onAttachmentSelected,
                                onBrowseFilesResult = { uris ->
                                    onAttachmentsSelected(
                                        attachmentsPickerViewModel.getAttachmentsFromUris(
                                            uris
                                        )
                                    )
                                }
                            )
                            Images -> ImagesPicker(
                                modifier = Modifier.padding(
                                    top = 16.dp,
                                    start = 2.dp,
                                    end = 2.dp,
                                    bottom = 2.dp
                                ),
                                images = attachmentsPickerViewModel.images,
                                onImageSelected = attachmentsPickerViewModel::onAttachmentSelected
                            )
                            MediaCapture -> Box(modifier = Modifier.fillMaxSize()) {
                                LaunchedEffect(Unit) {
                                    mediaCaptureResultLauncher.launch(Unit)
                                }
                            }
                        }
                    }

                    PermissionRequired(
                        permissionState = permissionState,
                        permissionNotGrantedContent = { MissingPermissionContent(permissionState = permissionState) },
                        permissionNotAvailableContent = { MissingPermissionContent(permissionState = permissionState) },
                        content = content
                    )

                    LaunchedEffect(permissionState.hasPermission) {
                        if (permissionState.permissionRequested && permissionState.hasPermission) {
                            attachmentsPickerViewModel.start()
                        }
                    }

                    LaunchedEffect(pickerMode) {
                        if (!hasPermission && !permissionState.permissionRequested) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows the UI if we're missing permissions to fetch data for attachments.
 * The UI explains to the user which permission is missing and why we need it.
 *
 * @param permissionState - The missing permission.
 * */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MissingPermissionContent(permissionState: PermissionState) {
    val title = when (permissionState.permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> R.string.stream_ui_message_input_permission_storage_title
        else -> R.string.stream_ui_message_input_permission_camera_title
    }

    val message = when (permissionState.permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> R.string.stream_ui_message_input_permission_storage_message
        else -> R.string.stream_ui_message_input_permission_camera_message
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            style = ChatTheme.typography.title3Bold,
            text = stringResource(id = title),
            color = ChatTheme.colors.textHighEmphasis,
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = ChatTheme.typography.body,
            text = stringResource(id = message),
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.textLowEmphasis,
        )

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = {
                // TODO pull this out into a utility function
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = uri
                }
                context.startActivity(intent)
            },
            content = { Text(stringResource(id = R.string.grant_permission)) }
        )
    }
}

/**
 * The options for the Attachment picker. Shows three options:
 * - Images
 * - Files
 * - Media
 *
 * @param hasPickedImages - If the user picked any images. Used to disable other options.
 * @param hasPickedFiles - If the user picked any files. Used to disable other options.
 * @param onOptionClick - Handler for clicking on any of the options, to change the shown attachments.
 * @param onSendAttachmentsClick - Handler when confirming the picked attachments.
 * */
@Composable
private fun AttachmentPickerOptions(
    hasPickedImages: Boolean,
    hasPickedFiles: Boolean,
    onOptionClick: (AttachmentsPickerMode) -> Unit,
    onSendAttachmentsClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            Modifier.weight(4f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                enabled = !hasPickedFiles,
                content = {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(id = R.string.images_option),
                        tint = if (!hasPickedFiles) ChatTheme.colors.primaryAccent else ChatTheme.colors.disabled
                    )
                },
                onClick = { onOptionClick(Images) }
            )

            IconButton(
                enabled = !hasPickedImages,
                content = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = stringResource(id = R.string.files_option),
                        tint = if (!hasPickedImages) ChatTheme.colors.primaryAccent else ChatTheme.colors.disabled
                    )
                },
                onClick = { onOptionClick(Files) }
            )

            IconButton(
                enabled = !hasPickedFiles && !hasPickedImages,
                content = {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = stringResource(id = R.string.capture_option),
                        tint = if (!hasPickedFiles && !hasPickedImages) ChatTheme.colors.primaryAccent else ChatTheme.colors.disabled
                    )
                },
                onClick = { onOptionClick(MediaCapture) }
            )
        }

        Spacer(modifier = Modifier.weight(5f))

        IconButton(
            enabled = hasPickedFiles || hasPickedImages,
            onClick = onSendAttachmentsClick,
            content = {
                Icon(
                    modifier = Modifier.weight(1f),
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(id = R.string.send_attachment),
                    tint = if (hasPickedFiles || hasPickedImages) ChatTheme.colors.primaryAccent else ChatTheme.colors.disabled
                )
            }
        )
    }
}
