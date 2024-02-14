package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply

@Composable
fun CustomMessageComposer(cid: String?, onBackClick: () -> Unit = {}) {
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
                    attachmentsPickerViewModel.isShowingAttachments -> attachmentsPickerViewModel.changeAttachmentState(
                        false,
                    )
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
                    MessageComposer(
                        viewModel = composerViewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        input = { composerState ->
                            MessageInput(
                                messageComposerState = composerState,
                                onValueChange = { composerViewModel.setMessageInput(it) },
                                onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .align(Alignment.CenterVertically),
                                label = { // create a custom label with an icon
                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = "Type a message",
                                        color = ChatTheme.colors.textLowEmphasis
                                    )
                                },
                                innerTrailingContent = {
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
                                            onClick = {
                                                composerViewModel.sendMessage(
                                                    composerViewModel.buildNewMessage(
                                                        composerState.inputValue,
                                                        composerState.attachments
                                                    )
                                                )
                                            },
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
                        integrations = {}
                    )
                }
            ) {
                MessageList(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ChatTheme.colors.appBackground)
                        .padding(it),
                    viewModel = listViewModel,
                )
            }

            if (isShowingAttachments) {
                AttachmentsPicker( // Add the picker to your UI
                    attachmentsPickerViewModel = attachmentsPickerViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(350.dp),
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