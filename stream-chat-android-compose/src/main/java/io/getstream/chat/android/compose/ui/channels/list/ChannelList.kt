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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData

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
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no channels.
 * @param emptySearchContent Composable that represents the empty content if there are no channels matching the search
 * query.
 * @param helperContent Composable that represents the helper content. Empty by default, but can be used to implement
 * scroll to top button.
 * @param loadingMoreContent: Composable that represents the loading more content, when we're loading the next page.
 * @param channelContent Composable that allows the user to completely customize the item UI.
 * It shows [ChannelItem] if left unchanged, with the actions provided by [onChannelClick] and
 * [onChannelLongClick].
 * @param divider Composable that allows the user to define an item divider.
 */
@Composable
public fun ChannelList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ChannelListViewModel = viewModel(
        factory =
        ChannelViewModelFactory(
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
    loadingContent: @Composable () -> Unit = { LoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultChannelListEmptyContent(modifier) },
    emptySearchContent: @Composable (String) -> Unit = { searchQuery ->
        DefaultChannelSearchEmptyContent(
            searchQuery = searchQuery,
            modifier = modifier,
        )
    },
    helperContent: @Composable BoxScope.() -> Unit = {},
    loadingMoreContent: @Composable () -> Unit = { DefaultChannelsLoadingMoreIndicator() },
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit = { itemState ->
        val user by viewModel.user.collectAsState()
        DefaultChannelItem(
            channelItem = itemState,
            currentUser = user,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    },
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit = { itemState ->
        val user by viewModel.user.collectAsState()
        DefaultSearchResultItem(
            searchResultItemState = itemState,
            currentUser = user,
            onSearchResultClick = onSearchResultClick,
        )
    },
    divider: @Composable () -> Unit = { DefaultChannelItemDivider() },
) {
    val user by viewModel.user.collectAsState()

    ChannelList(
        modifier = modifier,
        contentPadding = contentPadding,
        channelsState = viewModel.channelsState,
        currentUser = user,
        lazyListState = lazyListState,
        onLastItemReached = onLastItemReached,
        onChannelClick = onChannelClick,
        onChannelLongClick = onChannelLongClick,
        loadingContent = loadingContent,
        emptyContent = emptyContent,
        emptySearchContent = emptySearchContent,
        helperContent = helperContent,
        loadingMoreContent = loadingMoreContent,
        channelContent = channelContent,
        searchResultContent = searchResultContent,
        divider = divider,
    )
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
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no channels.
 * @param emptySearchContent Composable that represents the empty content if there are no channels matching the search
 * query.
 * @param helperContent Composable that represents the helper content. Empty by default, but can be used to implement
 * scroll to top button.
 * @param loadingMoreContent: Composable that represents the loading more content, when we're loading the next page.
 * @param channelContent Composable that allows the user to completely customize the item UI.
 * It shows [ChannelItem] if left unchanged, with the actions provided by [onChannelClick] and
 * [onChannelLongClick].
 * @param searchResultContent Composable that allows the user to completely customize the search result item UI.
 * It shows [SearchResultItem] if left unchanged, with the actions provided by [onSearchResultClick].
 * @param divider Composable that allows the user to define an item divider.
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
    loadingContent: @Composable () -> Unit = { DefaultChannelListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultChannelListEmptyContent(modifier) },
    emptySearchContent: @Composable (String) -> Unit = { searchQuery ->
        DefaultChannelSearchEmptyContent(
            searchQuery = searchQuery,
            modifier = modifier,
        )
    },
    helperContent: @Composable BoxScope.() -> Unit = {},
    loadingMoreContent: @Composable () -> Unit = { DefaultChannelsLoadingMoreIndicator() },
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit = { itemState ->
        DefaultChannelItem(
            channelItem = itemState,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    },
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit = { itemState ->
        DefaultSearchResultItem(
            searchResultItemState = itemState,
            currentUser = currentUser,
            onSearchResultClick = onSearchResultClick,
        )
    },
    divider: @Composable () -> Unit = { DefaultChannelItemDivider() },
) {
    val (isLoading, _, _, channels, searchQuery) = channelsState

    when {
        channels.isNotEmpty() -> {
            Channels(
                modifier = modifier,
                contentPadding = contentPadding,
                channelsState = channelsState,
                lazyListState = lazyListState,
                onLastItemReached = onLastItemReached,
                helperContent = helperContent,
                loadingMoreContent = loadingMoreContent,
                itemContent = { itemState ->
                    WrapperItemContent(
                        itemState = itemState,
                        channelContent = channelContent,
                        searchResultContent = searchResultContent,
                    )
                },
                divider = divider,
            )
        }
        isLoading -> loadingContent()
        searchQuery.query.isBlank() -> emptyContent()
        else -> emptySearchContent(searchQuery.query)
    }
}

/**
 * The default item.
 *
 * @param itemState The item to represent.
 * @param channelContent Composable that represents the channel item.
 * @param searchResultContent Composable that represents the search result item.
 */
@Composable
internal fun WrapperItemContent(
    itemState: ItemState,
    channelContent: @Composable (ItemState.ChannelItemState) -> Unit,
    searchResultContent: @Composable (ItemState.SearchResultItemState) -> Unit,
) {
    when (itemState) {
        is ItemState.ChannelItemState -> channelContent(itemState)
        is ItemState.SearchResultItemState -> searchResultContent(itemState)
    }
}

/**
 * The default search result item.
 *
 * @param searchResultItemState The item to represent.
 * @param currentUser The currently logged in user.
 * @param onSearchResultClick Handler when the user clicks on an item.
 */
@Composable
internal fun DefaultSearchResultItem(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
    onSearchResultClick: (Message) -> Unit,
) {
    SearchResultItem(
        searchResultItemState = searchResultItemState,
        currentUser = currentUser,
        onSearchResultClick = onSearchResultClick,
    )
}

/**
 * The default channel item.
 *
 * @param channelItem The item to represent.
 * @param currentUser The currently logged in user.
 * @param onChannelClick Handler when the user clicks on an item.
 * @param onChannelLongClick Handler when the user long taps on an item.
 */
@Composable
internal fun DefaultChannelItem(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
) {
    ChannelItem(
        channelItem = channelItem,
        currentUser = currentUser,
        onChannelClick = onChannelClick,
        onChannelLongClick = onChannelLongClick,
    )
}

/**
 * Default loading indicator.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultChannelListLoadingIndicator(modifier: Modifier) {
    LoadingIndicator(modifier)
}

/**
 * The default empty placeholder for the case when there are no channels available to the user.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultChannelListEmptyContent(modifier: Modifier = Modifier) {
    EmptyContent(
        modifier = modifier,
        painter = painterResource(id = R.drawable.stream_compose_empty_channels),
        text = stringResource(R.string.stream_compose_channel_list_empty_channels),
    )
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
        painter = painterResource(id = R.drawable.stream_compose_empty_search_results),
        text = stringResource(R.string.stream_compose_channel_list_empty_search_results, searchQuery),
    )
}

/**
 * Represents the default item divider in channel items.
 */
@Composable
public fun DefaultChannelItemDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(color = ChatTheme.colors.borders),
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
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithMessages,
                    typingUsers = emptyList(),
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithFewMembers,
                    typingUsers = emptyList(),
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithManyMembers,
                    typingUsers = emptyList(),
                ),
                ItemState.ChannelItemState(
                    channel = PreviewChannelData.channelWithOnlineUser,
                    typingUsers = emptyList(),
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
