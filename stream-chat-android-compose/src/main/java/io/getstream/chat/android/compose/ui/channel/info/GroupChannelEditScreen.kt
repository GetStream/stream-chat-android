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

package io.getstream.chat.android.compose.ui.channel.info

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.messages.attachments.media.rememberCaptureMediaLauncher
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.bottomBorder
import io.getstream.chat.android.compose.viewmodel.channel.GroupChannelEditViewEvent
import io.getstream.chat.android.compose.viewmodel.channel.GroupChannelEditViewModel
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupChannelEditScreen(
    viewModel: GroupChannelEditViewModel,
    channel: Channel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var channelName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = channel.name, selection = TextRange(channel.name.length)))
    }
    var pendingImagePath by rememberSaveable { mutableStateOf<String?>(null) }
    val pendingImageFile = pendingImagePath?.let(::File)
    var removeImage by rememberSaveable { mutableStateOf(false) }
    var showImagePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                GroupChannelEditViewEvent.SaveSuccess -> onDismiss()
                GroupChannelEditViewEvent.SaveError -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.stream_ui_channel_info_edit_save_error),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    val displayChannel = remember(channel, pendingImageFile, removeImage) {
        when {
            pendingImageFile != null -> channel.copy(image = Uri.fromFile(pendingImageFile).toString())
            removeImage -> channel.copy(image = "")
            else -> channel
        }
    }

    GroupChannelEditContent(
        channel = displayChannel,
        channelName = channelName,
        isSaving = state.isSaving,
        onChannelNameChange = { channelName = it },
        onNavigationIconClick = onDismiss,
        onSaveActionClick = { viewModel.save(channelName.text, pendingImageFile, removeImage) },
        onUploadPictureClick = { showImagePicker = true },
    )

    ImagePickerSheet(
        visible = showImagePicker,
        showRemoveOption = (channel.image.isNotBlank() || pendingImageFile != null) && !removeImage,
        onDismiss = { showImagePicker = false },
        onImageSelected = { file ->
            pendingImagePath = file.absolutePath
            removeImage = false
        },
        onImageRemoved = {
            pendingImagePath = null
            removeImage = true
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePickerSheet(
    visible: Boolean,
    showRemoveOption: Boolean,
    onDismiss: () -> Unit = {},
    onImageSelected: (File) -> Unit = {},
    onImageRemoved: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(DispatcherProvider.IO) {
            uri.toCacheFile(context)?.let { file ->
                withContext(DispatcherProvider.Main) { onImageSelected(file) }
            }
        }
    }

    val capturePhotoLauncher = rememberCaptureMediaLauncher(
        mode = CaptureMediaContract.Mode.PHOTO,
        onResult = onImageSelected,
    )

    val previewMode = LocalInspectionMode.current
    val sheetState = rememberModalBottomSheetState()
    LaunchedEffect(previewMode) {
        if (previewMode) sheetState.show()
    }

    if (visible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ) {
            ImagePickerOptions(
                showRemoveOption = showRemoveOption,
                onChooseFromLibraryClick = {
                    onDismiss()
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onTakePhotoClick = {
                    onDismiss()
                    capturePhotoLauncher.launch(Unit)
                },
                onRemovePictureClick = {
                    onDismiss()
                    onImageRemoved()
                },
            )
        }
    }
}

@Composable
private fun GroupChannelEditContent(
    channel: Channel,
    channelName: TextFieldValue,
    isSaving: Boolean,
    onChannelNameChange: (TextFieldValue) -> Unit = {},
    onNavigationIconClick: () -> Unit = {},
    onSaveActionClick: () -> Unit = {},
    onUploadPictureClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            GroupChannelEditTopBar(
                isSaving = isSaving,
                onNavigationIconClick = onNavigationIconClick,
                onSaveActionClick = onSaveActionClick,
            )
        },
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacing2xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChatTheme.componentFactory.ChannelAvatar(
                params = ChannelAvatarParams(
                    modifier = Modifier.size(AvatarSize.ExtraExtraLarge),
                    channel = channel,
                ),
            )
            Spacer(modifier = Modifier.size(StreamTokens.spacingXs))
            StreamTextButton(
                onClick = onUploadPictureClick,
                text = stringResource(R.string.stream_ui_channel_info_edit_upload_picture),
                style = StreamButtonStyleDefaults.primaryGhost,
                enabled = !isSaving,
            )
            Spacer(modifier = Modifier.size(StreamTokens.spacing2xl))
            ChannelNameField(
                value = channelName,
                enabled = !isSaving,
                onValueChange = onChannelNameChange,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GroupChannelEditTopBar(
    isSaving: Boolean,
    onNavigationIconClick: () -> Unit,
    onSaveActionClick: () -> Unit,
) {
    val colors = ChatTheme.colors
    CenterAlignedTopAppBar(
        modifier = Modifier.bottomBorder(color = colors.borderCoreSubtle),
        title = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_edit_title),
                style = ChatTheme.typography.headingMedium,
                maxLines = 1,
            )
        },
        navigationIcon = { ChannelInfoNavigationIcon(onClick = onNavigationIconClick) },
        actions = {
            if (isSaving) {
                LoadingIndicator(
                    modifier = Modifier
                        .padding(end = StreamTokens.spacingSm)
                        .size(24.dp),
                )
            } else {
                StreamButton(
                    style = StreamButtonStyleDefaults.primarySolid,
                    onClick = onSaveActionClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                        contentDescription = stringResource(id = R.string.stream_ui_channel_info_edit_save_action),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colors.backgroundCoreApp,
            scrolledContainerColor = colors.backgroundCoreApp,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textPrimary,
        ),
    )
}

@Composable
private fun ImagePickerOptions(
    showRemoveOption: Boolean,
    onChooseFromLibraryClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onRemovePictureClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = StreamTokens.spacingMd),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.stream_ui_channel_info_edit_group_picture),
            style = ChatTheme.typography.headingSmall,
            color = ChatTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.size(StreamTokens.spacingSm))
        PickerOptionButton(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.stream_compose_ic_attachment_camera_picker,
            textRes = R.string.stream_ui_channel_info_edit_take_photo,
            onClick = onTakePhotoClick,
        )
        PickerOptionButton(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.stream_compose_ic_media,
            textRes = R.string.stream_ui_channel_info_edit_choose_from_library,
            onClick = onChooseFromLibraryClick,
        )
        if (showRemoveOption) {
            PickerOptionButton(
                modifier = Modifier.fillMaxWidth(),
                destructive = true,
                iconRes = R.drawable.stream_ic_action_delete,
                textRes = R.string.stream_ui_channel_info_edit_remove_picture,
                onClick = onRemovePictureClick,
            )
        }
    }
}

@Composable
private fun PickerOptionButton(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (destructive) ChatTheme.colors.buttonDestructiveText else ChatTheme.colors.textSecondary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(textRes),
            style = ChatTheme.typography.bodyDefault,
            color = if (destructive) ChatTheme.colors.buttonDestructiveText else ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChannelNameField(
    value: TextFieldValue,
    enabled: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val colors = ChatTheme.colors
    OutlinedTextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        textStyle = ChatTheme.typography.bodyDefault,
        placeholder = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_edit_name_field_placeholder),
                style = ChatTheme.typography.bodyDefault,
            )
        },
        value = value,
        singleLine = true,
        enabled = enabled,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(StreamTokens.radiusLg),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = colors.systemCaret,
            focusedBorderColor = colors.borderUtilityActive,
            focusedTextColor = colors.inputTextDefault,
            unfocusedBorderColor = colors.borderCoreDefault,
            unfocusedTextColor = colors.inputTextDefault,
            disabledTextColor = colors.textDisabled,
            disabledBorderColor = colors.borderUtilityDisabled,
        ),
    )
}

@Preview
@Composable
private fun GroupChannelEditPlaceholderPreview() {
    ChatTheme {
        GroupChannelEditPlaceholder()
    }
}

@Composable
internal fun GroupChannelEditPlaceholder() {
    GroupChannelEditContent(
        channel = PreviewChannelData.channelWithImage,
        channelName = TextFieldValue(text = ""),
        isSaving = false,
    )
}

@Preview
@Composable
private fun GroupChannelEditFilledPreview() {
    ChatTheme {
        GroupChannelEditFilled()
    }
}

@Composable
internal fun GroupChannelEditFilled() {
    GroupChannelEditContent(
        channel = PreviewChannelData.channelWithImage.copy(name = "Channel Name"),
        channelName = TextFieldValue(text = "Channel Name"),
        isSaving = false,
    )
}

@Preview
@Composable
private fun GroupChannelEditSavingPreview() {
    ChatTheme {
        GroupChannelEditSaving()
    }
}

@Composable
internal fun GroupChannelEditSaving() {
    GroupChannelEditContent(
        channel = PreviewChannelData.channelWithImage.copy(name = "Channel Name"),
        channelName = TextFieldValue(text = "Channel Name"),
        isSaving = true,
    )
}

@Preview
@Composable
private fun ImagePickerOptionsPreview() {
    ChatTheme {
        ImagePickerOptionsWithRemove()
    }
}

@Composable
internal fun ImagePickerOptionsWithRemove() {
    ImagePickerSheet(
        visible = true,
        showRemoveOption = true,
    )
}

@Preview
@Composable
private fun ImagePickerOptionsNoRemovePreview() {
    ChatTheme {
        ImagePickerOptionsNoRemove()
    }
}

@Composable
internal fun ImagePickerOptionsNoRemove() {
    ImagePickerSheet(
        visible = true,
        showRemoveOption = false,
    )
}
