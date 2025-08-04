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

package io.getstream.chat.android.compose.ui.channel.attachments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState

/**
 * Displays the channel files attachments list.
 *
 * @param viewState The state of the channel attachments view.
 * @param modifier The modifier for styling.
 * @param listState The state of the lazy list.
 * @param currentUser The currently logged in user.
 * @param stickHeader Whether the header should stick to the top of the list when scrolling.
 * @param headerKeySelector The function to select the group key for each item and group them in the list.
 * @param onLoadMoreRequested The callback to be invoked when more items need to be loaded.
 * @param itemContent The composable to display each item in the list.
 * @param headerItem The composable to display as the header for each group of items.
 * @param loadingIndicator The composable to display when the list is loading.
 * @param emptyContent The composable to display when the list is empty.
 * @param errorContent The composable to display when there is an error.
 * @param itemDivider The composable to display as a divider between items.
 * @param loadingItem The composable to display when more items are being loaded.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ChannelFilesAttachmentsList(
    viewState: ChannelAttachmentsViewState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    stickHeader: Boolean = true,
    headerKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String,
    onLoadMoreRequested: () -> Unit = {},
    itemContent: @Composable LazyItemScope.(index: Int, item: ChannelAttachmentsViewState.Content.Item) -> Unit =
        { index, item ->
            val previewHandlers = ChatTheme.attachmentPreviewHandlers
            with(ChatTheme.componentFactory) {
                ChannelFilesAttachmentsItem(
                    modifier = Modifier,
                    index = index,
                    item = item,
                    currentUser = currentUser,
                    onClick = {
                        onFileAttachmentContentItemClick(
                            previewHandlers = previewHandlers,
                            attachment = item.attachment,
                        )
                    },
                )
            }
        },
    headerItem: @Composable LazyItemScope.(label: String) -> Unit = { label ->
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsHeaderItem(
                modifier = Modifier,
                label = label,
            )
        }
    },
    loadingIndicator: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsLoadingIndicator(
                modifier = Modifier,
            )
        }
    },
    emptyContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsEmptyContent(
                modifier = Modifier,
            )
        }
    },
    errorContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsErrorContent(
                modifier = Modifier,
            )
        }
    },
    itemDivider: @Composable LazyItemScope.(index: Int) -> Unit = { index ->
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsItemDivider(
                modifier = Modifier,
                index = index,
            )
        }
    },
    loadingItem: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsLoadingItem(
                modifier = Modifier,
            )
        }
    },
) {
    val isLoading = viewState is ChannelAttachmentsViewState.Loading
    ContentBox(
        modifier = modifier,
        isLoading = isLoading,
        isEmpty = viewState is ChannelAttachmentsViewState.Content && viewState.items.isEmpty(),
        isError = viewState is ChannelAttachmentsViewState.Error,
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
        errorContent = errorContent,
    ) {
        val content = viewState as ChannelAttachmentsViewState.Content
        val groupedItems by remember(content.items) {
            derivedStateOf { content.items.groupBy(headerKeySelector) }
        }
        LazyColumn(
            modifier = Modifier.matchParentSize(),
            state = listState,
        ) {
            groupedItems.forEach { (group, items) ->
                if (stickHeader) {
                    stickyHeader(key = group) {
                        headerItem(group)
                    }
                } else {
                    item(key = group) {
                        headerItem(group)
                    }
                }
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.id },
                ) { index, item ->
                    itemContent(index, item)
                    itemDivider(index)
                }
            }
            if (content.isLoadingMore) {
                item { loadingItem() }
            }
        }
        LoadMoreHandler(
            lazyListState = listState,
            loadMore = onLoadMoreRequested,
        )
    }
}
