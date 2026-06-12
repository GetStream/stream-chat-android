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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChannelListBannerParams
import io.getstream.chat.android.compose.ui.theme.ChannelListDividerItemParams
import io.getstream.chat.android.compose.ui.theme.ChannelListEmptyContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListEmptySearchContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListHelperContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListItemContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListLoadingIndicatorParams
import io.getstream.chat.android.compose.ui.theme.ChannelListLoadingMoreItemContentParams
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.SearchResultItemContentParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.ArchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnarchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import kotlinx.coroutines.launch

/**
 * Default ChannelList component, that relies on the [ChannelListViewModel] to load the data and
 * show it on the UI.
 *
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the channel list surrounding the content inside.
 * @param viewModel The ViewModel that loads all the data and connects it to the UI. We provide a
 * factory that builds the default ViewModel in case the user doesn't want to provide their own.
 * @param lazyListState State of the lazy list that represents the list of channels. Useful for controlling the
 * scroll state.
 * @param onLastItemReached Handler for pagination, when the user reaches the last item in the list.
 * @param onChannelClick Handler for a single item tap.
 * @param onChannelLongClick Handler for a long item tap.
 * @param onSearchResultClick Handler for a single search result tap.
 * @param onLoadingErrorClick Handler for the "Tap to retry" banner shown when loading the next page fails.
 * Defaults to retrying the failed load.
 * @param onStartChatClick Handler for the "Start a chat" button in the empty state. If null, the button is hidden.
 */
@Composable
public fun ChannelList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ChannelListViewModel = viewModel(
        factory =
        ChannelListViewModelFactory(
            ChatClient.instance(),
            QuerySortByField.descByName("last_updated"),
            filters = null,
        ),
    ),
    lazyListState: LazyListState = rememberLazyListState(),
    onLastItemReached: () -> Unit = remember(viewModel) { { viewModel.loadMore() } },
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = remember(viewModel) { { viewModel.selectChannel(it) } },
    onSearchResultClick: (Message) -> Unit = {},
    onLoadingErrorClick: () -> Unit = remember(viewModel) { { viewModel.loadMore() } },
    onStartChatClick: (() -> Unit)? = null,
) {
    val user by viewModel.user.collectAsState()
    val selectedCid = viewModel.selectedChannel.value?.cid
    val channelsState = viewModel.channelsState
    val enrichedState = remember(channelsState, selectedCid) { channelsState.withSelectedChannel(selectedCid) }

    val scope = rememberCoroutineScope()
    val swipeCoordinator = remember { SwipeRevealCoordinator() }
    val swipeActionHandler: (ChannelAction) -> Unit = remember(viewModel) {
        {
                action ->
            scope.launch { swipeCoordinator.closeAll() }
            when (action) {
                is MuteChannel -> viewModel.muteChannel(action.channel)
                is UnmuteChannel -> viewModel.unmuteChannel(action.channel)
                is PinChannel -> viewModel.pinChannel(action.channel)
                is UnpinChannel -> viewModel.unpinChannel(action.channel)
                is ArchiveChannel -> viewModel.archiveChannel(action.channel)
                is UnarchiveChannel -> viewModel.unarchiveChannel(action.channel)
                else -> viewModel.executeOrConfirm(action)
            }
        }
    }
    val moreClickHandler: (Channel) -> Unit = remember(viewModel) {
        {
                channel ->
            scope.launch { swipeCoordinator.closeAll() }
            viewModel.selectChannel(channel)
        }
    }

    CompositionLocalProvider(
        LocalSwipeRevealCoordinator provides swipeCoordinator,
        LocalSwipeActionHandler provides swipeActionHandler,
        LocalChannelMoreClickHandler provides moreClickHandler,
    ) {
        ChannelList(
            modifier = modifier,
            contentPadding = contentPadding,
            channelsState = enrichedState,
            currentUser = user,
            lazyListState = lazyListState,
            onLastItemReached = onLastItemReached,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
            onSearchResultClick = onSearchResultClick,
            onLoadingErrorClick = onLoadingErrorClick,
            onStartChatClick = onStartChatClick,
        )
    }
}

/**
 * Root Channel list component, that represents different UI, based on the current channel state.
 *
 * This is decoupled from ViewModels, so the user can provide manual and custom data handling,
 * as well as define a completely custom UI component for the channel item.
 *
 * If there is no state, no query active or the data is being loaded, we show the [LoadingIndicator].
 *
 * If there are no results or we're offline, usually due to an error in the API or network, we show an [EmptyContent].
 *
 * If there is data available and it is not empty, we show [Channels].
 *
 * @param channelsState Current state of the Channel list, represented by [ChannelsState].
 * @param currentUser The data of the current user, used various states.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the list surrounding the content inside.
 * @param lazyListState State of the lazy list that represents the list of channels. Useful for controlling the
 * scroll state.
 * @param onLastItemReached Handler for pagination, when the user reaches the end of the list.
 * @param onChannelClick Handler for a single item tap.
 * @param onChannelLongClick Handler for a long item tap.
 * @param onSearchResultClick Handler for a single search result tap.
 * @param onLoadingErrorClick Handler for the "Tap to retry" banner shown when loading the next page fails.
 * @param onStartChatClick Handler for the "Start a chat" button in the empty state. If null, the button is hidden.
 */
@Composable
public fun ChannelList(
    channelsState: ChannelsState,
    currentUser: User?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    lazyListState: LazyListState = rememberLazyListState(),
    onLastItemReached: () -> Unit = {},
    onChannelClick: (Channel) -> Unit = {},
    onChannelLongClick: (Channel) -> Unit = {},
    onSearchResultClick: (Message) -> Unit = {},
    onLoadingErrorClick: () -> Unit = {},
    onStartChatClick: (() -> Unit)? = null,
) {
    val (isLoading, _, _, channels, searchQuery) = channelsState

    when {
        channels.isNotEmpty() -> {
            Column(modifier = modifier) {
                if (channelsState.loadingError) {
                    with(ChatTheme.componentFactory) {
                        ChannelListBanner(params = ChannelListBannerParams(onClick = onLoadingErrorClick))
                    }
                }
                Channels(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    channelsState = channelsState,
                    lazyListState = lazyListState,
                    onLastItemReached = onLastItemReached,
                    helperContent = {
                        with(ChatTheme.componentFactory) {
                            ChannelListHelperContent(params = ChannelListHelperContentParams())
                        }
                    },
                    loadingMoreContent = {
                        with(ChatTheme.componentFactory) {
                            ChannelListLoadingMoreItemContent(params = ChannelListLoadingMoreItemContentParams())
                        }
                    },
                    itemContent = { itemState ->
                        WrapperItemContent(
                            itemState = itemState,
                            currentUser = currentUser,
                            onChannelClick = onChannelClick,
                            onChannelLongClick = onChannelLongClick,
                            onSearchResultClick = onSearchResultClick,
                        )
                    },
                    divider = {
                        with(ChatTheme.componentFactory) {
                            ChannelListDividerItem(params = ChannelListDividerItemParams())
                        }
                    },
                )
            }
        }

        isLoading -> ChatTheme.componentFactory.ChannelListLoadingIndicator(
            params = ChannelListLoadingIndicatorParams(modifier = modifier),
        )

        searchQuery.query.isBlank() -> ChatTheme.componentFactory.ChannelListEmptyContent(
            params = ChannelListEmptyContentParams(modifier = modifier, onStartChatClick = onStartChatClick),
        )

        else -> ChatTheme.componentFactory.ChannelListEmptySearchContent(
            params = ChannelListEmptySearchContentParams(searchQuery = searchQuery.query, modifier = modifier),
        )
    }
}

/**
 * The default item content that dispatches between channel items and search result items.
 *
 * @param itemState The item to represent.
 * @param currentUser The currently logged in user.
 * @param onChannelClick Handler for a single channel item tap.
 * @param onChannelLongClick Handler for a long channel item tap.
 * @param onSearchResultClick Handler for a single search result tap.
 */
@Composable
internal fun LazyItemScope.WrapperItemContent(
    itemState: ItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    onSearchResultClick: (Message) -> Unit,
) {
    when (itemState) {
        is ItemState.ChannelItemState -> with(ChatTheme.componentFactory) {
            ChannelListItemContent(
                params = ChannelListItemContentParams(
                    channelItem = itemState,
                    currentUser = currentUser,
                    onChannelClick = onChannelClick,
                    onChannelLongClick = onChannelLongClick,
                ),
            )
        }

        is ItemState.SearchResultItemState -> with(ChatTheme.componentFactory) {
            SearchResultItemContent(
                params = SearchResultItemContentParams(
                    searchResultItem = itemState,
                    currentUser = currentUser,
                    onSearchResultClick = onSearchResultClick,
                ),
            )
        }
    }
}

/**
 * Returns a copy of this [ChannelsState] with the channel matching [selectedCid] marked as selected.
 */
private fun ChannelsState.withSelectedChannel(selectedCid: String?): ChannelsState {
    if (selectedCid == null) return this
    return copy(
        channelItems = channelItems.map { item ->
            if (item is ItemState.ChannelItemState) {
                item.copy(isSelected = item.channel.cid == selectedCid)
            } else {
                item
            }
        },
    )
}

/**
 * Default loading indicator showing skeleton shimmer items.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultChannelListLoadingIndicator(modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .testTag("Stream_ChannelListLoading")
            .background(ChatTheme.colors.backgroundCoreApp),
        userScrollEnabled = false,
    ) {
        items(count = 8) { ChannelListLoadingItem() }
    }
}

/**
 * The default empty placeholder for the case when there are no channels available to the user.
 *
 * @param modifier Modifier for styling.
 * @param onStartChatClick Optional callback for the "Start a chat" button. If null, the button is hidden.
 */
@Composable
internal fun DefaultChannelListEmptyContent(
    modifier: Modifier = Modifier,
    onStartChatClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.background(color = ChatTheme.colors.backgroundCoreApp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_message_bubbles),
            contentDescription = null,
            tint = ChatTheme.colors.textTertiary,
            modifier = Modifier.size(StreamTokens.spacing2xl),
        )
        Spacer(Modifier.size(StreamTokens.spacingXs))
        Text(
            text = stringResource(R.string.stream_compose_channel_list_empty_channels),
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (onStartChatClick != null) {
            Spacer(Modifier.size(StreamTokens.spacingMd))
            StreamTextButton(
                onClick = onStartChatClick,
                text = stringResource(R.string.stream_compose_channel_list_start_chat),
                style = StreamButtonStyleDefaults.secondaryOutline,
            )
        }
    }
}

/**
 * The default empty placeholder for the case when channel search returns no results.
 *
 * @param searchQuery The search query that returned no results.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultChannelSearchEmptyContent(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    EmptyContent(
        modifier = modifier,
        painter = painterResource(id = R.drawable.stream_design_ic_message_bubbles),
        text = stringResource(R.string.stream_compose_channel_list_empty_search_results, searchQuery),
    )
}

/**
 * Preview of [ChannelItem] for a list of channels.
 *
 * Should show a list of channels.
 */
@Preview(showBackground = true, name = "ChannelList Preview (Content state)")
@Composable
private fun ChannelListForContentStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = false,
            channelItems = listOf(
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithImage,
                    typingUsers = emptyList(),
                    draftMessage = null,
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithMessages,
                    typingUsers = emptyList(),
                    draftMessage = null,
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithFewMembers,
                    typingUsers = emptyList(),
                    draftMessage = null,
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithManyMembers,
                    typingUsers = emptyList(),
                    draftMessage = null,
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithOnlineUser,
                    typingUsers = emptyList(),
                    draftMessage = null,
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithOneUser,
                    typingUsers = emptyList(),
                    draftMessage = PreviewMessageData.draftMessage,
                ),
            ),
        ),
    )
}

/**
 * Preview of [ChannelItem] for an empty state.
 *
 * Should show an empty placeholder.
 */
@Preview(showBackground = true, name = "ChannelList Preview (Empty state)")
@Composable
private fun ChannelListForEmptyStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = false,
            channelItems = emptyList(),
        ),
    )
}

/**
 * Preview of [ChannelItem] for a loading state.
 *
 * Should show a progress indicator.
 */
@Preview(showBackground = true, name = "ChannelList Preview (Loading state)")
@Composable
private fun ChannelListForLoadingStatePreview() {
    ChannelListPreview(
        ChannelsState(
            isLoading = true,
        ),
    )
}

/**
 * Shows [ChannelList] preview for the provided parameters.
 *
 * @param channelsState The current state of the component.
 */
@Composable
private fun ChannelListPreview(channelsState: ChannelsState) {
    ChatPreviewTheme {
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            channelsState = channelsState,
            currentUser = PreviewUserData.user1,
        )
    }
}
