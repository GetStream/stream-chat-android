package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerActions
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.docs.R
import io.getstream.chat.android.compose.R as ComposeR

@Composable
fun CustomComposerAndAttachmentsPicker(cid: String?, onBackClick: () -> Unit = {}) {
    cid?.let {
        val viewModelFactory = MessagesViewModelFactory(LocalContext.current, channelId = cid)
        val listViewModel = viewModel(
            modelClass = MessageListViewModel::class.java,
            factory = viewModelFactory,
        )
        val composerViewModel = viewModel(
            modelClass = MessageComposerViewModel::class.java,
            factory = viewModelFactory,
        )
        val attachmentsPickerViewModel = viewModel(
            modelClass = AttachmentsPickerViewModel::class.java,
            factory = viewModelFactory,
        )

        val isPickerVisible = attachmentsPickerViewModel.isPickerVisible
        val backAction = remember(composerViewModel, attachmentsPickerViewModel) {
            {
                when {
                    attachmentsPickerViewModel.isPickerVisible -> {
                        attachmentsPickerViewModel.setPickerVisible(visible = false)
                    }

                    else -> onBackClick()
                }
            }
        }

        BackHandler(enabled = true, onBack = backAction)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Scaffold(
                topBar = {
                    CustomMessageListHeader(cid = cid, onBackClick = onBackClick)
                },
                bottomBar = {
                    CustomMessageComposer(
                        composerViewModel = composerViewModel,
                        attachmentsPickerViewModel = attachmentsPickerViewModel,
                    )
                },
                content = {
                    MessageList(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ChatTheme.colors.appBackground)
                            .padding(it),
                        viewModel = listViewModel,
                    )
                },
            )

            if (isPickerVisible) {
                CustomAttachmentsPicker(
                    attachmentsPickerViewModel = attachmentsPickerViewModel,
                    actions = AttachmentPickerActions.defaultActions(
                        attachmentsPickerViewModel,
                        composerViewModel,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CustomMessageComposer(
    composerViewModel: MessageComposerViewModel,
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
) {
    MessageComposer(
        viewModel = composerViewModel,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        input = { composerState ->
            val onSendClick: (String, List<Attachment>) -> Unit = { text, attachments ->
                composerViewModel.sendMessage(
                    message = composerViewModel.buildNewMessage(
                        message = text,
                        attachments = attachments,
                    ),
                )
            }
            MessageInput(
                messageComposerState = composerState,
                onValueChange = { composerViewModel.setMessageInput(it) },
                onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                onCancelAction = { composerViewModel.dismissMessageActions() },
                onSendClick = onSendClick,
                recordingActions = AudioRecordingActions.defaultActions(
                    viewModel = composerViewModel,
                    sendOnComplete = ChatTheme.config.composer.audioRecordingSendOnComplete,
                ),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.CenterVertically),
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                attachmentsPickerViewModel.setPickerVisible(visible = true)
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = Color.DarkGray,
                                )
                            },
                        )
                        IconButton(
                            enabled = composerState.canSendMessage(),
                            onClick = { onSendClick(composerState.inputValue, composerState.attachments) },
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null,
                                    tint = Color.DarkGray,
                                )
                            },
                        )
                    }
                },
            )
        },
        onAttachmentsClick = { attachmentsPickerViewModel.setPickerVisible(visible = true) },
        leadingContent = {},
    )
}

/**
 * The default picker modes available in the attachment picker.
 */
private val DefaultPickerModes: List<AttachmentPickerMode> = listOf(
    GalleryPickerMode(),
    FilePickerMode(),
    CameraPickerMode(),
)

@Composable
private fun CustomAttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    actions: AttachmentPickerActions,
    pickerModes: List<AttachmentPickerMode> = DefaultPickerModes,
) {
    var shouldShowMenu by remember { mutableStateOf(true) }
    var selectedModeIndex by remember { mutableStateOf(-1) }

    Box(
        // Gray overlay
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                onClick = actions.onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Card(
            modifier = Modifier
                .heightIn(max = 350.dp)
                .align(Alignment.BottomCenter)
                .clickable(
                    indication = null,
                    onClick = {},
                    interactionSource = remember { MutableInteractionSource() },
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = ChatTheme.shapes.bottomSheet,
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.inputBackground),
        ) {
            Box(modifier = Modifier.padding(vertical = 24.dp)) {
                if (shouldShowMenu) {
                    // Show the menu with Images, Files, Camera options
                    AttachmentsTypeMenu(
                        channel = attachmentsPickerViewModel.channel,
                        pickerModes = pickerModes,
                        onClick = {
                            selectedModeIndex = it
                            shouldShowMenu = false
                        },
                    )
                } else {
                    // Show the selected picker content with back and submit buttons
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        AttachmentsPickerToolbar(
                            onBackClick = {
                                shouldShowMenu = true
                                selectedModeIndex = -1
                            },
                            isSubmitEnabled = attachmentsPickerViewModel.attachments.any { it.isSelected },
                            onSubmitClick = {
                                actions.onAttachmentsSelected(
                                    attachmentsPickerViewModel.getSelectedAttachments(),
                                )
                            },
                        )

                        pickerModes.getOrNull(selectedModeIndex)?.let { pickerMode ->
                            ChatTheme.componentFactory.AttachmentPickerContent(
                                pickerMode = pickerMode,
                                commands = attachmentsPickerViewModel.channel.config.commands,
                                attachments = attachmentsPickerViewModel.attachments,
                                onLoadAttachments = attachmentsPickerViewModel::loadAttachments,
                                onUrisSelected = attachmentsPickerViewModel::resolveAndSubmitUris,
                                actions = actions,
                                onAttachmentsSubmitted = { metaData ->
                                    actions.onAttachmentsSelected(
                                        attachmentsPickerViewModel.getAttachmentsFromMetadata(metaData),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun AttachmentsTypeMenu(
    channel: Channel,
    pickerModes: List<AttachmentPickerMode>,
    onClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pickerModes.forEachIndexed { index, pickerMode ->
            AttachmentsTypeMenuItem(
                pickerMode = pickerMode,
                index = index,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun AttachmentsTypeMenuItem(
    pickerMode: AttachmentPickerMode,
    index: Int,
    onClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onClick(index) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val backgroundColor: Color
        val label: String

        when (pickerMode) {
            is GalleryPickerMode -> {
                backgroundColor = Color(0xFFCCCCFF)
                label = "Images"
            }

            is FilePickerMode -> {
                backgroundColor = Color(0xFFFFCCCC)
                label = "Files"
            }

            is CameraPickerMode -> {
                backgroundColor = Color(0xFFFFCC99)
                label = "Camera"
            }

            else -> {
                backgroundColor = Color.LightGray
                label = "Other"
            }
        }

        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .background(backgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    when (pickerMode) {
                        is GalleryPickerMode -> ComposeR.drawable.stream_compose_ic_media_picker
                        is FilePickerMode -> ComposeR.drawable.stream_compose_ic_attachment_file_picker
                        is CameraPickerMode -> ComposeR.drawable.stream_compose_ic_attachment_camera_picker
                        else -> R.drawable.ic_menu
                    }
                ),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(text = label)
    }
}

@Composable
private fun AttachmentsPickerToolbar(
    onBackClick: () -> Unit,
    isSubmitEnabled: Boolean,
    onSubmitClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
            )
        }
        IconButton(
            enabled = isSubmitEnabled,
            onClick = onSubmitClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Submit Attachments",
                modifier = Modifier.size(24.dp),
                tint = if (isSubmitEnabled) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.textLowEmphasis
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomAttachmentPickerOptions() {
    ChatTheme {
        AttachmentsTypeMenu(
            channel = Channel(),
            pickerModes = DefaultPickerModes,
        ) {}
    }
}
