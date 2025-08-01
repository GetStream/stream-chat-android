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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPager
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelMediaAttachmentsPreview(
    items: List<ChannelAttachmentsViewState.Content.Item>,
    initialItem: ChannelAttachmentsViewState.Content.Item,
    onNavigationIconClick: () -> Unit,
    onVideoPlaybackError: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = items.indexOf(initialItem),
        pageCount = items::size,
    )
    val topBarTitle by remember { derivedStateOf(items[pagerState.currentPage].message.user::name) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTheme.componentFactory.ChannelMediaAttachmentsPreviewTopBar(
                title = {
                    Text(
                        text = topBarTitle,
                        style = ChatTheme.typography.title3Bold,
                        maxLines = 1,
                    )
                },
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        bottomBar = {
            ChatTheme.componentFactory.ChannelMediaAttachmentsPreviewBottomBar(
                text = "${pagerState.currentPage + 1} / ${items.size}",
            )
        },
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
    }
}
