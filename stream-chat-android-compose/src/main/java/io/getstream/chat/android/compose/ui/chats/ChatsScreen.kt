/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.chats

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.messages.BackAction
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.threads.ThreadList
import io.getstream.chat.android.compose.ui.util.AdaptiveLayoutConstraints
import io.getstream.chat.android.compose.ui.util.AdaptiveLayoutInfo
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.compose.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.compose.viewmodel.threads.ThreadsViewModelFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

/**
 * Represents a complete screen for chat, including a list of channels, threads, and messages.
 * The layout adapts based on the screen size, showing a single-panel layout on smaller screens
 * and a dual-panel layout on larger screens.
 *
 * @param modifier The modifier to be applied to the root layout of the screen.
 * @param channelViewModelFactory Factory for creating the [ChannelListViewModel] used for managing channel data.
 * @param threadsViewModelFactory Factory for creating the [ThreadListViewModel] used for managing thread data.
 * @param messagesViewModelFactoryProvider A lambda function that provides a [MessagesViewModelFactory]
 * for managing messages within a selected channel.
 * The factory is created dynamically based on the selected channel and message context (if any).
 * When the initial [MessagesViewModelFactory] is requested (before a channel is selected),
 * `channelId`, `messageId`, and `parentMessageId` are `null`.
 * @param title The title displayed in the list pane top bar. Default is `"Stream Chat"`.
 * @param searchMode The current search mode for the chat screen. Default is [SearchMode.None].
 * @param listContentMode The mode for displaying the list content in the chat screen.
 * Default is [ListContentMode.Channels].
 * @param onBackPress Callback invoked when the user presses the back button.
 * @param onListTopBarAvatarClick Callback invoked when the user clicks on the avatar in the list pane top bar.
 * @param onListTopBarActionClick Callback invoked when the user clicks on the action icon in the list pane top bar.
 * @param onDetailTopBarTitleClick Callback invoked when the user clicks on the title in the detail pane top bar.
 * @param onViewChannelInfoAction Callback invoked when the user selects a channel to view its details.
 * @param listTopBarContent The content to display at the top of the list pane.
 * @param listBottomBarContent The content to display at the bottom of the list pane.
 * @param detailTopBarContent The content to display at the top of the detail pane.
 * @param detailBottomBarContent The content to display at the bottom of the detail pane.
 */
@ExperimentalStreamChatApi
@Suppress("LongMethod")
@Composable
public fun ChatsScreen(
    modifier: Modifier = Modifier,
    channelViewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    threadsViewModelFactory: ThreadsViewModelFactory = ThreadsViewModelFactory(),
    messagesViewModelFactoryProvider: MessagesViewModelFactoryProvider = DefaultMessagesViewModelFactoryProvider(),
    title: String = "Stream Chat",
    searchMode: SearchMode = SearchMode.None,
    listContentMode: ListContentMode = ListContentMode.Channels,
    extraContentMode: ExtraContentMode = ExtraContentMode.Hidden,
    onBackPress: () -> Unit = {},
    onListTopBarAvatarClick: (User?) -> Unit = {},
    onListTopBarActionClick: () -> Unit = {},
    onDetailTopBarTitleClick: (channel: Channel) -> Unit = {},
    onViewChannelInfoAction: (channel: Channel) -> Unit = {},
    listTopBarContent: @Composable () -> Unit = {
        DefaultListTopBarContent(
            viewModelFactory = channelViewModelFactory,
            title = title,
            onAvatarClick = onListTopBarAvatarClick,
            onActionClick = onListTopBarActionClick,
        )
    },
    listBottomBarContent: @Composable () -> Unit = {},
    detailTopBarContent: @Composable (viewModelFactory: MessagesViewModelFactory, backAction: BackAction) -> Unit =
        { viewModelFactory, backAction ->
            DefaultDetailTopBarContent(
                viewModelFactory = viewModelFactory,
                backAction = backAction,
                onTitleClick = onDetailTopBarTitleClick,
            )
        },
    detailBottomBarContent: @Composable (viewModelFactory: MessagesViewModelFactory) -> Unit = { viewModelFactory ->
        DefaultDetailBottomBarContent(viewModelFactory = viewModelFactory)
    },
) {
    val context = LocalContext.current
    val singlePanel = !AdaptiveLayoutInfo.isWidthExpanded()

    var channelMessagesViewModelFactory by rememberSaveable(
        saver = factorySaver { selection -> messagesViewModelFactoryProvider(context, selection) },
    ) {
        mutableStateOf(messagesViewModelFactoryProvider(context, MessageSelection()))
    }
    val backPressHandler = {
        // Clear the messages view model factory when the user navigates back in single-panel mode
        if (singlePanel && channelMessagesViewModelFactory != null) {
            channelMessagesViewModelFactory = null
        } else {
            onBackPress()
        }
    }

    // Clear the messages view model factory when switching between list content modes
    DisposableEffect(listContentMode) {
        onDispose { channelMessagesViewModelFactory = null }
    }

    // Auto-select the first item in the list when it loads on wide screens
    if (!singlePanel) {
        when (listContentMode) {
            ListContentMode.Channels -> {
                FirstChannelLoadHandler(channelViewModelFactory) { selection ->
                    if (channelMessagesViewModelFactory == null) {
                        channelMessagesViewModelFactory = messagesViewModelFactoryProvider(context, selection)
                    }
                }
            }

            ListContentMode.Threads -> {
                FirstThreadLoadHandler(threadsViewModelFactory) { selection ->
                    if (channelMessagesViewModelFactory == null) {
                        channelMessagesViewModelFactory = messagesViewModelFactoryProvider(context, selection)
                    }
                }
            }
        }
    }

    val listPane = remember(listContentMode) {
        movableContentOf { padding: PaddingValues ->
            Crossfade(
                modifier = Modifier.padding(padding),
                targetState = listContentMode,
            ) { mode ->
                when (mode) {
                    ListContentMode.Channels -> {
                        ChannelsScreen(
                            title = title,
                            viewModelFactory = channelViewModelFactory,
                            isShowingHeader = false,
                            searchMode = searchMode,
                            onChannelClick = { channel ->
                                channelMessagesViewModelFactory = messagesViewModelFactoryProvider(
                                    context,
                                    MessageSelection(channelId = channel.cid),
                                )
                            },
                            onSearchMessageItemClick = { message ->
                                channelMessagesViewModelFactory = messagesViewModelFactoryProvider(
                                    context,
                                    MessageSelection(
                                        channelId = message.cid,
                                        messageId = message.id,
                                        parentMessageId = message.parentId,
                                    ),
                                )
                            },
                            onViewChannelInfoAction = onViewChannelInfoAction,
                            onBackPressed = backPressHandler,
                        )
                    }

                    ListContentMode.Threads -> {
                        val viewModel = viewModel(
                            modelClass = ThreadListViewModel::class.java,
                            factory = threadsViewModelFactory,
                        )
                        ThreadList(
                            viewModel = viewModel,
                            onThreadClick = { thread ->
                                channelMessagesViewModelFactory = messagesViewModelFactoryProvider(
                                    context,
                                    MessageSelection(
                                        channelId = thread.cid,
                                        parentMessageId = thread.parentMessageId,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (singlePanel) {
        Box(
            modifier = modifier,
        ) {
            Scaffold(
                topBar = { listTopBarContent() },
                bottomBar = { listBottomBarContent() },
                content = listPane,
            )
            AnimatedContent(
                targetState = channelMessagesViewModelFactory,
                transitionSpec = slideTransitionSpec(),
            ) { viewModelFactory ->
                if (viewModelFactory != null) {
                    DetailPane(
                        viewModelFactory = viewModelFactory,
                        topBarContent = detailTopBarContent,
                        bottomBarContent = detailBottomBarContent,
                        onBackPress = backPressHandler,
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier,
        ) {
            Scaffold(
                modifier = Modifier.weight(AdaptiveLayoutConstraints.LIST_PANE_WEIGHT),
                topBar = { listTopBarContent() },
                bottomBar = { listBottomBarContent() },
                content = listPane,
            )
            VerticalDivider()
            var detailPaneSize by remember { mutableStateOf(IntSize.Zero) }
            Row(
                modifier = Modifier
                    .weight(AdaptiveLayoutConstraints.DETAIL_PANE_WEIGHT)
                    .onSizeChanged { size -> detailPaneSize = size },
            ) {
                Crossfade(
                    modifier = Modifier.weight(1f),
                    targetState = channelMessagesViewModelFactory,
                ) { viewModelFactory ->
                    if (viewModelFactory != null) {
                        DetailPane(
                            viewModelFactory = viewModelFactory,
                            topBarContent = detailTopBarContent,
                            bottomBarContent = detailBottomBarContent,
                            onBackPress = backPressHandler,
                        )
                    }
                }
                val extraPaneOffsetX by animateFloatAsState(
                    targetValue = if (extraContentMode is ExtraContentMode.Hidden) {
                        detailPaneSize.width / 2f
                    } else {
                        0f
                    },
                )
                val extraPaneWeight by animateFloatAsState(
                    targetValue = if (extraContentMode is ExtraContentMode.Hidden) {
                        0f
                    } else {
                        1f
                    },
                )
                if (extraPaneWeight > 0f) {
                    Box(
                        modifier = Modifier.weight(extraPaneWeight),
                    ) {
                        Row(
                            modifier = Modifier.offset { IntOffset(x = extraPaneOffsetX.toInt(), y = 0) },
                        ) {
                            VerticalDivider()
                            Crossfade(
                                targetState = extraContentMode,
                            ) { mode ->
                                if (mode is ExtraContentMode.Display) {
                                    mode.content()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The mode for displaying the list content in the chat screen.
 */
public enum class ListContentMode {
    /**
     * Display the list of channels.
     */
    Channels,

    /**
     * Display the list of threads.
     */
    Threads,
}

/**
 * The mode for displaying extra content in the chat screen.
 */
public sealed class ExtraContentMode {
    /**
     * No extra content is displayed.
     */
    public data object Hidden : ExtraContentMode()

    /**
     * Display extra custom content.
     *
     * @param content The composable content to display.
     */
    public data class Display(val content: @Composable () -> Unit) : ExtraContentMode()
}

/**
 * Represents the selection of a message within a channel.
 */
public data class MessageSelection(
    /**
     * The ID of the selected channel, or `null` if no channel is selected.
     */
    val channelId: String? = null,
    /**
     * The ID of a specific message, or `null` if navigating to a channel without a pre-selected message.
     */
    val messageId: String? = null,
    /**
     * The ID of the parent message (for threads), or `null` if not in a thread.
     */
    val parentMessageId: String? = null,
)

/**
 * A lambda function that provides a [MessagesViewModelFactory] for managing messages within a selected channel.
 */
public typealias MessagesViewModelFactoryProvider =
        (context: Context, selection: MessageSelection) -> MessagesViewModelFactory?

/**
 * Calls the provided block when the first channel item is loaded.
 */
@Composable
private fun FirstChannelLoadHandler(
    channelViewModelFactory: ChannelViewModelFactory,
    block: (selection: MessageSelection) -> Unit,
) {
    val viewModel = viewModel(ChannelListViewModel::class.java, factory = channelViewModelFactory)
    val itemList = viewModel.channelsState.channelItems
    LaunchedEffect(itemList) {
        if (itemList.isNotEmpty()) {
            when (val item = itemList.first()) {
                is ItemState.ChannelItemState ->
                    block(MessageSelection(channelId = item.channel.cid))

                is ItemState.SearchResultItemState ->
                    block(
                        MessageSelection(
                            channelId = item.message.cid,
                            messageId = item.message.id,
                            parentMessageId = item.message.parentId,
                        ),
                    )
            }
        }
    }
}

/**
 * Calls the provided block when the first thread item is loaded.
 */
@Composable
private fun FirstThreadLoadHandler(
    threadsViewModelFactory: ThreadsViewModelFactory,
    block: (selection: MessageSelection) -> Unit,
) {
    val viewModel = viewModel(ThreadListViewModel::class.java, factory = threadsViewModelFactory)
    val state by viewModel.state.collectAsState()
    val threadList = state.threads
    LaunchedEffect(threadList) {
        if (threadList.isNotEmpty()) {
            val thread = threadList.first()
            block(
                MessageSelection(
                    channelId = thread.cid,
                    parentMessageId = thread.parentMessageId,
                ),
            )
        }
    }
}

@Composable
private fun DefaultListTopBarContent(
    viewModelFactory: ChannelViewModelFactory,
    title: String,
    onAvatarClick: (User?) -> Unit,
    onActionClick: () -> Unit,
) {
    val viewModel = viewModel(ChannelListViewModel::class.java, factory = viewModelFactory)
    val user by viewModel.user.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    ChatTheme.componentFactory.ChannelListHeader(
        modifier = Modifier,
        title = title,
        currentUser = user,
        connectionState = connectionState,
        onAvatarClick = onAvatarClick,
        onHeaderActionClick = onActionClick,
    )
}

@Composable
private fun DefaultDetailTopBarContent(
    viewModelFactory: MessagesViewModelFactory,
    backAction: BackAction,
    onTitleClick: (channel: Channel) -> Unit,
) {
    val viewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val connectionState by viewModel.connectionState.collectAsState()
    val user by viewModel.user.collectAsState()
    val messageMode = viewModel.messageMode

    MessageListHeader(
        channel = viewModel.channel,
        currentUser = user,
        connectionState = connectionState,
        typingUsers = viewModel.typingUsers,
        messageMode = messageMode,
        onHeaderTitleClick = onTitleClick,
        leadingContent = {
            if (!AdaptiveLayoutInfo.isWidthExpanded()) {
                with(ChatTheme.componentFactory) {
                    MessageListHeaderLeadingContent(
                        onBackPressed = backAction,
                    )
                }
            }
        },
    )
}

@Composable
private fun DefaultDetailBottomBarContent(viewModelFactory: MessagesViewModelFactory) {
    val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = viewModelFactory)
    val attachmentsPickerViewModel = viewModel(AttachmentsPickerViewModel::class.java, factory = viewModelFactory)

    MessageComposer(
        viewModel = composerViewModel,
        onAttachmentsClick = { attachmentsPickerViewModel.changeAttachmentState(showAttachments = true) },
        onCommandsClick = composerViewModel::toggleCommandsVisibility,
        onCancelAction = {
            listViewModel.dismissAllMessageActions()
            composerViewModel.dismissMessageActions()
        },
        onSendMessage = composerViewModel::sendMessage,
    )
}

@Composable
private fun DetailPane(
    viewModelFactory: MessagesViewModelFactory,
    topBarContent: @Composable (viewModelFactory: MessagesViewModelFactory, onBackPressed: BackAction) -> Unit,
    bottomBarContent: @Composable (viewModelFactory: MessagesViewModelFactory) -> Unit,
    onBackPress: () -> Unit,
) {
    // Restart composition on every new instance of the factory
    key(viewModelFactory) {
        // Scope messages view models to a local store
        // so that they are cleared when the user navigates between channels
        val viewModelStore = remember { ViewModelStore() }
        val viewModelStoreOwner = remember(viewModelStore) {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore get() = viewModelStore
            }
        }

        // Ensure the store is cleared when the composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                viewModelStore.clear()
            }
        }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            MessagesScreen(
                viewModelFactory = viewModelFactory,
                onBackPressed = onBackPress,
                topBarContent = { backAction -> topBarContent(viewModelFactory, backAction) },
                bottomBarContent = { bottomBarContent(viewModelFactory) },
            )
        }
    }
}

/**
 * The transition spec used for animating the content when switching between channels and threads in single-panel mode.
 */
@Composable
private fun <S> slideTransitionSpec(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
    (
        fadeIn() + slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) togetherWith
            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
        )
        .using(
            // Disable clipping to allow content to slide out fully
            SizeTransform(clip = false),
        )
}

private class DefaultMessagesViewModelFactoryProvider : MessagesViewModelFactoryProvider {
    override fun invoke(context: Context, selection: MessageSelection): MessagesViewModelFactory? =
        if (selection.channelId == null) {
            null
        } else {
            MessagesViewModelFactory(
                context = context,
                channelId = selection.channelId,
                messageId = selection.messageId,
                parentMessageId = selection.parentMessageId,
            )
        }
}

/**
 * This [Saver] is used to save and restore the state of the [MessagesViewModelFactory]
 * across configuration changes and process death.
 * It saves the channel ID, message ID, and parent message ID of the factory.
 */
private fun factorySaver(
    messagesViewModelFactory: (selection: MessageSelection) -> MessagesViewModelFactory?,
): Saver<MutableState<MessagesViewModelFactory?>, *> = listSaver(
    save = { state ->
        state.value?.let { factory ->
            listOf(factory.channelId, factory.messageId, factory.parentMessageId)
        } ?: emptyList()
    },
    restore = { state ->
        if (state.isEmpty()) {
            mutableStateOf(null)
        } else {
            val channelId = state[0]
            val messageId = state[1]
            val parentMessageId = state[2]
            mutableStateOf(
                messagesViewModelFactory(MessageSelection(channelId, messageId, parentMessageId)),
            )
        }
    },
)
