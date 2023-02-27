/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.model.DeleteMessage
import io.getstream.chat.android.common.model.EditMessage
import io.getstream.chat.android.common.model.SendAnyway
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.Resend
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.state.messages.SelectedMessageFailedModerationState
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageState
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.moderatedmessage.ModeratedMessageDialog
import io.getstream.chat.android.compose.ui.components.reactionpicker.ReactionsPicker
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedReactionsMenu
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentsPicker
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
 * @param showDateSeparators If we should show date separators or not.
 * @param showSystemMessages If we should show system messages or not.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param onBackPressed Handler for when the user taps on the Back button and/or the system
 * back button.
 * @param onHeaderActionClick Handler for when the user taps on the header action.
 * @param messageId The ID of the message which we wish to focus on, if such exists.
 * @param navigateToThreadViaNotification If true, when a thread message arrives in a push notification,
 * clicking it will automatically open the thread in which the message is located. If false, the SDK will always
 * navigate to the channel containing the thread but will not navigate to the thread itself.
 * @param skipPushNotification If new messages should skip triggering a push notification when sent. False by default.
 * @param skipEnrichUrl If new messages being sent, or existing ones being updated should skip enriching the URL.
 * If URL is not enriched, it will not be displayed as a link attachment. False by default.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
public fun MessagesScreen(
    channelId: String,
    messageLimit: Int = MessageListViewModel.DefaultMessageLimit,
    showHeader: Boolean = true,
    enforceUniqueReactions: Boolean = true,
    showDateSeparators: Boolean = true,
    showSystemMessages: Boolean = true,
    deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    onBackPressed: () -> Unit = {},
    onHeaderActionClick: (channel: Channel) -> Unit = {},
    messageId: String? = null,
    navigateToThreadViaNotification: Boolean = false,
    skipPushNotification: Boolean = false,
    skipEnrichUrl: Boolean = false,
) {
    val factory = buildViewModelFactory(
        context = LocalContext.current,
        channelId = channelId,
        enforceUniqueReactions = enforceUniqueReactions,
        messageLimit = messageLimit,
        showSystemMessages = showSystemMessages,
        showDateSeparators = showDateSeparators,
        deletedMessageVisibility = deletedMessageVisibility,
        messageFooterVisibility = messageFooterVisibility,
        messageId = messageId,
        navigateToThreadViaNotification = navigateToThreadViaNotification,
    )

    val listViewModel = viewModel(MessageListViewModel::class.java, factory = factory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = factory)

    val messageMode = listViewModel.messageMode

    if (messageMode is MessageMode.MessageThread) {
        composerViewModel.setMessageMode(messageMode)
    }

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

    BackHandler(enabled = true, onBack = backAction)

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showHeader) {
                    val connectionState by listViewModel.connectionState.collectAsState()
                    val user by listViewModel.user.collectAsState()

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
                    },
                    onSendMessage = { message ->
                        composerViewModel.sendMessage(
                            message.apply {
                                this.skipPushNotification = skipPushNotification
                                this.skipEnrichUrl = skipEnrichUrl
                            }
                        )
                    },
                )
            }
        ) {
            val currentState = listViewModel.currentMessagesState

            MessageList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(it),
                viewModel = listViewModel,
                lazyListState = rememberMessageListState(parentMessageId = currentState.parentMessageId),
                onThreadClick = { message ->
                    composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                    listViewModel.openMessageThread(message)
                },
                onImagePreviewResult = { result ->
                    when (result?.resultType) {
                        ImagePreviewResultType.QUOTE -> {
                            val message = listViewModel.getMessageWithId(result.messageId)

                            if (message != null) {
                                composerViewModel.performMessageAction(
                                    Reply(
                                        message.apply {
                                            this.skipPushNotification = skipPushNotification
                                            this.skipEnrichUrl = skipEnrichUrl
                                        }
                                    )
                                )
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

        MessageMenus(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        AttachmentsPickerMenu(
            attachmentsPickerViewModel = attachmentsPickerViewModel,
            composerViewModel = composerViewModel
        )
        MessageModerationDialog(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        MessageDialogs(listViewModel = listViewModel)
    }
}

/**
 * Contains the various menus and pickers the user
 * can use to interact with messages.
 *
 * @param listViewModel The [MessageListViewModel] used to read state from.
 * @param composerViewModel The [MessageComposerViewModel] used to read state from.
 * @param skipPushNotification If the message should skip triggering a push notification when sent. False by default. Note, only
 * new messages trigger push notifications, updating edited messages does not.
 * @param skipEnrichUrl If the message should skip enriching the URL. If URL is not enriched, it will not be
 * displayed as a link attachment. False by default.
 */
@Composable
private fun BoxScope.MessageMenus(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState

    val selectedMessage = selectedMessageState?.message ?: Message()

    MessagesScreenMenus(
        listViewModel = listViewModel,
        composerViewModel = composerViewModel,
        selectedMessageState = selectedMessageState,
        selectedMessage = selectedMessage,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
    )

    MessagesScreenReactionsPicker(
        listViewModel = listViewModel,
        composerViewModel = composerViewModel,
        selectedMessageState = selectedMessageState,
        selectedMessage = selectedMessage,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
    )
}

/**
 * Contains selected message and reactions menus
 * wrapped inside an animated composable.
 *
 * @param listViewModel The [MessageListViewModel] used to read state and
 * perform actions.
 * @param composerViewModel The [MessageComposerViewModel] used to read state and
 * perform actions.
 * @param selectedMessageState The state of the currently selected message.
 * @param selectedMessage The currently selected message.
 * @param skipPushNotification If the message should skip triggering a push notification when sent. False by default. Note, only
 * new messages trigger push notifications, updating edited messages does not.
 * @param skipEnrichUrl If the message should skip enriching the URL. If URL is not enriched, it will not be
 * displayed as a link attachment. False by default.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.MessagesScreenMenus(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    selectedMessageState: SelectedMessageState?,
    selectedMessage: Message,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {

    val user by listViewModel.user.collectAsState()

    val ownCapabilities = selectedMessageState?.ownCapabilities ?: setOf()

    val isInThread = listViewModel.isInThread

    val newMessageOptions = defaultMessageOptionsState(
        selectedMessage = selectedMessage,
        currentUser = user,
        isInThread = isInThread,
        ownCapabilities = ownCapabilities
    )

    var messageOptions by remember {
        mutableStateOf<List<MessageOptionItemState>>(emptyList())
    }

    if (newMessageOptions.isNotEmpty()) {
        messageOptions = newMessageOptions
    }

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
            messageOptions = messageOptions,
            message = selectedMessage,
            ownCapabilities = ownCapabilities,
            onMessageAction = { action ->
                action.message.skipPushNotification = skipPushNotification
                action.message.skipEnrichUrl = skipEnrichUrl

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
                action.message.skipPushNotification = skipPushNotification
                action.message.skipEnrichUrl = skipEnrichUrl

                composerViewModel.performMessageAction(action)
                listViewModel.performMessageAction(action)
            },
            onShowMoreReactionsSelected = {
                listViewModel.selectExtendedReactions(selectedMessage)
            },
            onDismiss = { listViewModel.removeOverlay() },
            ownCapabilities = selectedMessageState?.ownCapabilities ?: setOf()
        )
    }
}

/**
 * Contains the reactions picker wrapped inside
 * of an animated composable.
 *
 * @param listViewModel The [MessageListViewModel] used to read state and
 * perform actions.
 * @param composerViewModel [MessageComposerViewModel] used to read state and
 * perform actions.
 * @param selectedMessageState The state of the currently selected message.
 * @param selectedMessage The currently selected message.
 * @param skipPushNotification If the message should skip triggering a push notification when sent. False by default. Note, only
 * new messages trigger push notifications, updating edited messages does not.
 * @param skipEnrichUrl If the message should skip enriching the URL. If URL is not enriched, it will not be
 * displayed as a link attachment. False by default.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.MessagesScreenReactionsPicker(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    selectedMessageState: SelectedMessageState?,
    selectedMessage: Message,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {

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
                action.message.skipPushNotification = skipPushNotification
                action.message.skipEnrichUrl = skipEnrichUrl

                composerViewModel.performMessageAction(action)
                listViewModel.performMessageAction(action)
            },
            onDismiss = { listViewModel.removeOverlay() }
        )
    }
}

/**
 * Contains the attachments picker menu wrapped inside
 * of an animated composable.
 *
 * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] used to read state and
 * perform actions.
 * @param composerViewModel The [MessageComposerViewModel] used to read state and
 * perform actions.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.AttachmentsPickerMenu(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    composerViewModel: MessageComposerViewModel,
) {

    val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

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
}

/**
 * Contains the dialog for a message that needs to be moderated.
 *
 * @param listViewModel The [MessageListViewModel] used to read state and
 * perform actions.
 * @param composerViewModel The [MessageComposerViewModel] used to read state and
 * perform actions.
 */
@Composable
private fun MessageModerationDialog(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val selectedMessageState = listViewModel.currentMessagesState.selectedMessageState

    val selectedMessage = selectedMessageState?.message ?: Message()

    if (selectedMessageState is SelectedMessageFailedModerationState) {
        ModeratedMessageDialog(
            message = selectedMessage,
            modifier = Modifier.background(
                shape = MaterialTheme.shapes.medium,
                color = ChatTheme.colors.inputBackground
            ),
            onDismissRequest = { listViewModel.removeOverlay() },
            onDialogOptionInteraction = { message, action ->
                when (action) {
                    DeleteMessage -> listViewModel.deleteMessage(message = message, true)
                    EditMessage -> composerViewModel.performMessageAction(Edit(message))
                    SendAnyway -> listViewModel.performMessageAction(
                        Resend(
                            message.apply {
                                this.skipPushNotification = skipPushNotification
                                this.skipEnrichUrl = skipEnrichUrl
                            }
                        )
                    )
                    else -> {
                        // Custom events
                    }
                }
            }
        )
    }
}

/**
 * Contains the message dialogs used to prompt the
 * user with message flagging and deletion actions
 *
 * @param listViewModel The [MessageListViewModel] used to read state and
 * perform actions.
 */
@Composable
private fun MessageDialogs(listViewModel: MessageListViewModel) {

    val messageActions = listViewModel.messageActions

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

/**
 * Builds the [MessagesViewModelFactory] required to run the Conversation/Messages screen.
 *
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param messageLimit The limit when loading messages.
 * @param showDateSeparators If we should show date separators or not.
 * @param showSystemMessages If we should show system messages or not. * @param deletedMessageVisibility The behavior of deleted messages in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param messageId The ID of the message which we wish to focus on, if such exists.
 * @param navigateToThreadViaNotification If true, when a thread message arrives in a push notification,
 * clicking it will automatically open the thread in which the message is located. If false, the SDK will always
 * navigate to the channel containing the thread but will not navigate to the thread itself.
 */
@ExperimentalCoroutinesApi
@Suppress("LongParameterList")
private fun buildViewModelFactory(
    context: Context,
    channelId: String,
    enforceUniqueReactions: Boolean,
    messageLimit: Int,
    showDateSeparators: Boolean,
    showSystemMessages: Boolean,
    deletedMessageVisibility: DeletedMessageVisibility,
    messageFooterVisibility: MessageFooterVisibility,
    messageId: String? = null,
    navigateToThreadViaNotification: Boolean = false,
): MessagesViewModelFactory {
    return MessagesViewModelFactory(
        context = context,
        channelId = channelId,
        enforceUniqueReactions = enforceUniqueReactions,
        messageLimit = messageLimit,
        showDateSeparators = showDateSeparators,
        showSystemMessages = showSystemMessages,
        deletedMessageVisibility = deletedMessageVisibility,
        messageFooterVisibility = messageFooterVisibility,
        messageId = messageId,
        navigateToThreadViaNotification = navigateToThreadViaNotification,
    )
}
