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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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

private data class PaneState(
    val detail: Detail? = null,
    val extra: Extra? = null,
) {

    data class Detail(
        val channelId: String,
        val messageId: String? = null,
        val parentMessageId: String? = null,
    )

    data class Extra(
        val mode: ExtraContentMode,
    )

    companion object {
        val Saver: Saver<MutableState<PaneState>, *> = listSaver(
            save = { state ->
                listOf(
                    state.value.detail?.run {
                        listOf(channelId, messageId, parentMessageId)
                    },
                    state.value.extra?.run {
                        with(ExtraContentMode.Saver) { save(mutableStateOf(mode)) }
                    }
                )
            },
            restore = { state ->
                val (detail, extra) = state
                mutableStateOf(PaneState(
                    detail = (detail as? List<String?>)?.run {
                        Detail(
                            channelId = get(0)!!,
                            messageId = get(1),
                            parentMessageId = get(2)
                        )
                    },
                    extra = (extra as? String)?.run {
                        Extra(
                            mode = ExtraContentMode.Saver.restore(this)!!.value
                        )
                    }
                ))
            }
        )
    }
}

private fun PaneState.update(
    viewModelFactory: MessagesViewModelFactory?,
    extraContentMode: ExtraContentMode,
): PaneState =
    copy(
        detail = viewModelFactory?.let { factory ->
            PaneState.Detail(
                factory.channelId,
                factory.messageId,
                factory.parentMessageId
            )
        },
        extra = if (viewModelFactory == null || extraContentMode is ExtraContentMode.Hidden) {
            null
        } else {
            PaneState.Extra(mode = extraContentMode)
        }
    )

/**
 * Represents a complete screen for chat, including a list of channels, threads, and messages.
 * The layout adapts based on the screen size, showing a single-pane layout on smaller screens
 * and a dual-pane layout on larger screens.
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
 * @param onViewChannelInfoClick Callback invoked when the user long presses a channel and clicks "View Info".
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
    extraContent: @Composable (mode: ExtraContentMode) -> Unit = {},
) {
    val context = LocalContext.current
    val singlePane = AdaptiveLayoutInfo.singlePaneWindow()

    var channelMessagesViewModelFactory by rememberSaveable(
        saver = factorySaver { selection -> messagesViewModelFactoryProvider(context, selection) },
    ) {
        mutableStateOf(messagesViewModelFactoryProvider(context, MessageSelection()))
    }
    val backPressHandler = {
        println("alor: backPressHandler channelMessagesViewModelFactory=$channelMessagesViewModelFactory")
        // Clear the messages view model factory when the user navigates back in single-pane mode
        if (singlePane && channelMessagesViewModelFactory != null) {
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
    if (!singlePane) {
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
    }

    val detailPane = remember {
        movableContentOf { detail: PaneState.Detail ->
            messagesViewModelFactoryProvider(
                context,
                MessageSelection(
                    channelId = detail.channelId,
                    messageId = detail.messageId,
                    parentMessageId = detail.parentMessageId
                )
            )?.let { viewModelFactory ->
                DetailPane(
                    viewModelFactory = viewModelFactory,
                    topBarContent = detailTopBarContent,
                    bottomBarContent = detailBottomBarContent,
                    onBackPress = backPressHandler,
                )
            }
        }
    }

    var paneState by rememberSaveable(saver = PaneState.Saver) { mutableStateOf(PaneState()) }
    LaunchedEffect(channelMessagesViewModelFactory, extraContentMode) {
        println("alor: channelMessagesViewModelFactory=$channelMessagesViewModelFactory extraContentMode=$extraContentMode")
        paneState = paneState.update(channelMessagesViewModelFactory, extraContentMode)
        println("alor: navigation=$paneState")
    }

    if (singlePane) {
        Box(
            modifier = modifier,
        ) {
            AnimatedContent(
                targetState = paneState,
                transitionSpec = {
                    val isNavigatingForward =
                        (targetState.detail != null && initialState.detail == null) ||
                            (targetState.extra != null && initialState.extra == null)
                    slideContentTransform(isNavigatingForward = isNavigatingForward)
                },
            ) { state ->
                if (state.detail == null) {
                    listPane(Modifier)
                }

                if (state.detail != null) {
                    detailPane(state.detail)
                }

                if (state.extra != null) {
                    extraContent(state.extra.mode)
                }
            }
        }
    } else {
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
                Crossfade(
                    modifier = Modifier.weight(1f),
                    targetState = paneState.detail,
                ) { detail ->
                    if (detail != null) {
                        detailPane(detail)
                    }
                }
                val extraPaneOffsetX by animateFloatAsState(
                    targetValue = if (paneState.extra == null) {
                        detailPaneSize.width / 2f
                    } else {
                        0f
                    },
                )
                val extraPaneWeight by animateFloatAsState(
                    targetValue = if (paneState.extra == null) {
                        0f
                    } else {
                        1f
                    },
                )
                if (extraPaneWeight > 0f) {
                    Box(
                        modifier = Modifier.weight(extraPaneWeight),
                    ) {
                        Crossfade(
                            modifier = Modifier.offset { IntOffset(x = extraPaneOffsetX.toInt(), y = 0) },
                            targetState = paneState.extra,
                        ) { extra ->
                            if (extra != null) {
                                Row {
                                    VerticalDivider()
                                    extraContent(extra.mode)
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
            val showBackButton = AdaptiveLayoutInfo.singlePaneWindow()
            if (showBackButton) {
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
 * The content transform used for animating the content when navigating in single-pane mode.
 */
private fun AnimatedContentTransitionScope<*>.slideContentTransform(
    isNavigatingForward: Boolean,
): ContentTransform =
    (
        fadeIn() +
            slideInHorizontally(initialOffsetX = { fullWidth -> if (isNavigatingForward) fullWidth else -fullWidth })
            togetherWith
            slideOutHorizontally(targetOffsetX = { fullWidth -> if (isNavigatingForward) -fullWidth else fullWidth }) +
            fadeOut()
        )
        .using(
            // Disable clipping to allow content to slide out fully
            SizeTransform(clip = false),
        )

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
