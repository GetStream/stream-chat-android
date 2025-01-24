/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.LoadingFooter
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Builds a list of [ChannelItem] elements, based on [channelsState] and action handlers that it receives.
 *
 * @param channelsState Exposes if we're loading more items, reaches the end of the list and the
 * current list of channels to show.
 * @param lazyListState State of the lazy list that represents the list of channels. Useful for controlling the
 * scroll state.
 * @param onLastItemReached Handler for when the user reaches the end of the list.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the channel list surrounding the content inside.
 * @param helperContent Composable that represents the helper content. Empty by default, but can be used to implement
 * scroll to top button.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Customizable UI component, that represents each item in the list.
 * @param divider Customizable UI component, that represents item dividers.
 */
@Composable
public fun Channels(
    channelsState: ChannelsState,
    lazyListState: LazyListState,
    onLastItemReached: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    helperContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory.channelList) {
            HelperContent()
        }
    },
    loadingMoreContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory.channelList) {
            LoadingMoreItemContent()
        }
    },
    itemContent: @Composable LazyItemScope.(ItemState) -> Unit,
    divider: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory.channelList) {
            DividerItem()
        }
    },
) {
    val (_, isLoadingMore, endOfChannels, channelItems) = channelsState

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("Stream_ChannelList"),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
        ) {
            item {
                DummyFirstChannelItem()
            }

            items(
                items = channelItems,
                key = { it.key },
            ) { item ->
                itemContent(item)

                divider()
            }

            if (isLoadingMore) {
                item {
                    loadingMoreContent()
                }
            }
        }

        if (!endOfChannels && channelItems.isNotEmpty()) {
            LoadMoreHandler(lazyListState) {
                onLastItemReached()
            }
        }

        helperContent()
    }
}

/**
 * The default loading more indicator.
 */
@Composable
internal fun DefaultChannelsLoadingMoreIndicator() {
    LoadingFooter(modifier = Modifier.fillMaxWidth())
}

/**
 * Represents an almost invisible dummy item to be added to the top of the list.
 *
 * If the list is scrolled to the top and a channel new item is added or moved
 * to the position above, then the list will automatically autoscroll to it.
 */
@Composable
private fun DummyFirstChannelItem() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth(),
    )
}
