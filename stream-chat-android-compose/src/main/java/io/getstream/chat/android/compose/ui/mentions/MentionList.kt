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

package io.getstream.chat.android.compose.ui.mentions

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.channels.list.SearchResultItem
import io.getstream.chat.android.compose.ui.components.LoadingFooter
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
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
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest(onEvent)
    }
    MentionList(
        state = state,
        currentUser = currentUser,
        modifier = modifier,
        onItemClick = onItemClick,
        onLoadMore = viewModel::loadMore,
    )
}

@Composable
public fun MentionList(
    state: MentionListState,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onItemClick: ((message: Message) -> Unit)? = null,
    onLoadMore: () -> Unit = {},
) {
    Crossfade(
        targetState = state.isLoading,
        modifier = modifier,
    ) { isLoading ->
        if (isLoading) {
            LoadingIndicator(
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = modifier,
                state = lazyListState,
            ) {
                items(
                    items = state.results,
                    key = { item -> item.message.identifierHash() },
                ) { mention ->
                    MentionListItem(
                        mention = mention,
                        currentUser = currentUser,
                        onClick = onItemClick,
                    )
                }
                if (state.isLoadingMore) {
                    item {
                        LoadingFooter(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            LoadMoreHandler(
                listState = lazyListState,
                loadMore = onLoadMore,
            )
        }
    }
}

@Composable
public fun MentionListItem(
    mention: MessageResult,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    onClick: ((message: Message) -> Unit)? = null,
) {
    SearchResultItem(
        searchResultItemState = remember {
            ItemState.SearchResultItemState(
                message = mention.message,
                channel = mention.channel,
            )
        },
        currentUser = currentUser,
        modifier = modifier,
        onSearchResultClick = onClick,
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
