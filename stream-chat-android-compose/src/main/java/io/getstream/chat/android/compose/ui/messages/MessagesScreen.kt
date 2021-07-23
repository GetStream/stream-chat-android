package io.getstream.chat.android.compose.ui.messages

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.state.messages.Thread
import io.getstream.chat.android.compose.state.messages.list.Copy
import io.getstream.chat.android.compose.state.messages.list.Delete
import io.getstream.chat.android.compose.state.messages.list.Edit
import io.getstream.chat.android.compose.state.messages.list.MessageOption
import io.getstream.chat.android.compose.state.messages.list.MuteUser
import io.getstream.chat.android.compose.state.messages.list.Reply
import io.getstream.chat.android.compose.state.messages.list.ThreadReply
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOption
import io.getstream.chat.android.compose.ui.common.SimpleDialog
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.messages.overlay.SelectedMessageOverlay
import io.getstream.chat.android.compose.ui.util.reactionTypes
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain

/**
 * Default root Messages screen component, that provides the necessary ViewModels and
 * connects all the data handling operations, as well as some basic actions, like back pressed handling.
 *
 * Because this screen can be shown only if there is an active/selected Channel, the user must provide
 * a [channelId] in order to load up all the data. Otherwise, we can't show the UI.
 *
 * @param channelId - The ID of the opened/active Channel.
 * @param messageLimit - The limit of messages per query.
 * @param showHeader - If we're showing the header or not.
 * @param onBackPressed - Handler for when the user taps on the Back button and/or the system
 * back button.
 * @param onHeaderActionClick - Handler for when the user taps on the header action.
 * */
@InternalStreamChatApi
@ExperimentalPermissionsApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MessagesScreen(
    channelId: String,
    messageLimit: Int = 30,
    showHeader: Boolean = true,
    onBackPressed: () -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
) {
    val factory = buildViewModelFactory(LocalContext.current, channelId, messageLimit)

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

    LaunchedEffect(Unit) {
        listViewModel.start()
    }

    val backAction = {
        val isInThread = listViewModel.isInThread
        val isShowingOverlay = listViewModel.isShowingOverlay

        when {
            isShowingAttachments -> attachmentsPickerViewModel.onShowAttachments(false)
            isShowingOverlay -> listViewModel.onMessageSelected(null)
            isInThread -> {
                listViewModel.leaveThread()
                composerViewModel.leaveThread()
            }
            else -> onBackPressed()
        }
    }

    SystemBackPressedHandler(isEnabled = true, onBackPressed = backAction)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize(),
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
                    onAttachmentsClick = { attachmentsPickerViewModel.onShowAttachments(true) },
                    onCancelAction = {
                        listViewModel.dismissAllMessageActions()
                        composerViewModel.onDismissMessageActions()
                    }
                )
            }
        ) {
            MessageList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                viewModel = listViewModel,
                onThreadClick = { message ->
                    composerViewModel.setMessageMode(Thread(message))
                    listViewModel.onMessageThreadClick(message)
                }
            )
        }

        if (selectedMessage != null) {
            SelectedMessageOverlay(
                defaultReactionOptions(selectedMessage.ownReactions),
                defaultMessageOptions(selectedMessage, user, listViewModel.isInThread),
                selectedMessage,
                onMessageAction = { action ->
                    composerViewModel.onMessageAction(action)
                    listViewModel.onMessageAction(action)
                }, onDismiss = { listViewModel.removeOverlay() })
        }

        if (isShowingAttachments) {
            AttachmentsPicker(
                attachmentsPickerViewModel = attachmentsPickerViewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(350.dp),
                onAttachmentsSelected = { attachments ->
                    attachmentsPickerViewModel.onShowAttachments(false)
                    composerViewModel.onAttachmentsSelected(attachments)
                },
                onDismiss = {
                    attachmentsPickerViewModel.onShowAttachments(false)
                    attachmentsPickerViewModel.onDismiss()
                }
            )
        }

        val deleteAction = messageActions.firstOrNull { it is Delete }

        if (deleteAction != null) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.delete_message_title),
                message = stringResource(id = R.string.delete_message_text),
                onPositiveAction = { listViewModel.deleteMessage(deleteAction.message) },
                onDismiss = { listViewModel.dismissMessageAction(deleteAction) }
            )
        }
    }
}

/**
 * Builds the [MessagesViewModelFactory] required to run the Conversation/Messages screen.
 *
 * @param context - Used to build the [ClipboardManager].
 * @param channelId - The current channel ID, to load the messages from.
 * @param messageLimit - The limit when loading messages.
 * */
private fun buildViewModelFactory(
    context: Context,
    channelId: String,
    messageLimit: Int,
): MessagesViewModelFactory {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    return MessagesViewModelFactory(
        context,
        clipboardManager,
        ChatClient.instance(),
        ChatDomain.instance(),
        channelId,
        messageLimit
    )
}

/**
 * Builds the default reaction options we show to our users.
 *
 * @param ownReactions - options the user selected on the message.
 * */
@InternalStreamChatApi
fun defaultReactionOptions(ownReactions: List<Reaction>): List<ReactionOption> {
    return reactionTypes.entries
        .map { (type, emoji) ->
            ReactionOption(
                emoji = emoji,
                isSelected = ownReactions.any { it.type == "love" },
                type = type
            )
        }
}

/**
 * Builds the default message options we show to our users.
 *
 * @param selectedMessage - Currently selected message, used to callbacks.
 * @param user - Current user, used to expose different states for messages.
 * @param inThread - If the message is in a thread or not, to block off some options.
 * */
fun defaultMessageOptions(
    selectedMessage: Message,
    user: User?,
    inThread: Boolean,
): List<MessageOption> {
    val messageOptions = arrayListOf(
        MessageOption(
            title = R.string.reply,
            icon = Icons.Default.Reply,
            action = Reply(selectedMessage)
        )
    )

    if (selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()) {
        messageOptions.add(
            MessageOption(
                title = R.string.copy_message,
                icon = Icons.Default.FileCopy,
                action = Copy(selectedMessage)
            )
        )
    }

    if (!inThread) {
        messageOptions.add(
            1, MessageOption(
                title = R.string.thread_reply,
                icon = Icons.Default.Chat,
                action = ThreadReply(selectedMessage)
            )
        )
    }

    if (selectedMessage.user.id == user?.id) {
        messageOptions.add(
            MessageOption(
                title = R.string.edit_message,
                icon = Icons.Default.Edit,
                action = Edit(selectedMessage)
            )
        )

        messageOptions.add(
            MessageOption(
                title = R.string.delete_message,
                icon = Icons.Default.Delete,
                action = Delete(selectedMessage),
                iconColor = Color.Red,
                titleColor = Color.Red
            )
        )
    } else {
        messageOptions.add(
            MessageOption(
                title = R.string.mute_user,
                icon = Icons.Default.VolumeMute,
                action = MuteUser(selectedMessage)
            )
        )
    }

    return messageOptions
}