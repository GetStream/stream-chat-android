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

package io.getstream.chat.android.compose.ui.channels

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.channels.buildDefaultChannelActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * Default root Channel screen component, that provides the necessary ViewModel.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 * @param viewModelFactory The factory used to build the ViewModels and power the behavior.
 * You can use the default implementation by not passing in an instance yourself, or you
 * can customize the behavior using its parameters.
 * @param viewModelKey Key to differentiate between instances of [ChannelListViewModel].
 * @param title Header title.
 * @param isShowingHeader If we show the header or hide it.
 * @param searchMode The search mode for the screen.
 * @param onHeaderActionClick Handler for the default header action.
 * @param onHeaderAvatarClick Handle for when the user clicks on the header avatar.
 * @param onChannelClick Handler for Channel item clicks.
 * @param onViewChannelInfoAction Handler for when the user selects the [ViewInfo] option for a [Channel].
 * @param onBackPressed Handler for back press action.
 */
@Composable
@Suppress("LongMethod")
public fun ChannelsScreen(
    viewModelFactory: ChannelViewModelFactory = ChannelViewModelFactory(),
    viewModelKey: String? = null,
    title: String = "Stream Chat",
    isShowingHeader: Boolean = true,
    searchMode: SearchMode = SearchMode.None,
    onHeaderActionClick: () -> Unit = {},
    onHeaderAvatarClick: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onSearchMessageItemClick: (Message) -> Unit = {},
    onViewChannelInfoAction: (Channel) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val listViewModel: ChannelListViewModel = viewModel(
        ChannelListViewModel::class.java,
        key = viewModelKey,
        factory = viewModelFactory,
    )

    val selectedChannel by listViewModel.selectedChannel
    val user by listViewModel.user.collectAsState()
    val connectionState by listViewModel.connectionState.collectAsState()

    BackHandler(enabled = true) {
        if (selectedChannel != null) {
            listViewModel.selectChannel(null)
        } else {
            onBackPressed()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf(listViewModel.searchQuery.query) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("Stream_ChannelsScreen"),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (isShowingHeader) {
                    ChatTheme.componentFactory.ChannelListHeader(
                        modifier = Modifier,
                        onHeaderActionClick = onHeaderActionClick,
                        onAvatarClick = { onHeaderAvatarClick() },
                        currentUser = user,
                        title = title,
                        connectionState = connectionState,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(color = ChatTheme.colors.backgroundCoreApp),
            ) {
                if (searchMode != SearchMode.None) {
                    ChatTheme.componentFactory.ChannelListSearchInput(
                        modifier = Modifier
                            .testTag("Stream_SearchInput")
                            .padding(
                                top = StreamTokens.spacingMd,
                                bottom = StreamTokens.spacingXs,
                                start = StreamTokens.spacingMd,
                                end = StreamTokens.spacingMd,
                            )
                            .fillMaxWidth(),
                        query = searchQuery,
                        onSearchStarted = {},
                        onValueChange = remember(listViewModel) {
                            {
                                searchQuery = it
                                listViewModel.setSearchQuery(
                                    when {
                                        it.isBlank() -> SearchQuery.Empty
                                        searchMode == SearchMode.Channels -> SearchQuery.Channels(it)
                                        searchMode == SearchMode.Messages -> SearchQuery.Messages(it)
                                        else -> SearchQuery.Empty
                                    },
                                )
                            }
                        },
                    )
                }

                ChannelList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = listViewModel,
                    onChannelClick = onChannelClick,
                    onSearchResultClick = onSearchMessageItemClick,
                    onChannelLongClick = remember(listViewModel) {
                        {
                            listViewModel.selectChannel(it)
                        }
                    },
                    emptyContent = {
                        ChatTheme.componentFactory.ChannelListEmptyContent(
                            modifier = Modifier.fillMaxSize(),
                            onStartChatClick = onHeaderActionClick,
                        )
                    },
                )
            }
        }

        val isMenuVisible = selectedChannel != null
        val lastChannel = remember { mutableStateOf(Channel()) }
        if (selectedChannel != null) {
            lastChannel.value = selectedChannel!!
        }
        AnimatedVisibility(
            visible = isMenuVisible,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
        ) {
            val channel = lastChannel.value
            val channelActions = buildDefaultChannelActions(
                selectedChannel = channel,
                isMuted = listViewModel.isChannelMuted(channel.cid),
                ownCapabilities = channel.ownCapabilities,
                viewModel = listViewModel,
                onViewInfoAction = { ch ->
                    listViewModel.dismissChannelAction()
                    onViewChannelInfoAction(ch)
                },
            )

            ChatTheme.componentFactory.ChannelMenu(
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
                selectedChannel = channel,
                currentUser = user,
                channelActions = channelActions,
                onChannelOptionClick = remember(listViewModel) {
                    {
                            action ->
                        listViewModel.executeOrConfirm(action)
                    }
                },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }

        val activeAction = listViewModel.activeChannelAction
        val popup = activeAction?.confirmationPopup

        if (popup != null) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = popup.title,
                message = popup.message,
                onPositiveAction = remember(listViewModel) { { listViewModel.confirmPendingAction() } },
                onDismiss = remember(listViewModel) { { listViewModel.dismissChannelAction() } },
            )
        }
    }
}

/**
 * The types of search modes in the channel screen.
 */
public enum class SearchMode {
    /**
     * No search mode.
     */
    None,

    /**
     * Search for channels.
     */
    Channels,

    /**
     * Search for messages.
     */
    Messages,
}
