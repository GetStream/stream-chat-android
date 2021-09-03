package io.getstream.chat.android.compose.ui.messages

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.state.messages.Thread
import io.getstream.chat.android.compose.state.messages.list.Delete
import io.getstream.chat.android.compose.ui.common.SimpleDialog
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.messages.overlay.SelectedMessageOverlay
import io.getstream.chat.android.compose.ui.messages.overlay.defaultMessageOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.offline.ChatDomain

/**
 * Default root Messages screen component, that provides the necessary ViewModels and
 * connects all the data handling operations, as well as some basic actions, like back pressed handling.
 *
 * Because this screen can be shown only if there is an active/selected Channel, the user must provide
 * a [channelId] in order to load up all the data. Otherwise, we can't show the UI.
 *
 * @param channelId The ID of the opened/active Channel.
 * @param messageLimit The limit of messages per query.
 * @param showHeader If we're showing the header or not.
 * @param enforceUniqueReactions If we need to enforce unique reactions or not.
 * @param onBackPressed Handler for when the user taps on the Back button and/or the system
 * back button.
 * @param onHeaderActionClick Handler for when the user taps on the header action.
 */
@Composable
public fun MessagesScreen(
    channelId: String,
    messageLimit: Int = 30,
    showHeader: Boolean = true,
    enforceUniqueReactions: Boolean = true,
    onBackPressed: () -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
) {
    val factory = buildViewModelFactory(LocalContext.current, channelId, enforceUniqueReactions, messageLimit)

    val listViewModel = viewModel(MessageListViewModel::class.java, factory = factory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = factory)

    val currentState = listViewModel.currentMessagesState
    val messageActions = listViewModel.messageActions

    val selectedMessage = currentState.selectedMessage
    val messageMode = listViewModel.messageMode
    val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

    val isNetworkAvailable by listViewModel.isOnline.collectAsState()
    val user by listViewModel.user.collectAsState()

    val backAction = {
        val isInThread = listViewModel.isInThread
        val isShowingOverlay = listViewModel.isShowingOverlay

        when {
            attachmentsPickerViewModel.isShowingAttachments -> attachmentsPickerViewModel.changeAttachmentState(false)
            isShowingOverlay -> listViewModel.selectMessage(null)
            isInThread -> {
                listViewModel.leaveThread()
                composerViewModel.leaveThread()
            }
            else -> onBackPressed()
        }
    }

    SystemBackPressedHandler(isEnabled = true, onBackPressed = backAction)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showHeader) {
                    MessageListHeader(
                        modifier = Modifier
                            .height(56.dp),
                        channel = listViewModel.channel,
                        currentUser = user,
                        isNetworkAvailable = isNetworkAvailable,
                        messageMode = messageMode,
                        onBackPressed = backAction,
                        onHeaderActionClick = onHeaderActionClick
                    )
                }
            },
            bottomBar = {
                MessageComposer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    viewModel = composerViewModel,
                    onAttachmentsClick = { attachmentsPickerViewModel.changeAttachmentState(true) },
                    onCancelAction = {
                        listViewModel.dismissAllMessageActions()
                        composerViewModel.dismissMessageActions()
                    }
                )
            }
        ) {
            MessageList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(it),
                viewModel = listViewModel,
                onThreadClick = { message ->
                    composerViewModel.setMessageMode(Thread(message))
                    listViewModel.openMessageThread(message)
                }
            )
        }

        if (selectedMessage != null) {
            SelectedMessageOverlay(
                messageOptions = defaultMessageOptions(selectedMessage, user, listViewModel.isInThread),
                message = selectedMessage,
                onMessageAction = { action ->
                    composerViewModel.performMessageAction(action)
                    listViewModel.performMessageAction(action)
                },
                onDismiss = { listViewModel.removeOverlay() }
            )
        }

        if (isShowingAttachments) {
            AttachmentsPicker(
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

        val deleteAction = messageActions.firstOrNull { it is Delete }

        if (deleteAction != null) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.stream_compose_delete_message_title),
                message = stringResource(id = R.string.stream_compose_delete_message_text),
                onPositiveAction = { listViewModel.deleteMessage(deleteAction.message) },
                onDismiss = { listViewModel.dismissMessageAction(deleteAction) }
            )
        }
    }
}

/**
 * Builds the [MessagesViewModelFactory] required to run the Conversation/Messages screen.
 *
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param messageLimit The limit when loading messages.
 */
private fun buildViewModelFactory(
    context: Context,
    channelId: String,
    enforceUniqueReactions: Boolean,
    messageLimit: Int,
): MessagesViewModelFactory {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    return MessagesViewModelFactory(
        context,
        clipboardManager,
        ChatClient.instance(),
        ChatDomain.instance(),
        channelId,
        enforceUniqueReactions,
        messageLimit
    )
}
