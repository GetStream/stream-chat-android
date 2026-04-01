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

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import kotlinx.coroutines.launch

/**
 * Default swipe actions for a channel list item.
 *
 * Shows two actions:
 * - **More** (gray, left): Opens the channel options bottom sheet.
 * - **Primary action** (blue, right): Mute with fallback to Pin.
 *
 * Each action is a self-executing [ChannelAction] that invokes its handler via
 * [LocalSwipeActionHandler].
 *
 * @param channelItem The channel item state to build actions for.
 */
@Composable
public fun DefaultChannelSwipeActions(channelItem: ItemState.ChannelItemState) {
    val handler = LocalSwipeActionHandler.current ?: return
    val coordinator = LocalSwipeRevealCoordinator.current
    val moreHandler = LocalChannelMoreClickHandler.current
    val scope = rememberCoroutineScope()
    val channel = channelItem.channel

    if (moreHandler != null) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_design_ic_more),
            label = LocalContext.current.resources.getString(R.string.stream_compose_swipe_action_more),
            onClick = {
                scope.launch { coordinator?.closeAll() }
                moreHandler(channel)
            },
            style = SwipeActionStyle.Secondary,
        )
    }

    val primaryAction = rememberPrimarySwipeAction(
        channel = channel,
        isMuted = channelItem.isMuted,
        handler = handler,
    )
    if (primaryAction != null) {
        SwipeActionItem(
            icon = painterResource(primaryAction.icon),
            label = primaryAction.label,
            onClick = { primaryAction.onAction() },
            style = SwipeActionStyle.Primary,
        )
    }
}

/**
 * Resolves and remembers the primary swipe action based on channel capabilities.
 *
 * Priority: Mute → Pin.
 *
 * Pin is always available (membership operation, no capability gate).
 * Mute requires [ChannelCapabilities.MUTE_CHANNEL].
 */
@Composable
private fun rememberPrimarySwipeAction(
    channel: Channel,
    isMuted: Boolean,
    handler: (ChannelAction) -> Unit,
): ChannelAction {
    val resources = LocalResources.current
    val handlerState = rememberUpdatedState(handler)
    val isPinned = channel.isPinned()
    val canMute = channel.ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)

    return remember(channel.cid, isMuted, isPinned, canMute) {
        var resolved: ChannelAction? = null
        val onAction: () -> Unit = { resolved?.let { handlerState.value(it) } }

        resolved = muteAction(channel, isMuted, canMute, resources, onAction)
            ?: pinAction(channel, isPinned, resources, onAction)
        resolved
    }
}

private fun muteAction(
    channel: Channel,
    isMuted: Boolean,
    canMute: Boolean,
    resources: Resources,
    onAction: () -> Unit,
): ChannelAction? = when {
    !canMute -> null
    isMuted -> UnmuteChannel(channel, resources.getString(R.string.stream_compose_swipe_action_unmute), onAction)
    else -> MuteChannel(channel, resources.getString(R.string.stream_compose_swipe_action_mute), onAction)
}

private fun pinAction(
    channel: Channel,
    isPinned: Boolean,
    resources: Resources,
    onAction: () -> Unit,
): ChannelAction = if (isPinned) {
    UnpinChannel(channel, resources.getString(R.string.stream_compose_swipe_action_unpin), onAction)
} else {
    PinChannel(channel, resources.getString(R.string.stream_compose_swipe_action_pin), onAction)
}
