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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlin.math.abs

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
 * @param searchMode The current search mode. Default is [SearchMode.None].
 * @param listContentMode The mode for displaying the list content. Default is [ChatListContentMode.Channels].
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
public fun ChatsScreen(
    modifier: Modifier = Modifier,
    navigator: ThreePaneNavigator = rememberThreePaneNavigator(),
    channelViewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    threadsViewModelFactory: ThreadsViewModelFactory = ThreadsViewModelFactory(),
    messagesViewModelFactoryProvider: MessagesViewModelFactoryProvider = DefaultMessagesViewModelFactoryProvider(),
    title: String = "Stream Chat",
    searchMode: SearchMode = SearchMode.None,
    listContentMode: ChatListContentMode = ChatListContentMode.Channels,
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

    val listPane = remember(listContentMode) {
        movableContentOf { modifier: Modifier ->
            Scaffold(
                modifier = modifier.safeDrawingPadding(),
                topBar = { listTopBarContent() },
                bottomBar = { listBottomBarContent() },
            ) { padding ->
                Crossfade(
                    modifier = Modifier.padding(padding),
                    targetState = listContentMode,
                ) { mode ->
                    when (mode) {
                        ChatListContentMode.Channels -> {
                            ChannelsScreen(
                                viewModelFactory = channelViewModelFactory,
                                isShowingHeader = false,
                                searchMode = searchMode,
                                emphasizeClickedItem = !singlePane,
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

                        ChatListContentMode.Threads -> {
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

    val infoPane = remember {
        movableContentOf { arguments: Any? ->
            Box(modifier = Modifier.safeDrawingPadding()) {
                infoContent(arguments)
            }
        }
    }

    if (singlePane) {
        var pagerDestinations by remember { mutableStateOf(navigator.destinations) }
        val pagerState = rememberPagerState(pageCount = pagerDestinations::size)

        // Scroll to the last page when pages are updated.
        LaunchedEffect(pagerState.pageCount) {
            pagerState.animateScrollToPage(pagerState.pageCount - 1)
        }

        LaunchedEffect(navigator.destinations) {
            val diff = navigator.destinations.size - pagerDestinations.size
            if (diff > 0) {
                // When navigating forward, postpone the scroll to the last page until the new page is ready.
                pagerDestinations = navigator.destinations
            } else if (diff < 0) {
                if (abs(diff) > 1) {
                    // When navigating back multiple pages, scroll directly to the new page then update destinations.
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                } else {
                    // When navigating back to the previous page, scroll then update destinations.
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
                pagerDestinations = navigator.destinations
            }
        }

        LaunchedEffect(Unit) {
            navigator.initialDetailNavigation(messagesViewModelFactoryProvider, context)
        }

        Box(
            modifier = modifier,
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = pagerDestinations.size,
                userScrollEnabled = false,
            ) { page ->
                val destination = pagerDestinations[page]
                when (destination.pane) {
                    ThreePaneRole.List -> listPane(Modifier)
                    ThreePaneRole.Detail -> detailPane(destination.arguments as ChatMessageSelection)
                    ThreePaneRole.Info -> infoPane(destination.arguments)
                }
            }
        }
    } else {
        // Auto-select the first item in the list when it loads on wide screens
        AutoSelectFirstItem(
            listContentMode = listContentMode,
            channelViewModelFactory = channelViewModelFactory,
            threadsViewModelFactory = threadsViewModelFactory,
            navigator = navigator,
            messagesViewModelFactoryProvider = messagesViewModelFactoryProvider,
        )
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
                                    infoPane(state.arguments)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoSelectFirstItem(
    listContentMode: ChatListContentMode,
    channelViewModelFactory: ChannelViewModelFactory,
    threadsViewModelFactory: ThreadsViewModelFactory,
    navigator: ThreePaneNavigator,
    messagesViewModelFactoryProvider: MessagesViewModelFactoryProvider,
) {
    val context = LocalContext.current
    when (listContentMode) {
        ChatListContentMode.Channels -> {
            val viewModel = viewModel(ChannelListViewModel::class.java, factory = channelViewModelFactory)
            FirstChannelLoadHandler(viewModel) { selection ->
                navigator.initialDetailNavigation(messagesViewModelFactoryProvider, context) ?: run {
                    viewModel.itemClick(viewModel.channelsState.channelItems.first())
                    navigator.navigateTo(ThreePaneDestination(ThreePaneRole.Detail, selection))
                }
            }
        }

        ChatListContentMode.Threads -> {
            FirstThreadLoadHandler(threadsViewModelFactory) { selection ->
                navigator.initialDetailNavigation(messagesViewModelFactoryProvider, context) ?: run {
                    navigator.navigateTo(ThreePaneDestination(ThreePaneRole.Detail, selection))
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
 * If there's no detail pane, navigate to the initial detail pane,
 * when the provider returns a factory based on an empty selection.
 */
private fun ThreePaneNavigator.initialDetailNavigation(
    messagesViewModelFactoryProvider: MessagesViewModelFactoryProvider,
    context: Context,
) = if (destinations.none { destination -> destination.pane == ThreePaneRole.Detail }) {
    messagesViewModelFactoryProvider(context, ChatMessageSelection())?.let { viewModelFactory ->
        navigateTo(
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
} else {
    Unit
}

/**
 * Calls the provided block when the first channel item is loaded.
 */
@Composable
private fun FirstChannelLoadHandler(
    viewModel: ChannelListViewModel,
    onLoad: (selection: ChatMessageSelection) -> Unit,
) {
    val isLoading = viewModel.channelsState.isLoading
    val itemList = viewModel.channelsState.channelItems
    LaunchedEffect(isLoading) {
        if (!isLoading && itemList.isNotEmpty()) {
            when (val item = itemList.first()) {
                is ItemState.ChannelItemState ->
                    onLoad(ChatMessageSelection(channelId = item.channel.cid))

                is ItemState.SearchResultItemState ->
                    onLoad(
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
    onLoad: (selection: ChatMessageSelection) -> Unit,
) {
    val viewModel = viewModel(ThreadListViewModel::class.java, factory = threadsViewModelFactory)
    val state by viewModel.state.collectAsState()
    val isLoading = state.isLoading
    val threadList = state.threads
    LaunchedEffect(isLoading) {
        if (!isLoading && threadList.isNotEmpty()) {
            val thread = threadList.first()
            onLoad(
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
