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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel

@Composable
fun GroupChannelInfoScreen(
    state: GroupChannelInfoViewModel.State,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit,
    onPinnedMessagesClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            GroupChannelHeader(
                channel = state.channel,
                onNavigationIconClick = onNavigationIconClick,
                navigationIcon = navigationIcon,
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(padding),
            ) {
                LazyColumn {
                    // Members
                    items(state.channel.members) { member ->
                        ChannelInfoMemberItem(member, createdBy = state.channel.createdBy)
                        ChannelInfoContentDivider(height = 1.dp)
                    }
                    item {
                        ChannelInfoContentDivider(height = 8.dp)
                    }
                    // Pinned messages
                    item {
                        ChannelInfoOptionItem(
                            icon = R.drawable.stream_compose_ic_message_pinned,
                            text = stringResource(id = R.string.channel_info_option_pinned_messages),
                            onClick = onPinnedMessagesClick,
                        )
                        ChannelInfoContentDivider(height = 1.dp)
                    }
                }
            }
        },
    )
}

@Composable
private fun GroupChannelHeader(
    channel: Channel,
    onNavigationIconClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)?,
) {
    val user by ChatClient.instance().clientState.user.collectAsState()
    val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()

    MessageListHeader(
        channel = channel,
        currentUser = user,
        connectionState = connectionState,
        onBackPressed = onNavigationIconClick,
        leadingContent = {
            navigationIcon?.invoke() ?: run {
                with(ChatTheme.componentFactory) {
                    MessageListHeaderLeadingContent(
                        onBackPressed = onNavigationIconClick,
                    )
                }
            }
        },
        trailingContent = {
            Spacer(modifier = Modifier.size(ChatTheme.dimens.channelAvatarSize))
        },
    )
}
