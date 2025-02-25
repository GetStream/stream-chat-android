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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

/**
 * A composable function that displays the chat screen, including a list of channels and messages.
 * The layout adapts based on the screen size, showing a single-panel layout on smaller screens
 * and a dual-panel layout on larger screens.
 *
 * @param modifier The modifier to be applied to the root layout of the screen.
 * @param channelViewModelFactory Factory for creating the [ChannelListViewModel] used for managing channel data.
 * @param channelViewModelKey An optional key to scope the [ChannelListViewModel] instance.
 * @param messagesViewModelFactory A lambda function that provides a [MessagesViewModelFactory] for managing messages
 * within a selected channel.
 * The selected channel ID is `null` when the initial [MessagesViewModelFactory] is requested.
 * @param title The title displayed in the chat screen header. Default is `"Stream Chat"`.
 * @param isShowingHeader Whether to display the header in the channel list. Default is `true`.
 * @param searchMode The current search mode for the chat screen. Default is [SearchMode.None].
 * @param onChannelsHeaderAvatarClick Callback invoked when the user clicks on the avatar in the channel header.
 * @param onChannelsHeaderActionClick Callback invoked when the user clicks on the action button in the channel header.
 * @param onViewChannelInfoAction Callback invoked when the user selects a channel to view its details.
 * @param onMessagesHeaderTitleClick Callback invoked when the user clicks the title in the message header.
 * @param onMessagesHeaderAvatarClick Callback invoked when the user clicks on a user's avatar in a message.
 * @param onNavigateToMessages Callback invoked when navigating to the messages screen, passing the selected channel ID.
 * The selected channel ID is `null` when user navigates back from a channel.
 * @param onBackPressed Callback invoked when the user presses the back button.
 */
@ExperimentalStreamChatApi
@Suppress("LongMethod")
@Composable
public fun ChatsScreen(
    modifier: Modifier = Modifier,
    channelViewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    channelViewModelKey: String? = null,
    messagesViewModelFactory: (
        context: Context,
        channelId: String?,
        messageId: String?,
        parentMessageId: String?,
    ) -> MessagesViewModelFactory? = { context, channelId, messageId, parentMessageId ->
        if (channelId == null) {
            null
        } else {
            MessagesViewModelFactory(
                context = context,
                channelId = channelId,
                messageId = messageId,
                parentMessageId = parentMessageId,
            )
        }
    },
    title: String = "Stream Chat",
    isShowingHeader: Boolean = true,
    searchMode: SearchMode = SearchMode.None,
    onChannelsHeaderAvatarClick: () -> Unit = {},
    onChannelsHeaderActionClick: () -> Unit = {},
    onViewChannelInfoAction: (channel: Channel) -> Unit = {},
    onMessagesHeaderTitleClick: (channel: Channel) -> Unit = {},
    onMessagesHeaderAvatarClick: (user: User) -> Unit = {},
    onNavigateToMessages: (channelId: String?, singlePanel: Boolean) -> Unit = { _, _ -> },
    onBackPressed: () -> Unit = {},
) {
    val context = LocalContext.current

    var channelMessagesViewModelFactory by rememberSaveable(
        saver = factorySaver { channelId, messageId, parentMessageId ->
            messagesViewModelFactory(context, channelId, messageId, parentMessageId)
        }
    ) {
        mutableStateOf(messagesViewModelFactory(context, null, null, null))
    }
    val onSinglePanelBackPressed: () -> Unit = {
        if (channelMessagesViewModelFactory != null) {
            channelMessagesViewModelFactory = null
        } else {
            onBackPressed()
        }
    }

    val singlePanel = singlePanel()
    val channelListViewModel =
        viewModel(ChannelListViewModel::class.java, key = channelViewModelKey, factory = channelViewModelFactory)
    val channelItems = channelListViewModel.channelsState.channelItems
    LaunchedEffect(channelItems) {
        // Auto-select the first channel in the list when it loads on large screens
        if (!singlePanel && channelItems.isNotEmpty() && channelMessagesViewModelFactory == null) {
            val channelId = channelItems.first().key
            channelMessagesViewModelFactory = messagesViewModelFactory(context, channelId, null, null)
        }
    }
    LaunchedEffect(channelMessagesViewModelFactory) {
        onNavigateToMessages(channelMessagesViewModelFactory?.channelId, singlePanel)
    }

    Scaffold(
        modifier = modifier,
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            if (singlePanel) {
                ChannelsScreen(
                    title = title,
                    viewModelFactory = channelViewModelFactory,
                    viewModelKey = channelViewModelKey,
                    isShowingHeader = isShowingHeader,
                    searchMode = searchMode,
                    onHeaderAvatarClick = onChannelsHeaderAvatarClick,
                    onHeaderActionClick = onChannelsHeaderActionClick,
                    onChannelClick = { channel ->
                        channelMessagesViewModelFactory = messagesViewModelFactory(
                            context,
                            channel.cid,
                            null,
                            null,
                        )
                    },
                    onSearchMessageItemClick = { message ->
                        channelMessagesViewModelFactory = messagesViewModelFactory(
                            context,
                            message.cid,
                            message.id,
                            message.parentId,
                        )
                    },
                    onViewChannelInfoAction = onViewChannelInfoAction,
                    onBackPressed = onSinglePanelBackPressed,
                )
                AnimatedContent(
                    targetState = channelMessagesViewModelFactory,
                    transitionSpec = slideTransitionSpec(),
                ) { viewModelFactory ->
                    if (viewModelFactory != null) {
                        Messages(
                            viewModelFactory = viewModelFactory,
                            onHeaderTitleClick = onMessagesHeaderTitleClick,
                            onUserAvatarClick = onMessagesHeaderAvatarClick,
                            onBackPressed = onSinglePanelBackPressed,
                        )
                    }
                }
            } else {
                Row {
                    Box(
                        modifier = Modifier.weight(ListPanelWeight),
                    ) {
                        ChannelsScreen(
                            viewModelFactory = channelViewModelFactory,
                            viewModelKey = channelViewModelKey,
                            title = title,
                            isShowingHeader = isShowingHeader,
                            searchMode = searchMode,
                            onHeaderAvatarClick = onChannelsHeaderAvatarClick,
                            onHeaderActionClick = onChannelsHeaderActionClick,
                            onChannelClick = { channel ->
                                channelMessagesViewModelFactory = messagesViewModelFactory(
                                    context,
                                    channel.cid,
                                    null,
                                    null,
                                )
                            },
                            onSearchMessageItemClick = { message ->
                                channelMessagesViewModelFactory = messagesViewModelFactory(
                                    context,
                                    message.cid,
                                    message.id,
                                    message.parentId,
                                )
                            },
                            onViewChannelInfoAction = onViewChannelInfoAction,
                            onBackPressed = onBackPressed,
                        )
                    }
                    VerticalDivider()
                    Box(
                        modifier = Modifier.weight(DetailsPanelWeight),
                    ) {
                        Crossfade(targetState = channelMessagesViewModelFactory) { viewModelFactory ->
                            if (viewModelFactory != null) {
                                Messages(
                                    viewModelFactory = viewModelFactory,
                                    onHeaderTitleClick = onMessagesHeaderTitleClick,
                                    onUserAvatarClick = onMessagesHeaderAvatarClick,
                                    onBackPressed = onBackPressed,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ListPanelWeight = 0.3f
private const val DetailsPanelWeight = 0.7f

/**
 * @see <a href=https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes>
 *     Use window size classes</a>
 */
@Composable
private fun singlePanel(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED
}

@Composable
private fun Messages(
    viewModelFactory: MessagesViewModelFactory,
    onHeaderTitleClick: (channel: Channel) -> Unit,
    onUserAvatarClick: (user: User) -> Unit,
    onBackPressed: () -> Unit,
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
                onHeaderTitleClick = onHeaderTitleClick,
                onUserAvatarClick = onUserAvatarClick,
                onBackPressed = onBackPressed,
            )
        }
    }
}

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

/**
 * This [Saver] is used to save and restore the state of the [MessagesViewModelFactory]
 * across configuration changes and process death.
 * It saves the channel ID, message ID, and parent message ID of the factory.
 */
private fun factorySaver(
    messagesViewModelFactory: (
        channelId: String?,
        messageId: String?,
        parentMessageId: String?,
    ) -> MessagesViewModelFactory?,
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
                messagesViewModelFactory(
                    channelId,
                    messageId,
                    parentMessageId,
                ),
            )
        }
    },
)
