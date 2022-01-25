package io.getstream.chat.android.compose.ui.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

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
@ExperimentalFoundationApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun MessagesScreen(
    channelId: String,
    messageLimit: Int = MessageListViewModel.DEFAULT_MESSAGE_LIMIT,
    showHeader: Boolean = true,
    enforceUniqueReactions: Boolean = true,
    showDateSeparators: Boolean = true,
    showSystemMessages: Boolean = true,
    onBackPressed: () -> Unit = {},
    onHeaderActionClick: (channel: Channel) -> Unit = {},
) {
    val factory = buildViewModelFactory(
        context = LocalContext.current,
        channelId = channelId,
        enforceUniqueReactions = enforceUniqueReactions,
        messageLimit = messageLimit,
        showSystemMessages = showSystemMessages,
        showDateSeparators = showDateSeparators
    )

    val listViewModel = viewModel(MessageListViewModel::class.java, factory = factory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = factory)

    val currentState = listViewModel.currentMessagesState
    val messageActions = listViewModel.messageActions

    val selectedMessageState = currentState.selectedMessageState
    val messageMode = listViewModel.messageMode
    val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

    val connectionState by listViewModel.connectionState.collectAsState()
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
                        typingUsers = listViewModel.typingUsers,
                        connectionState = connectionState,
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
                    onCommandsClick = { composerViewModel.toggleCommandsVisibility() },
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
                    composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                    listViewModel.openMessageThread(message)
                },
                onImagePreviewResult = { result ->
                    when (result?.resultType) {
                        ImagePreviewResultType.QUOTE -> {
                            val message = listViewModel.getMessageWithId(result.messageId)

                            if (message != null) {
                                composerViewModel.performMessageAction(Reply(message))
                            }
                        }

                        ImagePreviewResultType.SHOW_IN_CHAT -> {
                            listViewModel.focusMessage(result.messageId)
                        }
                        null -> Unit
                    }
                }
            )
        }

        val selectedMessage = selectedMessageState?.message ?: Message()

        AnimatedVisibility(
            visible = selectedMessageState is SelectedMessageOptionsState && selectedMessage.id.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
        ) {
            SelectedMessageMenu(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween()
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                        )
                    ),
                messageOptions = defaultMessageOptionsState(
                    selectedMessage = selectedMessage,
                    currentUser = user,
                    isInThread = listViewModel.isInThread
                ),
                message = selectedMessage,
                onMessageAction = { action ->
                    composerViewModel.performMessageAction(action)
                    listViewModel.performMessageAction(action)
                },
                onShowMoreReactionsSelected = {
                    listViewModel.selectExtendedReactions(selectedMessage)
                },
                onDismiss = { listViewModel.removeOverlay() }
            )
        }

        AnimatedVisibility(
            visible = selectedMessageState is SelectedMessageReactionsState && selectedMessage.id.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
        ) {
            SelectedReactionsMenu(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween()
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                        )
                    ),
                currentUser = user,
                message = selectedMessage,
                onMessageAction = { action ->
                    composerViewModel.performMessageAction(action)
                    listViewModel.performMessageAction(action)
                },
                onShowMoreReactionsSelected = {
                    listViewModel.selectExtendedReactions(selectedMessage)
                },
                onDismiss = { listViewModel.removeOverlay() }
            )
        }

        AnimatedVisibility(
            visible = selectedMessageState is SelectedMessageReactionsPickerState && selectedMessage.id.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
        ) {
            ReactionsPicker(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .heightIn(max = 400.dp)
                    .wrapContentHeight()
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween()
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                        )
                    ),
                message = selectedMessage,
                onMessageAction = { action ->
                    composerViewModel.performMessageAction(action)
                    listViewModel.performMessageAction(action)
                },
                onDismiss = { listViewModel.removeOverlay() }
            )
        }

        AnimatedVisibility(
            visible = isShowingAttachments,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(delayMillis = AnimationConstants.DefaultDurationMillis / 2))
        ) {
            AttachmentsPicker(
                attachmentsPickerViewModel = attachmentsPickerViewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(350.dp)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween()
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(delayMillis = AnimationConstants.DefaultDurationMillis / 2)
                        )
                    ),
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

        val flagAction = messageActions.firstOrNull { it is Flag }

        if (flagAction != null) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.stream_compose_flag_message_title),
                message = stringResource(id = R.string.stream_compose_flag_message_text),
                onPositiveAction = { listViewModel.flagMessage(flagAction.message) },
                onDismiss = { listViewModel.dismissMessageAction(flagAction) }
            )
        }
    }
}

/**
 * Builds the [MessagesViewModelFactory] required to run the Conversation/Messages screen.
 *
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param messageLimit The limit when loading messages.
 */
private fun buildViewModelFactory(
    context: Context,
    channelId: String,
    enforceUniqueReactions: Boolean,
    messageLimit: Int,
    showDateSeparators: Boolean,
    showSystemMessages: Boolean,
): MessagesViewModelFactory {
    return MessagesViewModelFactory(
        context = context,
        channelId = channelId,
        enforceUniqueReactions = enforceUniqueReactions,
        messageLimit = messageLimit,
        showDateSeparators = showDateSeparators,
        showSystemMessages = showSystemMessages
    )
}
