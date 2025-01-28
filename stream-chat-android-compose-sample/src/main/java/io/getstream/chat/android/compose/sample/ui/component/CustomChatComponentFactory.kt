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

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User

class CustomChatComponentFactory : ChatComponentFactory(
    channelListHeader = object : ChannelListHeader() {
        @Composable
        override fun RowScope.LeadingContent(
            currentUser: User?,
            onAvatarClick: (User?) -> Unit,
        ) {
            Icon(
                imageVector = Icons.Rounded.Face,
                contentDescription = null,
            )
        }

        @Composable
        override fun RowScope.CenterContent(
            connectionState: ConnectionState,
            title: String,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                text = title,
                textAlign = TextAlign.Center,
                color = ChatTheme.colors.primaryAccent,
            )
        }

        @Composable
        override fun RowScope.TrailingContent(
            onHeaderActionClick: () -> Unit,
        ) {
            Icon(
                imageVector = Icons.Rounded.ThumbUp,
                contentDescription = null,
            )
        }
    },
    searchInput = object : SearchInput() {
        @Composable
        override fun RowScope.LeadingIcon() {
            Icon(
                imageVector = Icons.Rounded.Build,
                contentDescription = null,
            )
        }

        @Composable
        override fun Label() {
            Text(
                text = "Search",
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    },
    channelList = object : ChannelList() {
        @Composable
        override fun LazyItemScope.ChannelItemContent(
            channelItem: ItemState.ChannelItemState,
            currentUser: User?,
            onChannelClick: (Channel) -> Unit,
            onChannelLongClick: (Channel) -> Unit,
        ) {
            Text(
                text = channelItem.channel.name,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        @Composable
        override fun LoadingIndicator(modifier: Modifier) {
            Text(
                text = "Loading...",
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        @Composable
        override fun BoxScope.HelperContent() {
            Text(
                text = "Helper content",
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    },
)
