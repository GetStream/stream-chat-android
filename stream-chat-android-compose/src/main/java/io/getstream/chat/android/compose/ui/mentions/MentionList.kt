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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.getstream.chat.android.compose.ui.mentions

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.LazyPagingColumn
import io.getstream.chat.android.compose.ui.components.PullToRefreshBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.mentions.MentionListViewModel
import io.getstream.chat.android.compose.viewmodel.mentions.MentionListViewModelFactory
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.mentions.MentionListEvent
import io.getstream.chat.android.ui.common.state.mentions.MentionListState
import kotlinx.coroutines.flow.collectLatest

/**
 * The default stateful component that is bound to [MentionListViewModel]
 * to display a list of mentions for the current user.
 *
 * @see [MentionListViewModelFactory]
 *
 * @param viewModel The [MentionListViewModel] instance to use.
 * @param modifier The modifier to apply to this layout.
 * @param currentUser The current user to use for the mentions.
 * @param onItemClick The callback to be called when an item is clicked.
 * @param onEvent The callback to be called when an [MentionListEvent] is received from the [viewModel].
 * @param pullToRefreshEnabled If true, the pull-to-refresh functionality is enabled. Defaults to true.
 * @param itemContent The content displayed by a single item.
 * @param loadingIndicator The content displayed during the initial loading.
 * @param emptyContent The content displayed when the list is empty.
 * @param loadingItemContent The content displayed when loading more items.
 * @param pullToRefreshIndicator The custom indicator to be displayed during the pull-to-refresh action.
 */
@Composable
public fun MentionList(
    viewModel: MentionListViewModel,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onItemClick: ((message: Message) -> Unit)? = null,
    onEvent: (event: Any) -> Unit = {},
    pullToRefreshEnabled: Boolean = true,
    itemContent: @Composable LazyItemScope.(MessageResult) -> Unit = { mention ->
        with(ChatTheme.componentFactory) {
            MentionListItem(
                mention = mention,
                modifier = Modifier,
                currentUser = currentUser,
                onClick = onItemClick,
            )
        }
    },
    loadingIndicator: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingIndicator(
                modifier = Modifier,
            )
        }
    },
    emptyContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListEmptyContent(
                modifier = Modifier,
            )
        }
    },
    loadingItemContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingItem(
                modifier = Modifier,
            )
        }
    },
    pullToRefreshIndicator: @Composable BoxScope.(
        pullToRefreshState: PullToRefreshState,
        isRefreshing: Boolean,
    ) -> Unit = { pullToRefreshState, isRefreshing ->
        if (pullToRefreshEnabled) {
            with(ChatTheme.componentFactory) {
                MentionListPullToRefreshIndicator(
                    modifier = Modifier,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = isRefreshing,
                )
            }
        }
    },
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest(onEvent)
    }
    MentionList(
        state = state,
        modifier = modifier,
        currentUser = currentUser,
        onItemClick = onItemClick,
        onLoadMore = viewModel::loadMore,
        pullToRefreshEnabled = pullToRefreshEnabled,
        onRefresh = viewModel::refresh,
        itemContent = itemContent,
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
        loadingItemContent = loadingItemContent,
        pullToRefreshIndicator = pullToRefreshIndicator,
    )
}

/**
 * The default stateless component that displays a list of mentions for the current user.
 *
 * *This component is useful when you want to manage the state of the list yourself.*
 *
 * @param state The state of the list to display.
 * @param modifier The modifier to apply to this layout.
 * @param currentUser The current user to use for the mentions.
 * @param onItemClick The callback to be called when an item is clicked.
 * @param onLoadMore The callback to be called when more items should be loaded.
 * @param pullToRefreshEnabled If true, the pull-to-refresh functionality is enabled. Defaults to true.
 * @param onRefresh The callback to be invoked when the user performs a pull-to-refresh action.
 * @param itemContent The content displayed by a single item.
 * @param loadingIndicator The content displayed during the initial loading.
 * @param emptyContent The content displayed when the list is empty.
 * @param loadingItemContent The content displayed when loading more items.
 * @param pullToRefreshIndicator The custom indicator to be displayed during the pull-to-refresh action.
 */
@Composable
public fun MentionList(
    state: MentionListState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onItemClick: ((message: Message) -> Unit)? = null,
    onLoadMore: () -> Unit = {},
    pullToRefreshEnabled: Boolean = true,
    onRefresh: () -> Unit = {},
    itemContent: @Composable LazyItemScope.(MessageResult) -> Unit = { mention ->
        with(ChatTheme.componentFactory) {
            MentionListItem(
                mention = mention,
                modifier = Modifier,
                currentUser = currentUser,
                onClick = onItemClick,
            )
        }
    },
    loadingIndicator: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingIndicator(
                modifier = Modifier,
            )
        }
    },
    emptyContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListEmptyContent(
                modifier = Modifier,
            )
        }
    },
    loadingItemContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingItem(
                modifier = Modifier,
            )
        }
    },
    pullToRefreshIndicator: @Composable BoxScope.(
        pullToRefreshState: PullToRefreshState,
        isRefreshing: Boolean,
    ) -> Unit = { pullToRefreshState, isRefreshing ->
        if (pullToRefreshEnabled) {
            with(ChatTheme.componentFactory) {
                MentionListPullToRefreshIndicator(
                    modifier = Modifier,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = isRefreshing,
                )
            }
        }
    },
) {
    ContentBox(
        isLoading = state.isLoading,
        isEmpty = !state.isRefreshing && state.results.isEmpty(),
        modifier = modifier,
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
            state = pullToRefreshState,
            enabled = pullToRefreshEnabled,
            indicator = { pullToRefreshIndicator(pullToRefreshState, state.isRefreshing) },
        ) {
            LazyPagingColumn(
                items = state.results,
                modifier = modifier,
                itemKey = { _, item -> item.message.identifierHash() },
                showLoadingItem = state.isLoadingMore,
                onLoadMore = onLoadMore,
                itemContent = { _, item -> itemContent(item) },
                loadingItem = loadingItemContent,
            )
        }
    }
}

@Composable
internal fun MentionListLoading() {
    MentionList(
        state = MentionListState(),
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
    )
}

@Preview(showBackground = true)
@Composable
private fun MentionListLoadingLightPreview() {
    ChatTheme {
        MentionListLoading()
    }
}

@Composable
internal fun MentionListEmpty() {
    MentionList(
        state = MentionListState(isLoading = false),
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
    )
}

@Preview(showBackground = true)
@Composable
private fun MentionListEmptyLightPreview() {
    ChatTheme {
        MentionListEmpty()
    }
}

@Composable
internal fun MentionListLoaded() {
    MentionList(
        state = MentionListState(
            isLoading = false,
            results = listOf(
                MessageResult(
                    message = PreviewMessageData.message1,
                    channel = PreviewChannelData.channelWithImage,
                ),
                MessageResult(
                    message = PreviewMessageData.message2,
                    channel = PreviewChannelData.channelWithFewMembers,
                ),
                MessageResult(
                    message = PreviewMessageData.message3,
                    channel = PreviewChannelData.channelWithManyMembers,
                ),
            ),
        ),
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
    )
}

@Preview(showBackground = true)
@Composable
private fun MentionListLoadedLightPreview() {
    ChatTheme {
        MentionListLoaded()
    }
}

@Composable
internal fun MentionListLoadingMore() {
    MentionList(
        state = MentionListState(
            isLoading = false,
            results = listOf(
                MessageResult(
                    message = PreviewMessageData.message1,
                    channel = PreviewChannelData.channelWithImage,
                ),
                MessageResult(
                    message = PreviewMessageData.message2,
                    channel = PreviewChannelData.channelWithFewMembers,
                ),
                MessageResult(
                    message = PreviewMessageData.message3,
                    channel = PreviewChannelData.channelWithManyMembers,
                ),
            ),
            isLoadingMore = true,
        ),
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
    )
}

@Preview(showBackground = true)
@Composable
private fun MentionListLoadingMoreLightPreview() {
    ChatTheme {
        MentionListLoadingMore()
    }
}
