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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

/**
 * Custom implementation of the [ChatComponentFactory.ChannelList] providing custom component for the channel item.
 *
 * It is used to highlight the [ChannelItem] if the channel is pinned.
 */
class CustomChannelListComponentFactory : ChatComponentFactory.ChannelList() {

    @Composable
    override fun LazyItemScope.ChannelItemContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
        onChannelClick: (Channel) -> Unit,
        onChannelLongClick: (Channel) -> Unit,
    ) {
        // Highlight the item background color if it is pinned
        val backgroundModifier = if (channelItem.channel.isPinned()) {
            Modifier.background(color = ChatTheme.colors.highlight)
        } else {
            Modifier
        }
        ChannelItem(
            modifier = backgroundModifier,
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    }
}

/**
 * Custom implementation of the [ChatComponentFactory] to provide custom components for the chat sample app.
 */
class CustomChatComponentFactory : ChatComponentFactory(
    channelList = CustomChannelListComponentFactory(),
)
