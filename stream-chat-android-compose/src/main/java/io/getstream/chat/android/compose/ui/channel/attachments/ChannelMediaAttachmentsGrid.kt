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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowWidthSizeClass
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.result.Error

/**
 * Displays the channel media attachments grid.
 *
 * @param viewState The state of the channel attachments view.
 * @param modifier The modifier for styling.
 * @param gridState The state of the lazy grid.
 * @param gridColumnCount The number of columns in the grid. If null, it will adapt based on the screen size.
 * @param headerKeySelector The function to select the group key for each item and group them in the list.
 * @param onLoadMoreRequested The callback to be invoked when more items need to be loaded.
 * @param onVideoPlaybackError The callback to be invoked when there is an error during video playback.
 * @param onSharingError The callback to be invoked when there is an error during attachment sharing.
 * @param itemContent The composable to display each item in the grid.
 * @param floatingHeader The composable to display as a floating header for each group of items.
 * @param loadingIndicator The composable to display when the grid is loading.
 * @param emptyContent The composable to display when the grid is empty.
 * @param errorContent The composable to display when there is an error.
 * @param loadingItem The composable to display when more items are being loaded.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelMediaAttachmentsGrid(
    viewState: ChannelAttachmentsViewState,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    gridColumnCount: Int? = null,
    headerKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String,
    onLoadMoreRequested: () -> Unit = {},
    onVideoPlaybackError: (error: Throwable) -> Unit = {},
    onSharingError: (error: Error) -> Unit = {},
    onItemClick: (
        item: ChannelAttachmentsViewState.Content.Item,
        onClick: () -> Unit,
    ) -> Unit = { item, onClick -> onClick() },
    itemContent: @Composable LazyGridItemScope.(
        index: Int,
        item: ChannelAttachmentsViewState.Content.Item,
        onClick: () -> Unit,
    ) -> Unit = { index, item, onClick ->
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsItem(
                modifier = Modifier,
                index = index,
                item = item,
                onClick = { onItemClick(item, onClick) },
            )
        }
    },
    floatingHeader: @Composable BoxScope.(label: String) -> Unit = { label ->
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsFloatingHeader(
                modifier = Modifier.align(Alignment.TopCenter),
                label = label,
            )
        }
    },
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
    loadingItem: @Composable LazyGridItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMediaAttachmentsLoadingItem(
                modifier = Modifier,
            )
        }
    },
) {
    val isLoading = viewState is ChannelAttachmentsViewState.Loading
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val gridColumnCount = gridColumnCount ?: run {
        GridColumnCounts.getValue(adaptiveInfo.windowSizeClass.windowWidthSizeClass)
    }
    var previewItemIndex by remember { mutableStateOf<Int?>(null) }
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
            columns = GridCells.Fixed(gridColumnCount),
            verticalArrangement = Arrangement.spacedBy(ChatTheme.dimens.attachmentsContentMediaGridSpacing),
            horizontalArrangement = Arrangement.spacedBy(ChatTheme.dimens.attachmentsContentMediaGridSpacing),
            state = gridState,
        ) {
            itemsIndexed(
                items = content.items,
                key = { _, item -> item.id },
            ) { index, item ->
                itemContent(index, item) {
                    previewItemIndex = index
                }
            }
            if (content.isLoadingMore) {
                item { loadingItem() }
            }
        }

        val groupKey by remember(content.items) {
            derivedStateOf { headerKeySelector(content.items[gridState.firstVisibleItemIndex]) }
        }

        floatingHeader(groupKey)

        LoadMoreHandler(
            lazyGridState = gridState,
            loadMore = onLoadMoreRequested,
        )

        previewItemIndex?.let { itemIndex ->
            val items = content.items
            ModalBottomSheet(
                onDismissRequest = { previewItemIndex = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                sheetMaxWidth = Dp.Unspecified,
                shape = RectangleShape,
                dragHandle = {},
                containerColor = ChatTheme.colors.barsBackground,
            ) {
                ChannelMediaAttachmentsPreviewScreen(
                    items = items,
                    initialIndex = itemIndex,
                    onLoadMoreRequested = onLoadMoreRequested,
                    onNavigationIconClick = { previewItemIndex = null },
                    onVideoPlaybackError = onVideoPlaybackError,
                    onSharingError = onSharingError,
                )
            }
        }
    }
}

/**
 * The default number of columns based on the window width size class.
 */
@Suppress("MagicNumber")
private val GridColumnCounts = mapOf(
    WindowWidthSizeClass.COMPACT to 3,
    WindowWidthSizeClass.MEDIUM to 4,
    WindowWidthSizeClass.EXPANDED to 6,
)
