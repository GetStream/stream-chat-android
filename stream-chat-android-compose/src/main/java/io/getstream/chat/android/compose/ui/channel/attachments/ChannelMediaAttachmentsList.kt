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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowWidthSizeClass
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState

@Composable
internal fun ChannelMediaAttachmentsList(
    viewState: ChannelAttachmentsViewState,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    onViewAction: (action: ChannelAttachmentsViewAction) -> Unit = {},
    loadingIndicator: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsLoadingIndicator(
                modifier = Modifier,
            )
        }
    },
    emptyContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsEmptyContent(
                modifier = Modifier,
            )
        }
    },
    errorContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsErrorContent(
                modifier = Modifier,
            )
        }
    },
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String,
    groupItem: @Composable LazyGridItemScope.(label: String) -> Unit = { label ->
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsGroupItem(
                modifier = Modifier,
                label = label,
            )
        }
    },
    itemContent: @Composable LazyGridItemScope.(index: Int, item: ChannelAttachmentsViewState.Content.Item) -> Unit =
        { index, item ->
            val previewHandlers = ChatTheme.attachmentPreviewHandlers
            with(ChatTheme.componentFactory) {
                ChannelMediaAttachmentsItem(
                    modifier = Modifier,
                    index = index,
                    item = item,
                    onClick = {
                        onFileAttachmentContentItemClick(
                            previewHandlers = previewHandlers,
                            attachment = item.attachment,
                        )
                    },
                )
            }
        },
    loadingItem: @Composable LazyGridItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsLoadingItem(
                modifier = Modifier,
            )
        }
    },
) {
    val isLoading = viewState is ChannelAttachmentsViewState.Loading
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val gridColumnSize = ColumnSizes.getValue(windowSize.windowWidthSizeClass)
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
        LazyVerticalGrid(
            modifier = Modifier.matchParentSize(),
            columns = GridCells.Fixed(gridColumnSize),
            verticalArrangement = Arrangement.spacedBy(ChatTheme.dimens.attachmentsContentMediaGridSpacing),
            horizontalArrangement = Arrangement.spacedBy(ChatTheme.dimens.attachmentsContentMediaGridSpacing),
            state = gridState,
        ) {
            itemsIndexed(
                items = content.items,
                key = { _, item -> item.id },
            ) { index, item ->
                itemContent(index, item)
            }
            if (content.isLoadingMore) {
                item { loadingItem() }
            }
        }
        LoadMoreHandler(
            lazyGridState = gridState,
            loadMore = { onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested) },
        )
    }
}

@Suppress("MagicNumber")
private val ColumnSizes = mapOf(
    WindowWidthSizeClass.COMPACT to 3,
    WindowWidthSizeClass.MEDIUM to 4,
    WindowWidthSizeClass.EXPANDED to 6,
)
