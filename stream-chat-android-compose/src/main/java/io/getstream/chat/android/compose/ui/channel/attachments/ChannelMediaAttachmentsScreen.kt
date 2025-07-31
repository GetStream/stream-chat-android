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

import androidx.compose.foundation.background
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

/**
 * Displays the channel media attachments screen.
 *
 * @param viewModelFactory The factory to create the [ChannelAttachmentsViewModel].
 * @param modifier The modifier for styling.
 * @param groupKeySelector The function to select the group key for each media item and group them in the grid.
 * @param gridColumnCount The number of columns in the grid. If null, it will adapt based on the screen size.
 * @param onNavigationIconClick The callback to be invoked when the navigation icon is clicked.
 */
@Composable
public fun ChannelMediaAttachmentsScreen(
    viewModelFactory: ChannelAttachmentsViewModelFactory,
    modifier: Modifier = Modifier,
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.GroupKeySelector,
    gridColumnCount: Int? = null,
    onNavigationIconClick: () -> Unit = {},
) {
    val viewModel = viewModel<ChannelAttachmentsViewModel>(factory = viewModelFactory)
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ChannelMediaAttachmentsContent(
        modifier = modifier,
        viewState = viewState,
        groupKeySelector = groupKeySelector,
        gridColumnCount = gridColumnCount,
        onNavigationIconClick = onNavigationIconClick,
        onViewAction = viewModel::onViewAction,
    )
}

@Composable
private fun ChannelMediaAttachmentsContent(
    modifier: Modifier,
    viewState: ChannelAttachmentsViewState,
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.GroupKeySelector,
    gridColumnCount: Int? = null,
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
        ChannelMediaAttachmentsGrid(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            viewState = viewState,
            gridState = gridState,
            groupKeySelector = groupKeySelector,
            gridColumnCount = gridColumnCount,
            onViewAction = onViewAction,
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
                    message = PreviewMessageData.message1,
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
