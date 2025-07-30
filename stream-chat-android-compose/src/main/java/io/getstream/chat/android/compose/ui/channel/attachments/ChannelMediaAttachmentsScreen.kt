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

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutConstraints
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModelFactory
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import java.util.Date

/**
 * Displays the channel media attachments screen.
 *
 * @param viewModelFactory The factory to create the [ChannelAttachmentsViewModel].
 * @param modifier The modifier for styling.
 * @param groupKeySelector The function to select the group key for each item in the list.
 * This is used to group items in the list.
 * @param onNavigationIconClick The callback to be invoked when the navigation icon is clicked.
 */
@Composable
public fun ChannelMediaAttachmentsScreen(
    viewModelFactory: ChannelAttachmentsViewModelFactory,
    modifier: Modifier = Modifier,
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String = GroupKeySelector,
    onNavigationIconClick: () -> Unit = {},
) {
    val viewModel = viewModel<ChannelAttachmentsViewModel>(factory = viewModelFactory)
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ChannelMediaAttachmentsScaffold(
        modifier = modifier,
        viewState = viewState,
        groupKeySelector = groupKeySelector,
        onNavigationIconClick = onNavigationIconClick,
        onViewAction = viewModel::onViewAction,
    )
}

@Composable
private fun ChannelMediaAttachmentsScaffold(
    viewState: ChannelAttachmentsViewState,
    modifier: Modifier = Modifier,
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String = GroupKeySelector,
    onNavigationIconClick: () -> Unit = {},
    onViewAction: (action: ChannelAttachmentsViewAction) -> Unit = {},
) {
    val gridState = rememberLazyGridState()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTheme.componentFactory.ChannelMediaAttachmentsTopBar(
                gridState = gridState,
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        containerColor = ChatTheme.colors.appBackground,
    ) { padding ->
        ChannelMediaAttachmentsList(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            viewState = viewState,
            gridState = gridState,
            groupKeySelector = groupKeySelector,
            onViewAction = onViewAction,
        )
    }
}

/**
 * The default group key selector for the channel media attachments list.
 * It groups items by the relative time span of their creation date, skipping the day of the month.
 */
private val GroupKeySelector = { item: ChannelAttachmentsViewState.Content.Item ->
    DateUtils.getRelativeTimeSpanString(
        item.message.getCreatedAtOrThrow().time,
        Date().time,
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_NO_MONTH_DAY,
    ).toString()
}

@Suppress("MagicNumber")
private val ColumnSizes = mapOf(
    WindowWidthSizeClass.COMPACT to 3,
    WindowWidthSizeClass.MEDIUM to 4,
    WindowWidthSizeClass.EXPANDED to 6,
)

@Composable
private fun ChannelMediaAttachmentsList(
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
    val columnSize = ColumnSizes.getValue(windowSize.windowWidthSizeClass)
    ContentBox(
        modifier = modifier,
        isLoading = isLoading,
        isEmpty = viewState is ChannelAttachmentsViewState.Content && viewState.items.isEmpty(),
        isError = viewState is ChannelAttachmentsViewState.Error,
        loadingIndicator = loadingIndicator,
        emptyContent = emptyContent,
        errorContent = errorContent,
    ) {
        AdaptiveLayoutConstraints.DETAIL_PANE_WEIGHT
        val content = viewState as ChannelAttachmentsViewState.Content
        LazyVerticalGrid(
            modifier = Modifier.matchParentSize(),
            columns = GridCells.Fixed(columnSize),
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

@Composable
internal fun LazyGridItemScope.ChannelMediaAttachmentsItem(
    modifier: Modifier,
    item: ChannelAttachmentsViewState.Content.Item,
    onClick: () -> Unit,
) {
    val data = item.attachment.upload ?: item.attachment.imagePreviewUrl
    Box(
        modifier = Modifier
            .clickable(onClick = onClick),
    ) {
        StreamAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
            data = data,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        @Suppress("MagicNumber")
        UserAvatar(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .fillMaxSize(.3f)
                .aspectRatio(1f)
                .background(
                    color = Color.White,
                    shape = ChatTheme.shapes.avatar,
                )
                .padding(1.dp),
            user = item.message.user,
            showOnlineIndicator = false,
        )

        if (item.attachment.isVideo()) {
            @Suppress("MagicNumber")
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(.4f)
                    .aspectRatio(1f),
                imageVector = Icons.Rounded.PlayArrow,
                tint = Color.White,
                contentDescription = stringResource(R.string.stream_compose_cd_play_button),
            )
        }
    }
}

@Preview
@Composable
private fun ChannelMediaAttachmentsContentLoadingPreview() {
    ChatTheme {
        ChannelMediaAttachmentsLoading()
    }
}

@Composable
internal fun ChannelMediaAttachmentsLoading() {
    ChannelMediaAttachmentsScaffold(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Loading,
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsContentEmptyPreview() {
    ChatTheme {
        ChannelMediaAttachmentsEmpty()
    }
}

@Composable
internal fun ChannelMediaAttachmentsEmpty() {
    ChannelMediaAttachmentsScaffold(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Content(
            items = emptyList(),
        ),
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsContentPreview() {
    ChatTheme {
        ChannelMediaAttachmentsContent()
    }
}

@Composable
internal fun ChannelMediaAttachmentsContent() {
    ChannelMediaAttachmentsScaffold(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Content(
            items = PreviewMessageData.messageWithUserAndAttachment.attachments.map { attachment ->
                ChannelAttachmentsViewState.Content.Item(
                    message = PreviewMessageData.message1,
                    attachment = attachment,
                )
            },
        ),
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsContentErrorPreview() {
    ChatTheme {
        ChannelMediaAttachmentsError()
    }
}

@Composable
internal fun ChannelMediaAttachmentsError() {
    ChannelMediaAttachmentsScaffold(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Error(
            message = "An error occurred while loading attachments.",
        ),
    )
}
