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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.attachments.preview.ConfirmShareLargeFileDialog
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPager
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewPageIndicator
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewShareIcon
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewSharingInProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChannelMediaAttachmentsPreviewBottomBarParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelMediaAttachmentsPreviewViewAction
import io.getstream.chat.android.compose.viewmodel.channel.ChannelMediaAttachmentsPreviewViewEvent
import io.getstream.chat.android.compose.viewmodel.channel.ChannelMediaAttachmentsPreviewViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelMediaAttachmentsPreviewViewModelFactory
import io.getstream.chat.android.compose.viewmodel.channel.ChannelMediaAttachmentsPreviewViewState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.chat.android.ui.common.utils.shareLocalFile
import io.getstream.result.Error

/**
 * A full-screen pager that allows users to swipe through media attachments in a channel.
 * It includes a top bar for navigation and a bottom bar to show the current index of the media being viewed.
 *
 * @param items The list of media attachments to display.
 * @param initialIndex The initial index of the media attachment to display.
 * @param onLoadMoreRequested Callback invoked when more items need to be loaded.
 * @param onNavigationIconClick Callback invoked when the navigation icon is clicked.
 * @param onVideoPlaybackError Callback invoked when there is an error during video playback.
 * @param onSharingError Callback invoked when there is an error during attachment sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelMediaAttachmentsPreviewScreen(
    items: List<ChannelAttachmentsViewState.Content.Item>,
    initialIndex: Int,
    onLoadMoreRequested: () -> Unit = {},
    onNavigationIconClick: () -> Unit = {},
    onVideoPlaybackError: (error: Throwable) -> Unit = {},
    onSharingError: (error: Error) -> Unit = {},
) {
    val viewModel = viewModel<ChannelMediaAttachmentsPreviewViewModel>(
        factory = ChannelMediaAttachmentsPreviewViewModelFactory(context = LocalContext.current),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connectionState by ChatClient.instance().clientState.connectionState.collectAsStateWithLifecycle()

    ChannelMediaAttachmentsPreviewContent(
        items = items,
        initialIndex = initialIndex,
        viewState = state,
        connectionState = connectionState,
        onNavigationIconClick = onNavigationIconClick,
        onVideoPlaybackError = onVideoPlaybackError,
        onLoadMoreRequested = onLoadMoreRequested,
        onShareClick = { attachment ->
            viewModel.onViewAction(ChannelMediaAttachmentsPreviewViewAction.ShareClick(attachment))
        },
    )

    state.promptedAttachment?.let { attachment ->
        ConfirmShareLargeFileDialog(
            attachment = attachment,
            onConfirm = {
                viewModel.onViewAction(
                    ChannelMediaAttachmentsPreviewViewAction.ConfirmSharingClick(attachment),
                )
            },
            onDismiss = {
                viewModel.onViewAction(ChannelMediaAttachmentsPreviewViewAction.DismissSharingClick)
            },
        )
    }

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ChannelMediaAttachmentsPreviewViewEvent.ShareLocalFile -> context.shareLocalFile(
                    uri = event.uri,
                    mimeType = event.mimeType,
                    text = event.text,
                )

                is ChannelMediaAttachmentsPreviewViewEvent.SharingError -> onSharingError(event.error)
            }
        }
    }
}

@Composable
private fun ChannelMediaAttachmentsPreviewContent(
    items: List<ChannelAttachmentsViewState.Content.Item>,
    initialIndex: Int,
    viewState: ChannelMediaAttachmentsPreviewViewState,
    connectionState: ConnectionState,
    onLoadMoreRequested: () -> Unit = {},
    onNavigationIconClick: () -> Unit = {},
    onVideoPlaybackError: (error: Throwable) -> Unit = {},
    onShareClick: (attachment: Attachment) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = items::size,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTheme.componentFactory.ChannelMediaAttachmentsPreviewTopBar(
                item = items[pagerState.currentPage],
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        bottomBar = {
            ChannelMediaAttachmentsPreviewBottomBar(
                items = items,
                viewState = viewState,
                connectionState = connectionState,
                pagerState = pagerState,
                onShareClick = onShareClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = ChatTheme.colors.appBackground,
    ) { padding ->
        MediaGalleryPager(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            pagerState = pagerState,
            attachments = remember(items) {
                items.map(ChannelAttachmentsViewState.Content.Item::attachment)
            },
            onPlaybackError = onVideoPlaybackError,
        )
        LoadMoreHandler(
            pagerState = pagerState,
            pageCount = items::size,
            loadMore = onLoadMoreRequested,
        )
    }
}

@Composable
private fun ChannelMediaAttachmentsPreviewBottomBar(
    items: List<ChannelAttachmentsViewState.Content.Item>,
    viewState: ChannelMediaAttachmentsPreviewViewState,
    connectionState: ConnectionState,
    pagerState: PagerState,
    onShareClick: (Attachment) -> Unit,
) {
    val config = ChatTheme.mediaGalleryConfig

    ChatTheme.componentFactory.ChannelMediaAttachmentsPreviewBottomBar(
        params = ChannelMediaAttachmentsPreviewBottomBarParams(
            centerContent = {
                if (viewState.isPreparingToShare) {
                    MediaGalleryPreviewSharingInProgressIndicator()
                } else {
                    MediaGalleryPreviewPageIndicator(
                        currentPage = pagerState.currentPage,
                        totalPages = items.size,
                    )
                }
            },
            leadingContent = {
                if (config.isShareVisible) {
                    MediaGalleryPreviewShareIcon(
                        connectionState = connectionState,
                        isSharingInProgress = viewState.isPreparingToShare,
                        onClick = { onShareClick(items[pagerState.currentPage].attachment) },
                    )
                }
            },
        ),
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsPreviewContentPreview() {
    ChatTheme {
        ChannelMediaAttachmentsPreviewContent()
    }
}

@Composable
internal fun ChannelMediaAttachmentsPreviewContent() {
    val item = ChannelAttachmentsViewState.Content.Item(
        message = PreviewMessageData.messageWithUserAndAttachment,
        attachment = PreviewMessageData.messageWithUserAndAttachment.attachments.first(),
    )
    ChannelMediaAttachmentsPreviewContent(
        items = listOf(item),
        initialIndex = 0,
        viewState = ChannelMediaAttachmentsPreviewViewState(),
        connectionState = ConnectionState.Connected,
    )
}

@Preview
@Composable
private fun ChannelMediaAttachmentsPreviewPreparingToShareContentPreview() {
    ChatTheme {
        ChannelMediaAttachmentsPreviewPreparingToShareContent()
    }
}

@Composable
internal fun ChannelMediaAttachmentsPreviewPreparingToShareContent() {
    val item = ChannelAttachmentsViewState.Content.Item(
        message = PreviewMessageData.messageWithUserAndAttachment,
        attachment = PreviewMessageData.messageWithUserAndAttachment.attachments.first(),
    )
    ChannelMediaAttachmentsPreviewContent(
        items = listOf(item),
        initialIndex = 0,
        viewState = ChannelMediaAttachmentsPreviewViewState(isPreparingToShare = true),
        connectionState = ConnectionState.Connected,
    )
}
