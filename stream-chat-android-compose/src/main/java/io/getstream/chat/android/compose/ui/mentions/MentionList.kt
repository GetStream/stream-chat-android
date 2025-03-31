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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.getstream.chat.android.compose.ui.mentions

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.LazyPagingColumn
import io.getstream.chat.android.compose.ui.components.PullToRefreshContentListBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.mentions.MentionListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.mentions.MentionListState
import kotlinx.coroutines.flow.collectLatest

@Composable
public fun MentionList(
    viewModel: MentionListViewModel,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onItemClick: ((message: Message) -> Unit)? = null,
    onEvent: (event: Any) -> Unit = {},
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
    loadingIndicator: @Composable BoxScope.(pullToRefreshState: PullToRefreshState, isRefreshing: Boolean) -> Unit =
        { pullToRefreshState, isRefreshing ->
            with(ChatTheme.componentFactory) {
                MentionListLoadingIndicator(
                    modifier = Modifier,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = isRefreshing,
                )
            }
        },
    emptyContent: @Composable BoxScope.() -> Unit = {
        ChatTheme.componentFactory.MentionListEmptyContent(
            modifier = Modifier,
        )
    },
    loadingItemContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingItem(
                modifier = Modifier,
            )
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
        onRefresh = viewModel::refresh,
        itemContent = itemContent,
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
        loadingItemContent = loadingItemContent,
    )
}

@Composable
public fun MentionList(
    state: MentionListState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onItemClick: ((message: Message) -> Unit)? = null,
    onLoadMore: () -> Unit = {},
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
    loadingIndicator: @Composable BoxScope.(pullToRefreshState: PullToRefreshState, isRefreshing: Boolean) -> Unit =
        { pullToRefreshState, isRefreshing ->
            with(ChatTheme.componentFactory) {
                MentionListLoadingIndicator(
                    modifier = Modifier,
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = isRefreshing,
                )
            }
        },
    emptyContent: @Composable BoxScope.() -> Unit = {
        ChatTheme.componentFactory.MentionListEmptyContent(
            modifier = Modifier,
        )
    },
    loadingItemContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MentionListLoadingItem(
                modifier = Modifier,
            )
        }
    },
) {
    PullToRefreshContentListBox(
        modifier = modifier,
        isLoading = state.isLoading,
        items = state.results,
        onRefresh = onRefresh,
        listContent = {
            LazyPagingColumn(
                items = state.results,
                modifier = modifier,
                itemKey = { item -> item.message.identifierHash() },
                showLoadingItem = state.isLoadingMore,
                onLoadMore = onLoadMore,
                itemContent = itemContent,
                loadingItem = loadingItemContent,
            )
        },
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
    )
}

@Preview
@Composable
private fun MentionListLoadingPreview() {
    ChatTheme {
        MentionList(
            state = MentionListState(
                isLoading = true,
                results = emptyList(),
                nextPage = null,
                canLoadMore = true,
                isLoadingMore = false,
            ),
            currentUser = PreviewUserData.user1,
        )
    }
}

@Preview
@Composable
private fun MentionListEmptyPreview() {
    ChatTheme {
        MentionList(
            state = MentionListState(
                isLoading = false,
                results = emptyList(),
                nextPage = null,
                canLoadMore = true,
                isLoadingMore = false,
            ),
            currentUser = PreviewUserData.user1,
        )
    }
}

@Preview
@Composable
private fun MentionListLoadingMorePreview() {
    ChatTheme {
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
                nextPage = null,
                canLoadMore = true,
                isLoadingMore = true,
            ),
            currentUser = PreviewUserData.user1,
        )
    }
}
