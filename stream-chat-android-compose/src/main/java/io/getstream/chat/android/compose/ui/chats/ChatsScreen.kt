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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Default Chat screen component, that provides adaptive layout based on the screen size.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param channelViewModelFactory The factory used to build the [ChannelListViewModel] and power the behavior.
 * You can use the default implementation by not passing in an instance yourself, or you
 * can customize the behavior using its parameters.
 * @param channelViewModelKey Key to differentiate between instances of [ChannelListViewModel].
 * @param title The header title.
 * @param isShowingHeader If a header should be shown.
 * @param searchMode The search mode for the screen.
 * @param onChannelsHeaderAvatarClick Called when the user clicks on the channels header avatar.
 * @param onChannelsHeaderActionClick Called when the user clicks on the channels header action.
 * @param onViewChannelInfoAction Called when the user clicks on the view channel info action.
 * @param onMessagesHeaderTitleClick Called when the user clicks on the messages header title.
 * @param onMessagesHeaderAvatarClick Called when the user clicks on the messages header avatar.
 * @param onNavigateToMessages Called when the user navigates towards a channel or navigates back from a channel.
 * The `channelId` is `null` when user navigates back from a channel.
 * @param onBackPressed Handler for back press action.
 */
@ExperimentalStreamChatApi
@Suppress("LongMethod")
@Composable
public fun ChatsScreen(
    modifier: Modifier = Modifier,
    channelViewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    channelViewModelKey: String? = null,
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
    val singlePanel = singlePanel()
    var clickedChannelId by rememberSaveable { mutableStateOf<String?>(null) }
    val messagesViewModelFactory = remember(clickedChannelId) {
        clickedChannelId?.let {
            MessagesViewModelFactory(
                context = context,
                channelId = it,
            )
        }
    }
    val channelListViewModel =
        viewModel(ChannelListViewModel::class.java, key = channelViewModelKey, factory = channelViewModelFactory)
    val channelItems = channelListViewModel.channelsState.channelItems
    val onSinglePanelBackPressed: () -> Unit = {
        if (clickedChannelId != null) {
            clickedChannelId = null
        } else {
            onBackPressed()
        }
    }

    LaunchedEffect(channelItems) {
        // Auto-select the first channel in the list when it loads on large screens
        if (!singlePanel && channelItems.isNotEmpty() && clickedChannelId == null) {
            clickedChannelId = channelItems.first().key
        }
    }
    LaunchedEffect(clickedChannelId) {
        onNavigateToMessages(clickedChannelId, singlePanel)
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
                        clickedChannelId = channel.cid
                    },
                    onSearchMessageItemClick = { channel ->
                        clickedChannelId = channel.cid
                    },
                    onViewChannelInfoAction = onViewChannelInfoAction,
                    onBackPressed = onSinglePanelBackPressed,
                )
                AnimatedContent(
                    targetState = messagesViewModelFactory,
                    transitionSpec = slideTransitionSpec(),
                ) { factory ->
                    if (factory != null) {
                        Messages(
                            viewModelFactory = factory,
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
                                clickedChannelId = channel.cid
                            },
                            onSearchMessageItemClick = { channel ->
                                clickedChannelId = channel.cid
                            },
                            onViewChannelInfoAction = onViewChannelInfoAction,
                            onBackPressed = onBackPressed,
                        )
                    }
                    VerticalDivider()
                    Box(
                        modifier = Modifier.weight(DetailsPanelWeight),
                    ) {
                        Crossfade(targetState = messagesViewModelFactory) { factory ->
                            if (factory != null) {
                                Messages(
                                    viewModelFactory = factory,
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
