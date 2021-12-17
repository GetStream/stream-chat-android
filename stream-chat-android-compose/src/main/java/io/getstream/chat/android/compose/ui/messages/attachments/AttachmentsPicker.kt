package io.getstream.chat.android.compose.ui.messages.attachments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import io.getstream.chat.android.compose.ui.components.attachments.files.FilesPicker
import io.getstream.chat.android.compose.ui.components.attachments.images.ImagesPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import java.io.File

/**
 * Represents the bottom bar UI that allows users to pick attachments. It allows for different options
 * and enables/disables options based on currently selected items.
 *
 * @param attachmentsPickerViewModel ViewModel that loads the images or files and persists which
 * items have been selected.
 * @param modifier Modifier for styling.
 * @param onAttachmentsSelected Handler when attachments are selected and confirmed by the user.
 * @param onDismiss Handler when the user dismisses the UI.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun AttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
) {
    val context = LocalContext.current
    val storagePermissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    val requiresCameraPermission = isCameraPermissionDeclared(context)

    val cameraPermissionState =
        if (requiresCameraPermission) rememberPermissionState(permission = Manifest.permission.CAMERA) else null

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
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
            shape = shape,
            backgroundColor = ChatTheme.colors.inputBackground,
        ) {
            Column {
                AttachmentPickerOptions(
                    attachmentsPickerMode = attachmentsPickerViewModel.attachmentsPickerMode,
                    hasPickedFiles = attachmentsPickerViewModel.hasPickedFiles,
                    hasPickedImages = attachmentsPickerViewModel.hasPickedImages,
                    onOptionClick = {
                        attachmentsPickerViewModel.changeAttachmentPickerMode(it)
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

                    val hasPermission = permissionState?.hasPermission ?: true

                    val content = @Composable {
                        when (pickerMode) {
                            Files -> FilesPicker(
                                files = attachmentsPickerViewModel.files,
                                onItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
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
                                onImageSelected = attachmentsPickerViewModel::changeSelectedAttachments
                            )
                            MediaCapture -> Box(modifier = Modifier.fillMaxSize()) {
                                LaunchedEffect(Unit) {
                                    mediaCaptureResultLauncher.launch(Unit)
                                }
                            }
                        }
                    }

                    if (pickerMode == MediaCapture && permissionState == null) {
                        content()
                    } else if (permissionState != null) {
                        PermissionRequired(
                            permissionState = permissionState,
                            permissionNotGrantedContent = { MissingPermissionContent(permissionState) },
                            permissionNotAvailableContent = { MissingPermissionContent(permissionState) },
                            content = content
                        )
                    }

                    LaunchedEffect(storagePermissionState.hasPermission) {
                        if (storagePermissionState.hasPermission) {
                            attachmentsPickerViewModel.loadData()
                        }
                    }

                    LaunchedEffect(pickerMode) {
                        if (!hasPermission && !storagePermissionState.permissionRequested) {
                            storagePermissionState.launchPermissionRequest()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns if we need to check for the camera permission or not.
 *
 * @param context The context of the app.
 * @return If the camera permission is declared in the manifest or not.
 */
private fun isCameraPermissionDeclared(context: Context): Boolean {
    return context.packageManager
        .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions
        .contains(Manifest.permission.CAMERA)
}

/**
 * Shows the UI if we're missing permissions to fetch data for attachments.
 * The UI explains to the user which permission is missing and why we need it.
 */
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

        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
            onClick = {
                // TODO pull this out into a utility function
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = uri
                }
                context.startActivity(intent)
            }
        ) {
            Text(stringResource(id = R.string.stream_compose_grant_permission))
        }
    }
}

/**
 * The options for the Attachment picker. Shows three options:
 * - Images
 * - Files
 * - Media
 *
 * @param hasPickedImages If the user picked any images. Used to disable other options.
 * @param hasPickedFiles If the user picked any files. Used to disable other options.
 * @param onOptionClick Handler for clicking on any of the options, to change the shown attachments.
 * @param onSendAttachmentsClick Handler when confirming the picked attachments.
 */
@Composable
private fun AttachmentPickerOptions(
    attachmentsPickerMode: AttachmentsPickerMode,
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
                        painter = painterResource(id = R.drawable.stream_compose_ic_image_picker),
                        contentDescription = stringResource(id = R.string.stream_compose_images_option),
                        tint = when {
                            attachmentsPickerMode == Images -> ChatTheme.colors.primaryAccent
                            hasPickedFiles -> ChatTheme.colors.disabled
                            else -> ChatTheme.colors.textLowEmphasis
                        },
                    )
                },
                onClick = { onOptionClick(Images) }
            )

            IconButton(
                enabled = !hasPickedImages,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_file_picker),
                        contentDescription = stringResource(id = R.string.stream_compose_files_option),
                        tint = when {
                            attachmentsPickerMode == Files -> ChatTheme.colors.primaryAccent
                            hasPickedImages -> ChatTheme.colors.disabled
                            else -> ChatTheme.colors.textLowEmphasis
                        },
                    )
                },
                onClick = { onOptionClick(Files) }
            )

            IconButton(
                enabled = !hasPickedFiles && !hasPickedImages,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_media_picker),
                        contentDescription = stringResource(id = R.string.stream_compose_capture_option),
                        tint = if (!hasPickedFiles && !hasPickedImages) ChatTheme.colors.textLowEmphasis else ChatTheme.colors.disabled
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
                    painter = painterResource(id = R.drawable.stream_compose_ic_circle_left),
                    contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                    tint = if (hasPickedFiles || hasPickedImages) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
                )
            }
        )
    }
}
