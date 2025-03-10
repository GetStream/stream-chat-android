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
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutConstraints
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutInfo
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneDestination
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneNavigator
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneRole
import io.getstream.chat.android.compose.ui.util.adaptivelayout.rememberThreePaneNavigator
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
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * Represents a complete screen for chat, including a list of channels, threads, and messages.
 * The layout adapts based on the screen size, showing a single-pane layout on smaller screens
 * and a dual-pane layout on larger screens with an optional info pane.
 *
 * @param modifier The modifier to be applied to the root layout of the screen.
 * @param navigator The navigator used for managing the navigation between destinations.
 * Defaults to [rememberThreePaneNavigator].
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
 * @param onViewChannelInfoClick Callback invoked when the user long presses a channel and clicks "View Info".
 * @param listTopBarContent The content to display at the top of the list pane.
 * @param listBottomBarContent The content to display at the bottom of the list pane.
 * @param detailTopBarContent The content to display at the top of the detail pane.
 * @param detailBottomBarContent The content to display at the bottom of the detail pane.
 * @param infoContent The content to display in the optional info pane given the provided arguments.
 */
@ExperimentalStreamChatApi
@Suppress("LongMethod")
@Composable
public fun ChatScreen(
    modifier: Modifier = Modifier,
    navigator: ThreePaneNavigator = rememberThreePaneNavigator(),
    channelViewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    threadsViewModelFactory: ThreadsViewModelFactory = ThreadsViewModelFactory(),
    messagesViewModelFactoryProvider: MessagesViewModelFactoryProvider = DefaultMessagesViewModelFactoryProvider(),
    title: String = "Stream Chat",
    searchMode: SearchMode = SearchMode.None,
    listContentMode: ListContentMode = ListContentMode.Channels,
    onBackPress: () -> Unit = {},
    onListTopBarAvatarClick: (User?) -> Unit = {},
    onListTopBarActionClick: () -> Unit = {},
    onDetailTopBarTitleClick: (channel: Channel) -> Unit = {},
    onViewChannelInfoClick: (channel: Channel) -> Unit = {},
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
    infoContent: @Composable (arguments: Any?) -> Unit = {},
) {
    val context = LocalContext.current
    val singlePane = AdaptiveLayoutInfo.singlePaneWindow()

    val backPressHandler = {
        // Navigate back when the user presses back in single-pane mode
        if (singlePane && navigator.canNavigateBack()) {
            navigator.navigateBack()
        } else {
            onBackPress()
        }
    }

    // Pop up navigation to the list when switching between list content modes
    DisposableEffect(listContentMode) {
        onDispose { navigator.popUpTo(ThreePaneRole.List) }
    }

    // Initial navigation when the provider returns a factory based on an empty selection
    LaunchedEffect(Unit) {
        messagesViewModelFactoryProvider(context, ChatMessageSelection())?.let { viewModelFactory ->
            navigator.navigateTo(
                ThreePaneDestination(
                    pane = ThreePaneRole.Detail,
                    arguments = ChatMessageSelection(
                        channelId = viewModelFactory.channelId,
                        messageId = viewModelFactory.messageId,
                        parentMessageId = viewModelFactory.parentMessageId,
                    ),
                ),
            )
        }
    }

    val listPane = remember(listContentMode) {
        movableContentOf { modifier: Modifier ->
            Scaffold(
                modifier = modifier,
                topBar = { listTopBarContent() },
                bottomBar = { listBottomBarContent() },
            ) { padding ->
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
                                    navigator.navigateTo(
                                        destination = ThreePaneDestination(
                                            pane = ThreePaneRole.Detail,
                                            arguments = ChatMessageSelection(channelId = channel.cid),
                                        ),
                                        popUpTo = ThreePaneRole.List,
                                    )
                                },
                                onSearchMessageItemClick = { message ->
                                    navigator.navigateTo(
                                        destination = ThreePaneDestination(
                                            pane = ThreePaneRole.Detail,
                                            arguments = ChatMessageSelection(
                                                channelId = message.cid,
                                                messageId = message.id,
                                            ),
                                        ),
                                        popUpTo = ThreePaneRole.List,
                                    )
                                },
                                onViewChannelInfoAction = onViewChannelInfoClick,
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
                                    navigator.navigateTo(
                                        destination = ThreePaneDestination(
                                            pane = ThreePaneRole.Detail,
                                            arguments = ChatMessageSelection(
                                                channelId = thread.cid,
                                                parentMessageId = thread.parentMessageId,
                                            ),
                                        ),
                                        popUpTo = ThreePaneRole.List,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    val detailPane = remember {
        movableContentOf { selection: ChatMessageSelection ->
            messagesViewModelFactoryProvider(context, selection)?.let { viewModelFactory ->
                DetailPane(
                    viewModelFactory = viewModelFactory,
                    topBarContent = detailTopBarContent,
                    bottomBarContent = detailBottomBarContent,
                    onBackPress = backPressHandler,
                )
            }
        }
    }

    if (singlePane) {
        Box(
            modifier = modifier,
        ) {
            AnimatedContent(
                targetState = navigator.current,
                transitionSpec = {
                    val isNavigatingForward = navigator.destinations.contains(initialState)
                    slideContentTransform(isNavigatingForward = isNavigatingForward)
                },
            ) { destination ->
                when (destination.pane) {
                    ThreePaneRole.List -> listPane(Modifier)
                    ThreePaneRole.Detail -> detailPane(destination.arguments as ChatMessageSelection)
                    ThreePaneRole.Info -> infoContent(destination.arguments)
                }
            }
        }
    } else {
        // Auto-select the first item in the list when it loads on wide screens
        when (listContentMode) {
            ListContentMode.Channels -> {
                FirstChannelLoadHandler(channelViewModelFactory) { selection ->
                    navigator.navigateTo(ThreePaneDestination(ThreePaneRole.Detail, selection))
                }
            }

            ListContentMode.Threads -> {
                FirstThreadLoadHandler(threadsViewModelFactory) { selection ->
                    navigator.navigateTo(ThreePaneDestination(ThreePaneRole.Detail, selection))
                }
            }
        }
        Row(
            modifier = modifier,
        ) {
            listPane(Modifier.weight(AdaptiveLayoutConstraints.LIST_PANE_WEIGHT))

            VerticalDivider()

            var detailPaneSize by remember { mutableStateOf(IntSize.Zero) }

            Row(
                modifier = Modifier
                    .weight(AdaptiveLayoutConstraints.DETAIL_PANE_WEIGHT)
                    .onSizeChanged { size -> detailPaneSize = size },
            ) {
                val detailDestination by remember(navigator.destinations) {
                    derivedStateOf {
                        navigator.destinations.lastOrNull { destination ->
                            destination.pane == ThreePaneRole.Detail
                        }
                    }
                }
                Crossfade(
                    modifier = Modifier.weight(1f),
                    targetState = detailDestination,
                ) { state ->
                    if (state != null) {
                        detailPane(state.arguments as ChatMessageSelection)
                    }
                }
                val infoDestination by remember(navigator.destinations) {
                    derivedStateOf {
                        navigator.destinations.lastOrNull { destination ->
                            destination.pane == ThreePaneRole.Info
                        }
                    }
                }
                val infoPaneOffsetX by animateFloatAsState(
                    targetValue = if (infoDestination == null) {
                        detailPaneSize.width / 2f
                    } else {
                        0f
                    },
                )
                val infoPaneWeight by animateFloatAsState(
                    targetValue = if (infoDestination == null) {
                        0f
                    } else {
                        1f
                    },
                )
                if (infoPaneWeight > 0f) {
                    Box(
                        modifier = Modifier.weight(infoPaneWeight),
                    ) {
                        Crossfade(
                            modifier = Modifier.offset { IntOffset(x = infoPaneOffsetX.toInt(), y = 0) },
                            targetState = infoDestination,
                        ) { state ->
                            if (state != null) {
                                Row {
                                    VerticalDivider()
                                    infoContent(state.arguments)
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
 * A lambda function that provides a [MessagesViewModelFactory] for managing messages within a selected channel.
 */
public typealias MessagesViewModelFactoryProvider =
    (context: Context, selection: ChatMessageSelection) -> MessagesViewModelFactory?

/**
 * Calls the provided block when the first channel item is loaded.
 */
@Composable
private fun FirstChannelLoadHandler(
    channelViewModelFactory: ChannelViewModelFactory,
    block: (selection: ChatMessageSelection) -> Unit,
) {
    val viewModel = viewModel(ChannelListViewModel::class.java, factory = channelViewModelFactory)
    val isLoading = viewModel.channelsState.isLoading
    val itemList = viewModel.channelsState.channelItems
    LaunchedEffect(isLoading) {
        if (!isLoading && itemList.isNotEmpty()) {
            when (val item = itemList.first()) {
                is ItemState.ChannelItemState ->
                    block(ChatMessageSelection(channelId = item.channel.cid))

                is ItemState.SearchResultItemState ->
                    block(
                        ChatMessageSelection(
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
    block: (selection: ChatMessageSelection) -> Unit,
) {
    val viewModel = viewModel(ThreadListViewModel::class.java, factory = threadsViewModelFactory)
    val state by viewModel.state.collectAsState()
    val isLoading = state.isLoading
    val threadList = state.threads
    LaunchedEffect(isLoading) {
        if (!isLoading && threadList.isNotEmpty()) {
            val thread = threadList.first()
            block(
                ChatMessageSelection(
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
            val showNavigationIcon = AdaptiveLayoutInfo.singlePaneWindow() || messageMode is MessageMode.MessageThread
            if (showNavigationIcon) {
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
    // Restart composition on every new combination of values
    key(viewModelFactory.channelId, viewModelFactory.messageId, viewModelFactory.parentMessageId) {
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
 * The content transform used for animating the content when navigating in single-pane mode.
 */
private fun slideContentTransform(isNavigatingForward: Boolean): ContentTransform =
    (
        fadeIn() +
            slideInHorizontally(initialOffsetX = { fullWidth -> if (isNavigatingForward) fullWidth else -fullWidth })
            togetherWith
            slideOutHorizontally(targetOffsetX = { fullWidth -> if (isNavigatingForward) -fullWidth else fullWidth }) +
            fadeOut()
        )

private class DefaultMessagesViewModelFactoryProvider : MessagesViewModelFactoryProvider {
    override fun invoke(context: Context, selection: ChatMessageSelection): MessagesViewModelFactory? =
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
