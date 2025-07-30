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

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState

@Composable
internal fun ChannelFilesAttachmentsList(
    viewState: ChannelAttachmentsViewState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    onViewAction: (action: ChannelAttachmentsViewAction) -> Unit = {},
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
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String,
    groupItem: @Composable LazyItemScope.(label: String) -> Unit = { label ->
        with(ChatTheme.componentFactory) {
            ChannelFilesAttachmentsGroupItem(
                modifier = Modifier,
                label = label,
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
            derivedStateOf { content.items.groupBy(groupKeySelector) }
        }
        LazyColumn(
            modifier = Modifier.matchParentSize(),
            state = listState,
        ) {
            groupedItems.forEach { (group, items) ->
                item(key = group) {
                    groupItem(group)
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
            loadMore = { onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested) },
        )
    }
}
