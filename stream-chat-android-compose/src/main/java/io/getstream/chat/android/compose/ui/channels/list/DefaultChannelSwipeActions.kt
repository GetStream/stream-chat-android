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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.client.extensions.isArchive
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.util.isDistinct
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.channels.actions.ArchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnarchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import kotlinx.coroutines.launch

/**
 * Default swipe actions for a channel list item.
 *
 * Shows two actions:
 * - **Primary action** (blue): Archive for DMs, Mute for groups — with fallback priority.
 * - **More** (gray): Opens the channel options bottom sheet.
 *
 * The primary action is resolved via a priority list:
 * - DM: Archive → Mute → Pin
 * - Group: Mute → Archive → Pin
 *
 * @param channelItem The channel item state to build actions for.
 */
@Composable
public fun RowScope.DefaultChannelSwipeActions(channelItem: ItemState.ChannelItemState) {
    val handler = LocalSwipeActionHandler.current ?: return
    val coordinator = LocalSwipeRevealCoordinator.current
    val moreHandler = LocalChannelMoreClickHandler.current
    val scope = rememberCoroutineScope()
    val channel = channelItem.channel

    fun onAction(block: () -> Unit) {
        block()
        scope.launch { coordinator?.closeAll() }
    }

    // Resolve and render the primary action
    val primaryAction = resolvePrimaryAction(
        channel = channel,
        isMuted = channelItem.isMuted,
        isPinned = channelItem.isPinned,
    )
    if (primaryAction != null) {
        RenderSwipeAction(
            action = primaryAction,
            channel = channel,
            style = SwipeActionStyle.Primary,
            onAction = { onAction { handler(it) } },
        )
    }

    // "More" action — opens the channel options sheet
    if (moreHandler != null) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_more_options),
            label = stringResource(R.string.stream_compose_swipe_action_more),
            onClick = { onAction { moreHandler(channel) } },
            style = SwipeActionStyle.Secondary,
        )
    }
}

/**
 * Resolves the primary swipe action based on channel type and capabilities.
 *
 * DM priority: Archive → Mute → Pin.
 * Group priority: Mute → Archive → Pin.
 *
 * Archive and Pin are always available (membership operations, no capability gate).
 * Mute requires [ChannelCapabilities.MUTE_CHANNEL].
 */
private fun resolvePrimaryAction(
    channel: Channel,
    isMuted: Boolean,
    isPinned: Boolean,
): ResolvedSwipeAction? {
    val capabilities = channel.ownCapabilities
    val isArchived = channel.isArchive()
    val canMute = capabilities.contains(ChannelCapabilities.MUTE_CHANNEL)
    val isDM = channel.isDistinct() && channel.members.size == 2

    val candidates: List<ResolvedSwipeAction?> = if (isDM) {
        listOf(
            ResolvedSwipeAction.Archive(isArchived),
            ResolvedSwipeAction.Mute(isMuted).takeIf { canMute },
            ResolvedSwipeAction.Pin(isPinned),
        )
    } else {
        listOf(
            ResolvedSwipeAction.Mute(isMuted).takeIf { canMute },
            ResolvedSwipeAction.Archive(isArchived),
            ResolvedSwipeAction.Pin(isPinned),
        )
    }

    return candidates.firstOrNull { it != null }
}

/**
 * Renders a resolved swipe action as a [SwipeActionItem].
 */
@Composable
private fun RenderSwipeAction(
    action: ResolvedSwipeAction,
    channel: Channel,
    style: SwipeActionStyle,
    onAction: (ChannelAction) -> Unit,
) {
    when (action) {
        is ResolvedSwipeAction.Archive -> {
            if (action.isArchived) {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_unarchive),
                    label = stringResource(R.string.stream_compose_swipe_action_unarchive),
                    onClick = { onAction(UnarchiveChannel(channel)) },
                    style = style,
                )
            } else {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_archive),
                    label = stringResource(R.string.stream_compose_swipe_action_archive),
                    onClick = { onAction(ArchiveChannel(channel)) },
                    style = style,
                )
            }
        }
        is ResolvedSwipeAction.Mute -> {
            if (action.isMuted) {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_unmute),
                    label = stringResource(R.string.stream_compose_swipe_action_unmute),
                    onClick = { onAction(UnmuteChannel(channel)) },
                    style = style,
                )
            } else {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_mute),
                    label = stringResource(R.string.stream_compose_swipe_action_mute),
                    onClick = { onAction(MuteChannel(channel)) },
                    style = style,
                )
            }
        }
        is ResolvedSwipeAction.Pin -> {
            if (action.isPinned) {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_unpin),
                    label = stringResource(R.string.stream_compose_swipe_action_unpin),
                    onClick = { onAction(UnpinChannel(channel)) },
                    style = style,
                )
            } else {
                SwipeActionItem(
                    icon = painterResource(R.drawable.stream_compose_ic_pin),
                    label = stringResource(R.string.stream_compose_swipe_action_pin),
                    onClick = { onAction(PinChannel(channel)) },
                    style = style,
                )
            }
        }
    }
}

/**
 * Internal representation of a resolved swipe action, used by [resolvePrimaryAction]
 * to select the best action based on channel type and capabilities.
 *
 * This only carries the toggle state needed for icon/label selection.
 * The actual [Channel] is passed separately when constructing [ChannelAction]s.
 */
private sealed interface ResolvedSwipeAction {

    data class Archive(val isArchived: Boolean) : ResolvedSwipeAction

    data class Mute(val isMuted: Boolean) : ResolvedSwipeAction

    data class Pin(val isPinned: Boolean) : ResolvedSwipeAction
}
