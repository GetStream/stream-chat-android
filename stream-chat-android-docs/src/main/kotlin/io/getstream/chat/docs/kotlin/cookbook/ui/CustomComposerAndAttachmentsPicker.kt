package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Send
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
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerBack
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
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

@Composable
fun CustomComposerAndAttachmentsPicker(cid: String?, onBackClick: () -> Unit = {}) {
    cid?.let {
        val viewModelFactory = MessagesViewModelFactory(LocalContext.current, channelId = cid)
        val listViewModel = viewModel(
            modelClass = MessageListViewModel::class.java,
            factory = viewModelFactory
        )
        val composerViewModel = viewModel(
            modelClass = MessageComposerViewModel::class.java,
            factory = viewModelFactory
        )
        val attachmentsPickerViewModel = viewModel(
            modelClass = AttachmentsPickerViewModel::class.java,
            factory = viewModelFactory
        )

        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments
        val backAction = remember(composerViewModel, attachmentsPickerViewModel) {
            {
                when {
                    attachmentsPickerViewModel.isShowingAttachments -> {
                        attachmentsPickerViewModel.changeAttachmentState(false)
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
                        attachmentsPickerViewModel = attachmentsPickerViewModel
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
                }
            )

            if (isShowingAttachments) {
                CustomAttachmentsPicker(
                    attachmentsPickerViewModel = attachmentsPickerViewModel,
                    onAttachmentsSelected = { attachments ->
                        attachmentsPickerViewModel.changeAttachmentState(false)
                        composerViewModel.addSelectedAttachments(attachments)
                    },
                    onDismiss = {
                        attachmentsPickerViewModel.changeAttachmentState(false)
                        attachmentsPickerViewModel.dismissAttachments()
                    }
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
                        attachments = attachments
                    )
                )
            }
            MessageInput(
                messageComposerState = composerState,
                onValueChange = { composerViewModel.setMessageInput(it) },
                onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                onCancelAction = { composerViewModel.dismissMessageActions() },
                onSendClick = onSendClick,
                recordingActions = AudioRecordingActions.defaultActions(composerViewModel),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.CenterVertically),
                label = {
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = "Type a message",
                        color = ChatTheme.colors.textLowEmphasis
                    )
                },
                trailingContent = {
                    Row {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = {
                                attachmentsPickerViewModel.changeAttachmentState(showAttachments = true)
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.AddCircle,
                                    contentDescription = null,
                                    tint = Color.DarkGray
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            enabled = composerState.canSendMessage(),
                            onClick = { onSendClick(composerState.inputValue, composerState.attachments) },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Send,
                                    contentDescription = null,
                                    tint = Color.DarkGray
                                )
                            }
                        )
                    }
                }
            )
        },
        onAttachmentsClick = { attachmentsPickerViewModel.changeAttachmentState(showAttachments = true) },
        leadingContent = {}
    )
}

@Composable
private fun CustomAttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onDismiss: () -> Unit,
    tabFactories: List<AttachmentsPickerTabFactory> = ChatTheme.attachmentsPickerTabFactories,
) {
    var shouldShowMenu by remember { mutableStateOf(true) }
    var selectedOptionIndex by remember { mutableStateOf(-1) }

    Box(
        // Gray overlay
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                onClick = onDismiss,
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
                        attachmentsPickerViewModel.channel,
                        tabFactories = tabFactories,
                        onClick = {
                            selectedOptionIndex = it
                            shouldShowMenu = false
                        },
                    )
                } else {
                    // Show the selected tabFactory, with back and submit buttons
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        AttachmentsPickerToolbar(
                            onBackClick = {
                                shouldShowMenu = true
                                selectedOptionIndex = -1
                            },
                            isSubmitEnabled = attachmentsPickerViewModel.hasPickedAttachments,
                            onSubmitClick = {
                                onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
                            },
                        )

                        tabFactories.getOrNull(selectedOptionIndex)
                            ?.PickerTabContent(
                                onAttachmentPickerAction = { pickerAction ->
                                    when (pickerAction) {
                                        AttachmentPickerBack -> onDismiss.invoke()
                                        is AttachmentPickerPollCreation -> Unit
                                    }
                                },
                                attachments = attachmentsPickerViewModel.attachments,
                                onAttachmentItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
                                onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                                onAttachmentsSubmitted = {
                                    onAttachmentsSelected(attachmentsPickerViewModel.getAttachmentsFromMetaData(it))
                                },
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentsTypeMenu(
    channel: Channel,
    tabFactories: List<AttachmentsPickerTabFactory>,
    onClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabFactories.forEachIndexed { index, tabFactory ->
            AttachmentsTypeMenuItem(
                tabFactory = tabFactory,
                isEnabled = tabFactory.isPickerTabEnabled(channel),
                index = index,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun AttachmentsTypeMenuItem(
    tabFactory: AttachmentsPickerTabFactory,
    isEnabled: Boolean,
    index: Int,
    onClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.clickable(enabled = isEnabled) { onClick(index) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val backgroundColor: Color
        val label: String

        when (tabFactory.attachmentsPickerMode) {
            is Images -> {
                backgroundColor = Color(0xFFCCCCFF)
                label = "Images"
            }

            is Files -> {
                backgroundColor = Color(0xFFFFCCCC)
                label = "Files"
            }

            is MediaCapture -> {
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
            tabFactory.PickerTabIcon(isEnabled, isSelected = false)
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
        horizontalArrangement = Arrangement.SpaceBetween
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
            onClick = onSubmitClick
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
            Channel(),
            tabFactories = ChatTheme.attachmentsPickerTabFactories,
        ) {}
    }
}
