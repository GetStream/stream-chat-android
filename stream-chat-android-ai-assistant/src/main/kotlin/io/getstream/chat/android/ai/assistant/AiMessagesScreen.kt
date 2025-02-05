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

package io.getstream.chat.android.ai.assistant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageContainer
import io.getstream.chat.android.compose.ui.messages.list.MessageItem
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
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply

/**
 * This is a root Messages screen component for AI assistant, that provides the necessary ViewModels and
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
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Top].
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: [ThreadMessagesStart.BOTTOM].
 * @param topBarContent custom top bar content to be displayed on top of the messages list.
 * @param bottomBarContent custom bottom bar content to be displayed at the bottom of the messages list.
 */
@Suppress("LongMethod")
@Composable
public fun AiMessagesScreen(
    viewModelFactory: MessagesViewModelFactory,
    isAiStarted: Boolean,
    onStartAiAssistant: () -> Unit,
    onStopAiAssistant: () -> Unit,
    showHeader: Boolean = true,
    typingState: TypingState,
    reactionSorting: ReactionSorting = ReactionSortingByFirstReactionAt,
    onBackPressed: () -> Unit = {},
    onComposerLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onHeaderTitleClick: (channel: Channel) -> Unit = {},
    onChannelAvatarClick: () -> Unit = {},
    onMessageLinkClick: ((Message, String) -> Unit)? = null,
    onUserAvatarClick: (User) -> Unit = {},
    skipPushNotification: Boolean = false,
    skipEnrichUrl: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    aiStartButton: @Composable BoxScope.() -> Unit = {
        DefaultAiStartButton(
            isAiStarted = isAiStarted,
            onStartAiAssistant = onStartAiAssistant,
            onStopAiAssistant = onStopAiAssistant,
        )
    },
    topBarContent: @Composable (BackAction) -> Unit = {
        DefaultTopBarContent(
            viewModelFactory = viewModelFactory,
            backAction = it,
            onHeaderTitleClick = onHeaderTitleClick,
            onChannelAvatarClick = onChannelAvatarClick,
        )
    },
    bottomBarContent: @Composable (isAnimating: Boolean) -> Unit = {
        DefaultBottomBarContent(
            viewModelFactory = viewModelFactory,
            onComposerLinkPreviewClick = onComposerLinkPreviewClick,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
        )
    },
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val composerViewModel =
        viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    val messageMode = listViewModel.messageMode
    if (messageMode is MessageMode.MessageThread) {
        composerViewModel.setMessageMode(messageMode)
    }

    var isAnimating by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val backAction: BackAction =
        remember(listViewModel, composerViewModel, attachmentsPickerViewModel) {
            {
                val isStartedForThread = listViewModel.isStartedForThread
                val isInThread = listViewModel.isInThread
                val isShowingOverlay = listViewModel.isShowingOverlay

                when {
                    isImeVisible -> Unit
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (typingState is TypingState.Thinking || typingState is TypingState.Generating || isAnimating) {
                        AiTypingIndicator(
                            modifier = Modifier.padding(12.dp),
                            text = if (typingState == TypingState.Clear && isAnimating) {
                                TypingState.Generating("").name
                            } else {
                                typingState.name
                            },
                        )
                    }
                    bottomBarContent(isAnimating)
                }
            },
        ) {
            val currentState = listViewModel.currentMessagesState
            val state = rememberMessageListState(parentMessageId = currentState.parentMessageId)

            LaunchedEffect(key1 = typingState) {
                if (typingState is TypingState.Thinking || typingState is TypingState.Generating) {
                    state.lazyListState.scrollBy(-300f)
                }
            }

            MessageList(
                modifier = Modifier
                    .testTag("Stream_MessagesList")
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(it),
                viewModel = listViewModel,
                reactionSorting = reactionSorting,
                messagesLazyListState = state,
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
                itemContent = { messageListItem ->
                    MessageContainer(
                        messageListItemState = messageListItem,
                        reactionSorting = reactionSorting,
                        messageItemContent = { itemState ->
                            MessageItem(
                                messageItem = itemState,
                                reactionSorting = reactionSorting,
                                messageContentFactory = AiMessageContentFactory(),
                                onUserAvatarClick = { onUserAvatarClick.invoke(itemState.message.user) },
                                onLongItemClick = {},
                                centerContent = { messageItemState ->
                                    AiRegularMessageContent(
                                        messageItem = messageItemState,
                                        typingState = typingState,
                                        onAnimationState = { value ->
                                            isAnimating = value
                                        },
                                    )
                                },
                            )
                        },
                    )
                },
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

        aiStartButton.invoke(this)
    }
}

/**
 * Callback for when the user taps on the back button.
 */
internal typealias BackAction = () -> Unit

@Composable
internal fun BoxScope.DefaultAiStartButton(
    isAiStarted: Boolean,
    onStartAiAssistant: () -> Unit,
    onStopAiAssistant: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(top = 64.dp, end = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .align(Alignment.TopEnd)
            .background(
                color = if (isAiStarted) {
                    ChatTheme.colors.highlight
                } else {
                    ChatTheme.colors.appBackground
                },
                shape = RoundedCornerShape(8.dp),
            )
            .padding(vertical = 2.dp)
            .height(34.dp)
            .alpha(0.75f),
        color = if (isAiStarted) {
            ChatTheme.colors.highlight
        } else {
            ChatTheme.colors.appBackground
        },
        onClick = {
            if (isAiStarted) {
                onStopAiAssistant()
            } else {
                onStartAiAssistant()
            }
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(28.dp)
                    .padding(4.dp),
                painter = painterResource(id = R.drawable.stream_ai_assistant_ic_wand),
                tint = ChatTheme.colors.textHighEmphasis,
                contentDescription = null,
            )

            Text(
                modifier = Modifier.padding(4.dp),
                text = if (isAiStarted) {
                    "Stop AI"
                } else {
                    "Start AI"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }
}

@Composable
internal fun DefaultTopBarContent(
    viewModelFactory: MessagesViewModelFactory,
    backAction: BackAction,
    onHeaderTitleClick: (channel: Channel) -> Unit,
    onChannelAvatarClick: () -> Unit,
) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)

    val connectionState by listViewModel.connectionState.collectAsState()
    val user by listViewModel.user.collectAsState()
    val messageMode = listViewModel.messageMode

    MessageListHeader(
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
    val composerViewModel =
        viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel =
        viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    MessageComposer(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        viewModel = composerViewModel,
        onAttachmentsClick = remember(attachmentsPickerViewModel) {
            {
                attachmentsPickerViewModel.changeAttachmentState(
                    true,
                )
            }
        },
        onCommandsClick = remember(composerViewModel) {
            {
                composerViewModel.toggleCommandsVisibility()
            }
        },
        onCancelAction = remember(listViewModel, composerViewModel) {
            {
                listViewModel.dismissAllMessageActions()
                composerViewModel.dismissMessageActions()
            }
        },
        onLinkPreviewClick = onComposerLinkPreviewClick,
        onSendMessage = remember(composerViewModel) {
            {
                    message ->
                composerViewModel.sendMessage(
                    message.copy(
                        skipPushNotification = skipPushNotification,
                        skipEnrichUrl = skipEnrichUrl,
                    ),
                )
            }
        },
    )
}
