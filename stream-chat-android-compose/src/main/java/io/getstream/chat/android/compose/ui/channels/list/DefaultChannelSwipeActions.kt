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
 * - **More** (gray, left): Opens the channel options bottom sheet.
 * - **Primary action** (blue, right): Archive for DMs, Mute for groups — with fallback priority.
 *
 * The primary action is resolved via a priority list:
 * - DM: Archive → Mute → Pin
 * - Group: Mute → Archive → Pin
 *
 * Each action is a self-executing [ChannelAction] that invokes its handler via
 * [LocalSwipeActionHandler].
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

    fun onAction(action: ChannelAction) {
        handler(action)
        scope.launch { coordinator?.closeAll() }
    }

    // "More" action — opens the channel options sheet
    if (moreHandler != null) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_more_options),
            label = stringResource(R.string.stream_compose_swipe_action_more),
            onClick = {
                scope.launch { coordinator?.closeAll() }
                moreHandler(channel)
            },
            style = SwipeActionStyle.Secondary,
        )
    }

    // Primary action (rightmost) — resolved by channel type and capabilities
    val primaryAction = resolvePrimarySwipeAction(
        channel = channel,
        isMuted = channelItem.isMuted,
        isPinned = channelItem.isPinned,
    )
    if (primaryAction != null) {
        SwipeActionItem(
            icon = painterResource(primaryAction.icon),
            label = primaryAction.label,
            onClick = { onAction(primaryAction) },
            style = SwipeActionStyle.Primary,
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
@Composable
private fun resolvePrimarySwipeAction(
    channel: Channel,
    isMuted: Boolean,
    isPinned: Boolean,
): ChannelAction? {
    val capabilities = channel.ownCapabilities
    val isArchived = channel.isArchive()
    val canMute = capabilities.contains(ChannelCapabilities.MUTE_CHANNEL)
    val isDM = channel.isDistinct() && channel.members.size == 2

    // Build candidate actions with resolved labels
    val archiveAction: ChannelAction = if (isArchived) {
        UnarchiveChannel(
            channel = channel,
            label = stringResource(R.string.stream_compose_swipe_action_unarchive),
            onAction = {},
        )
    } else {
        ArchiveChannel(
            channel = channel,
            label = stringResource(R.string.stream_compose_swipe_action_archive),
            onAction = {},
        )
    }

    val muteAction: ChannelAction? = if (canMute) {
        if (isMuted) {
            UnmuteChannel(
                channel = channel,
                label = stringResource(R.string.stream_compose_swipe_action_unmute),
                onAction = {},
            )
        } else {
            MuteChannel(
                channel = channel,
                label = stringResource(R.string.stream_compose_swipe_action_mute),
                onAction = {},
            )
        }
    } else {
        null
    }

    val pinAction: ChannelAction = if (isPinned) {
        UnpinChannel(
            channel = channel,
            label = stringResource(R.string.stream_compose_swipe_action_unpin),
            onAction = {},
        )
    } else {
        PinChannel(
            channel = channel,
            label = stringResource(R.string.stream_compose_swipe_action_pin),
            onAction = {},
        )
    }

    val candidates: List<ChannelAction?> = if (isDM) {
        listOf(archiveAction, muteAction, pinAction)
    } else {
        listOf(muteAction, archiveAction, pinAction)
    }

    return candidates.firstOrNull { it != null }
}
