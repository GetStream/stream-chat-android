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

package io.getstream.chat.android.compose.ui.channel.attachments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModelFactory
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.result.Error

/**
 * Displays the channel media attachments screen.
 *
 * This screen includes a top bar, a grid of media attachments,
 * and display a preview screen when an attachment is clicked.
 * The attachments can be images or videos.
 * The screen supports loading more attachments when the user scrolls to the end of the grid.
 * It also supports grouping media items by a header key,
 * which can be customized using the [headerKeySelector] function.
 *
 * @param viewModelFactory The factory to create the [ChannelAttachmentsViewModel].
 * @param modifier The modifier for styling.
 * @param gridColumnCount The number of columns in the grid. If null, it will adapt based on the screen size.
 * @param headerKeySelector The function to select the group key for each media item and group them in the grid.
 * @param onNavigationIconClick The callback to be invoked when the navigation icon is clicked.
 * @param onVideoPlaybackError The callback to be invoked when there is an error during video playback.
 * @param onSharingError The callback to be invoked when there is an error during attachment sharing.
 */
@Composable
public fun ChannelMediaAttachmentsScreen(
    viewModelFactory: ChannelAttachmentsViewModelFactory,
    modifier: Modifier = Modifier,
    gridColumnCount: Int? = null,
    headerKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.HeaderKeySelector,
    onNavigationIconClick: () -> Unit = {},
    onVideoPlaybackError: (error: Throwable) -> Unit = {},
    onSharingError: (error: Error) -> Unit = {},
) {
    val viewModel = viewModel<ChannelAttachmentsViewModel>(factory = viewModelFactory)
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ChannelMediaAttachmentsContent(
        modifier = modifier,
        viewState = viewState,
        gridColumnCount = gridColumnCount,
        headerKeySelector = headerKeySelector,
        onNavigationIconClick = onNavigationIconClick,
        onLoadMoreRequested = { viewModel.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested) },
        onVideoPlaybackError = onVideoPlaybackError,
        onSharingError = onSharingError,
    )
}

@Composable
private fun ChannelMediaAttachmentsContent(
    modifier: Modifier,
    viewState: ChannelAttachmentsViewState,
    gridColumnCount: Int? = null,
    headerKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.HeaderKeySelector,
    onNavigationIconClick: () -> Unit = {},
    onLoadMoreRequested: () -> Unit = {},
    onVideoPlaybackError: (error: Throwable) -> Unit = {},
    onSharingError: (error: Error) -> Unit = {},
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
        ChannelMediaAttachmentsGrid(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            viewState = viewState,
            gridState = gridState,
            headerKeySelector = headerKeySelector,
            gridColumnCount = gridColumnCount,
            onLoadMoreRequested = onLoadMoreRequested,
            onVideoPlaybackError = onVideoPlaybackError,
            onSharingError = onSharingError,
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
                .fillMaxSize(.25f)
                .aspectRatio(1f),
            user = item.message.user,
            showIndicator = false,
            showBorder = true,
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
private fun ChannelMediaAttachmentsLoadingPreview() {
    ChatTheme {
        ChannelMediaAttachmentsLoading()
    }
}

@Composable
internal fun ChannelMediaAttachmentsLoading() {
    ChannelMediaAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Loading,
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsEmptyPreview() {
    ChatTheme {
        ChannelMediaAttachmentsEmpty()
    }
}

@Composable
internal fun ChannelMediaAttachmentsEmpty() {
    ChannelMediaAttachmentsContent(
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
    ChannelMediaAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Content(
            items = PreviewMessageData.messageWithUserAndAttachment.attachments.map { attachment ->
                ChannelAttachmentsViewState.Content.Item(
                    message = PreviewMessageData.messageWithUserAndAttachment,
                    attachment = attachment,
                )
            },
        ),
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsErrorPreview() {
    ChatTheme {
        ChannelMediaAttachmentsError()
    }
}

@Composable
internal fun ChannelMediaAttachmentsError() {
    ChannelMediaAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Error(
            message = "An error occurred while loading attachments.",
        ),
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsLoadingMorePreview() {
    ChatTheme {
        ChannelMediaAttachmentsLoadingMore()
    }
}

@Composable
internal fun ChannelMediaAttachmentsLoadingMore() {
    ChannelMediaAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        viewState = ChannelAttachmentsViewState.Content(
            items = PreviewMessageData.messageWithUserAndAttachment.attachments.map { attachment ->
                ChannelAttachmentsViewState.Content.Item(
                    message = PreviewMessageData.messageWithUserAndAttachment,
                    attachment = attachment,
                )
            },
            isLoadingMore = true,
        ),
    )
}
