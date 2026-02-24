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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import kotlinx.coroutines.launch

/**
 * Default swipe actions for a channel list item: Pin/Unpin, Mute/Unmute, Delete.
 * Respects channel capabilities to show/hide individual actions.
 *
 * @param channelItem The channel item state to build actions for.
 */
@Composable
public fun RowScope.DefaultChannelSwipeActions(channelItem: ItemState.ChannelItemState) {
    val handler = LocalSwipeActionHandler.current ?: return
    val coordinator = LocalSwipeRevealCoordinator.current
    val scope = rememberCoroutineScope()
    val channel = channelItem.channel
    val capabilities = channel.ownCapabilities

    fun onAction(block: () -> Unit) {
        block()
        scope.launch { coordinator?.closeAll() }
    }

    // Pin / Unpin
    if (channelItem.isPinned) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_unpin),
            label = stringResource(R.string.stream_compose_swipe_action_unpin),
            onClick = { onAction { handler(UnpinChannel(channel)) } },
            backgroundColor = ChatTheme.colors.accentPrimary,
            contentColor = Color.White,
        )
    } else {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_pin),
            label = stringResource(R.string.stream_compose_swipe_action_pin),
            onClick = { onAction { handler(PinChannel(channel)) } },
            backgroundColor = ChatTheme.colors.accentPrimary,
            contentColor = Color.White,
        )
    }

    // Mute / Unmute
    if (capabilities.contains(ChannelCapabilities.MUTE_CHANNEL)) {
        if (channelItem.isMuted) {
            SwipeActionItem(
                icon = painterResource(R.drawable.stream_compose_ic_unmute),
                label = stringResource(R.string.stream_compose_swipe_action_unmute),
                onClick = { onAction { handler(UnmuteChannel(channel)) } },
                backgroundColor = ChatTheme.colors.accentNeutral,
                contentColor = Color.White,
            )
        } else {
            SwipeActionItem(
                icon = painterResource(R.drawable.stream_compose_ic_mute),
                label = stringResource(R.string.stream_compose_swipe_action_mute),
                onClick = { onAction { handler(MuteChannel(channel)) } },
                backgroundColor = ChatTheme.colors.accentNeutral,
                contentColor = Color.White,
            )
        }
    }

    // Delete
    if (capabilities.contains(ChannelCapabilities.DELETE_CHANNEL)) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_delete),
            label = stringResource(R.string.stream_compose_swipe_action_delete),
            onClick = { onAction { handler(DeleteConversation(channel)) } },
            backgroundColor = ChatTheme.colors.accentError,
            contentColor = Color.White,
        )
    }
}
