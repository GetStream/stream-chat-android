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

package io.getstream.chat.android.compose.ui.messages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.moderatedmessage.ModeratedMessageDialog
import io.getstream.chat.android.compose.ui.components.poll.PollAnswersDialog
import io.getstream.chat.android.compose.ui.components.poll.PollMoreOptionsDialog
import io.getstream.chat.android.compose.ui.components.poll.PollViewResultDialog
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerMenu
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.messages.list.ThreadMessagesStart
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.ReactionSortingByFirstReactionAt
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.Delete
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Flag
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.Resend
import io.getstream.chat.android.ui.common.state.messages.list.DeleteMessage
import io.getstream.chat.android.ui.common.state.messages.list.EditMessage
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageFailedModerationState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageOptionsState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsPickerState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageState
import io.getstream.chat.android.ui.common.state.messages.list.SendAnyway
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.updateMessage

/**
 * Default root Messages screen component, that provides the necessary ViewModels and
 * connects all the data handling operations, as well as some basic actions, like back pressed handling.
 *
 * Because this screen can be shown only if there is an active/selected Channel, the user must provide
 * a [viewModelFactory] that contains the channel ID, in order to load up all the data. Otherwise, we can't show the UI.
 *
 * @param viewModelFactory The factory used to build ViewModels and power the behavior.
 * You can customize the behavior of the list through its parameters. For default behavior,
 * simply create an instance and pass in just the channel ID and the context.
 * @param showHeader If we're showing the header or not.
 * @param reactionSorting The sorting type for reactions. Default is [ReactionSortingByFirstReactionAt].
 * @param onBackPressed Handler for when the user taps on the Back button and/or the system
 * back button.
 * @param onHeaderTitleClick Handler for when the user taps on the header section.
 * @param onChannelAvatarClick Handler called when the user taps on the channel avatar.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param skipPushNotification If new messages should skip triggering a push notification when sent. False by default.
 * @param skipEnrichUrl If new messages being sent, or existing ones being updated should skip enriching the URL.
 * If URL is not enriched, it will not be displayed as a link attachment. False by default.
 * @param showAnonymousAvatar If the user avatar should be shown on comments for polls with anonymous voting visibility.
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Top].
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: [ThreadMessagesStart.BOTTOM].
 * @param topBarContent custom top bar content to be displayed on top of the messages list.
 * @param bottomBarContent custom bottom bar content to be displayed at the bottom of the messages list.
 */
@Suppress("LongMethod")
@Composable
public fun MessagesScreen(
    viewModelFactory: MessagesViewModelFactory,
    showHeader: Boolean = true,
    reactionSorting: ReactionSorting = ReactionSortingByFirstReactionAt,
    onBackPressed: () -> Unit = {},
    onHeaderTitleClick: (channel: Channel) -> Unit = {},
    onChannelAvatarClick: (() -> Unit)? = null,
    onComposerLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onMessageLinkClick: ((Message, String) -> Unit)? = null,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    skipPushNotification: Boolean = false,
    skipEnrichUrl: Boolean = false,
    showAnonymousAvatar: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    topBarContent: @Composable (BackAction) -> Unit = {
        DefaultTopBarContent(
            viewModelFactory = viewModelFactory,
            backAction = it,
            onHeaderTitleClick = onHeaderTitleClick,
            onChannelAvatarClick = onChannelAvatarClick,
        )
    },
    bottomBarContent: @Composable () -> Unit = {
        DefaultBottomBarContent(
            viewModelFactory = viewModelFactory,
            onComposerLinkPreviewClick = onComposerLinkPreviewClick,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
    },
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    val messageMode = listViewModel.messageMode

    if (messageMode is MessageMode.MessageThread) {
        composerViewModel.setMessageMode(messageMode)
    }

    val backAction: BackAction =
        {
            val isStartedForThread = listViewModel.isStartedForThread
            val isInThread = listViewModel.isInThread
            val isShowingOverlay = listViewModel.isShowingOverlay

            when {
                attachmentsPickerViewModel.isShowingAttachments -> attachmentsPickerViewModel.changeAttachmentState(
                    false,
                )

                isShowingOverlay -> listViewModel.selectMessage(null)
                isStartedForThread -> onBackPressed()
                isInThread -> {
                    listViewModel.leaveThread()
                    composerViewModel.leaveThread()
                }

                else -> onBackPressed()
            }
        }

    BackHandler(enabled = true, onBack = backAction)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            // Explicitly consume IME inset (even if not needed), to avoid children applying it again on some devices.
            .consumeWindowInsets(WindowInsets.ime)
            .testTag("Stream_MessagesScreen"),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showHeader) {
                    topBarContent(backAction)
                }
            },
            bottomBar = {
                bottomBarContent()
            },
            containerColor = ChatTheme.colors.appBackground,
        ) { contentPadding ->
            val currentState by listViewModel.currentMessagesState

            MessageList(
                modifier = Modifier
                    .testTag("Stream_MessagesList")
                    .fillMaxSize(),
                contentPadding = contentPadding,
                viewModel = listViewModel,
                reactionSorting = reactionSorting,
                messagesLazyListState = rememberMessageListState(parentMessageId = currentState.parentMessageId),
                verticalArrangement = verticalArrangement,
                threadMessagesStart = threadMessagesStart,
                onThreadClick = remember(composerViewModel, listViewModel) {
                    {
                            message ->
                        composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                        listViewModel.openMessageThread(message)
                    }
                },
                onUserAvatarClick = onUserAvatarClick,
                onMessageLinkClick = onMessageLinkClick,
                onUserMentionClick = onUserMentionClick,
                onReply = { message -> composerViewModel.performMessageAction(Reply(message)) },
                onMediaGalleryPreviewResult = remember(listViewModel, composerViewModel) {
                    {
                            result ->
                        when (result?.resultType) {
                            MediaGalleryPreviewResultType.QUOTE -> {
                                val message = listViewModel.getMessageById(result.messageId)

                                if (message != null) {
                                    composerViewModel.performMessageAction(
                                        Reply(
                                            message.copy(
                                                skipPushNotification = skipPushNotification,
                                                skipEnrichUrl = skipEnrichUrl,
                                            ),
                                        ),
                                    )
                                }
                            }

                            MediaGalleryPreviewResultType.SHOW_IN_CHAT -> {
                                listViewModel.scrollToMessage(
                                    messageId = result.messageId,
                                    parentMessageId = result.parentMessageId,
                                )
                            }

                            null -> Unit
                        }
                    }
                },
            )
        }

        MessageMenus(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        MessageModerationDialog(
            listViewModel = listViewModel,
            composerViewModel = composerViewModel,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
        MessageDialogs(listViewModel = listViewModel)
        PollDialogs(
            listViewModel = listViewModel,
            showAnonymousAvatar = showAnonymousAvatar,
        )
    }
}

/**
 * Callback for when the user taps on the back button.
 */
public typealias BackAction = () -> Unit

@Composable
internal fun DefaultTopBarContent(
    viewModelFactory: MessagesViewModelFactory,
    backAction: BackAction,
    onHeaderTitleClick: (channel: Channel) -> Unit,
    onChannelAvatarClick: (() -> Unit)?,
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)

    val connectionState by listViewModel.connectionState.collectAsState()
    val user by listViewModel.user.collectAsState()
    val messageMode = listViewModel.messageMode

    ChatTheme.componentFactory.MessageListHeader(
        modifier = Modifier
            .height(56.dp),
        channel = listViewModel.channel,
        currentUser = user,
        typingUsers = listViewModel.typingUsers,
        connectionState = connectionState,
        messageMode = messageMode,
        onBackPressed = backAction,
        onHeaderTitleClick = onHeaderTitleClick,
        onChannelAvatarClick = onChannelAvatarClick,
    )
}

@Composable
internal fun DefaultBottomBarContent(
    viewModelFactory: MessagesViewModelFactory,
    onComposerLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    skipPushNotification: Boolean = false,
    skipEnrichUrl: Boolean = false,
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    Column {
        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = composerViewModel,
            isAttachmentPickerVisible = attachmentsPickerViewModel.isShowingAttachments,
            onAttachmentsClick = attachmentsPickerViewModel::toggleAttachmentState,
            onAttachmentRemoved = { attachment ->
                composerViewModel.removeSelectedAttachment(attachment)
                attachmentsPickerViewModel.removeSelectedAttachment(attachment)
            },
            onCancelAction = {
                listViewModel.dismissAllMessageActions()
                composerViewModel.dismissMessageActions()
            },
            onLinkPreviewClick = onComposerLinkPreviewClick,
            onSendMessage = { message ->
                attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
                composerViewModel.sendMessage(
                    message.copy(
                        skipPushNotification = skipPushNotification,
                        skipEnrichUrl = skipEnrichUrl,
                    ),
                )
            },
        )

        AttachmentPickerMenu(
            attachmentsPickerViewModel = attachmentsPickerViewModel,
            composerViewModel = composerViewModel,
        )
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
public fun BoxScope.MessageMenus(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val messagesState by listViewModel.currentMessagesState
    val selectedMessageState = messagesState.selectedMessageState

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
        ownCapabilities = ownCapabilities,
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
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        ChatTheme.componentFactory.MessageMenu(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween(),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2),
                    ),
                ),
            messageOptions = messageOptions,
            message = selectedMessage,
            ownCapabilities = ownCapabilities,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(it)
                        listViewModel.performMessageAction(it)
                    }
                }
            },
            onShowMore = remember(listViewModel) {
                {
                    listViewModel.selectExtendedReactions(selectedMessage)
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
        )
    }

    AnimatedVisibility(
        visible = selectedMessageState is SelectedMessageReactionsState && selectedMessage.id.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        ChatTheme.componentFactory.ReactionsMenu(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween(),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2),
                    ),
                ),
            currentUser = user,
            message = selectedMessage,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(it)
                        listViewModel.performMessageAction(it)
                    }
                }
            },
            onShowMoreReactionsSelected = remember(listViewModel) {
                {
                    listViewModel.selectExtendedReactions(selectedMessage)
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
            ownCapabilities = selectedMessageState?.ownCapabilities ?: setOf(),
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
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        ChatTheme.componentFactory.MessageReactionPicker(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(max = 400.dp)
                .wrapContentHeight()
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween(),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2),
                    ),
                ),
            message = selectedMessage,
            onMessageAction = remember(composerViewModel, listViewModel) {
                {
                        action ->
                    action.updateMessage(
                        action.message.copy(
                            skipPushNotification = skipPushNotification,
                            skipEnrichUrl = skipEnrichUrl,
                        ),
                    ).let {
                        composerViewModel.performMessageAction(action)
                        listViewModel.performMessageAction(action)
                    }
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.removeOverlay() } },
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
public fun MessageModerationDialog(
    listViewModel: MessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    skipPushNotification: Boolean,
    skipEnrichUrl: Boolean,
) {
    val currentMessagesState by listViewModel.currentMessagesState
    val selectedMessageState = currentMessagesState.selectedMessageState

    val selectedMessage = selectedMessageState?.message ?: Message()

    if (selectedMessageState is SelectedMessageFailedModerationState) {
        ModeratedMessageDialog(
            message = selectedMessage,
            modifier = Modifier.background(
                shape = MaterialTheme.shapes.medium,
                color = ChatTheme.colors.inputBackground,
            ),
            onDismissRequest = remember(listViewModel) { { listViewModel.removeOverlay() } },
            onDialogOptionInteraction = remember(listViewModel, composerViewModel) {
                {
                        message, action ->
                    when (action) {
                        DeleteMessage -> listViewModel.deleteMessage(message = message, true)
                        EditMessage -> composerViewModel.performMessageAction(Edit(message))
                        SendAnyway -> listViewModel.performMessageAction(
                            Resend(
                                message.copy(
                                    skipPushNotification = skipPushNotification,
                                    skipEnrichUrl = skipEnrichUrl,
                                ),
                            ),
                        )

                        else -> {
                            // Custom events
                        }
                    }
                }
            },
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
public fun MessageDialogs(listViewModel: MessageListViewModel) {
    val messageActions = listViewModel.messageActions

    val deleteAction = messageActions.firstOrNull { it is Delete }

    if (deleteAction != null) {
        SimpleDialog(
            modifier = Modifier.padding(16.dp),
            title = stringResource(id = R.string.stream_compose_delete_message_title),
            message = stringResource(id = R.string.stream_compose_delete_message_text),
            onPositiveAction = remember(listViewModel) { { listViewModel.deleteMessage(deleteAction.message) } },
            onDismiss = remember(listViewModel) { { listViewModel.dismissMessageAction(deleteAction) } },
        )
    }

    val flagAction = messageActions.firstOrNull { it is Flag }

    if (flagAction != null) {
        SimpleDialog(
            modifier = Modifier.padding(16.dp),
            title = stringResource(id = R.string.stream_compose_flag_message_title),
            message = stringResource(id = R.string.stream_compose_flag_message_text),
            onPositiveAction = remember(listViewModel) {
                {
                    listViewModel.flagMessage(
                        flagAction.message,
                        reason = null,
                        customData = emptyMap(),
                    )
                }
            },
            onDismiss = remember(listViewModel) { { listViewModel.dismissMessageAction(flagAction) } },
        )
    }
}

@Composable
public fun PollDialogs(
    listViewModel: MessageListViewModel,
    showAnonymousAvatar: Boolean,
) {
    val dismiss = { listViewModel.displayPollMoreOptions(null) }
    val selectedPoll = listViewModel.pollState.selectedPoll

    if (selectedPoll?.pollSelectionType == PollSelectionType.MoreOption) {
        PollMoreOptionsDialog(
            selectedPoll = selectedPoll,
            onDismissRequest = { dismiss.invoke() },
            onBackPressed = { dismiss.invoke() },
            listViewModel = listViewModel,
        )
    }

    if (selectedPoll?.pollSelectionType == PollSelectionType.ViewResult) {
        PollViewResultDialog(
            selectedPoll = selectedPoll,
            onDismissRequest = { dismiss.invoke() },
            onBackPressed = { dismiss.invoke() },
        )
    }

    if (selectedPoll?.pollSelectionType == PollSelectionType.ViewAnswers) {
        PollAnswersDialog(
            selectedPoll = selectedPoll,
            showAnonymousAvatar = showAnonymousAvatar,
            listViewModel = listViewModel,
            onDismissRequest = { dismiss.invoke() },
            onBackPressed = { dismiss.invoke() },
        )
    }
}
