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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.client.extensions.isArchive
import io.getstream.chat.android.client.extensions.isPinned
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
public fun DefaultChannelSwipeActions(channelItem: ItemState.ChannelItemState) {
    val handler = LocalSwipeActionHandler.current ?: return
    val coordinator = LocalSwipeRevealCoordinator.current
    val moreHandler = LocalChannelMoreClickHandler.current
    val scope = rememberCoroutineScope()
    val channel = channelItem.channel

    if (moreHandler != null) {
        SwipeActionItem(
            icon = painterResource(R.drawable.stream_compose_ic_more_options),
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
 * Resolves and remembers the primary swipe action based on channel type and capabilities.
 *
 * DM priority: Archive → Mute → Pin.
 * Group priority: Mute → Archive → Pin.
 *
 * Archive and Pin are always available (membership operations, no capability gate).
 * Mute requires [ChannelCapabilities.MUTE_CHANNEL].
 */
@Composable
private fun rememberPrimarySwipeAction(
    channel: Channel,
    isMuted: Boolean,
    handler: (ChannelAction) -> Unit,
): ChannelAction? {
    val resources = LocalContext.current.resources
    val handlerState = rememberUpdatedState(handler)
    val isPinned = channel.isPinned()
    val isArchived = channel.isArchive()
    val canMute = channel.ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)
    val isDM = channel.isDistinct() && channel.members.size == 2

    return remember(channel.cid, isMuted, isPinned, isArchived, canMute, isDM) {
        var resolved: ChannelAction? = null
        val onAction: () -> Unit = { resolved?.let { handlerState.value(it) } }

        val archiveAction: ChannelAction = if (isArchived) {
            UnarchiveChannel(
                channel = channel,
                label = resources.getString(R.string.stream_compose_swipe_action_unarchive),
                onAction = onAction,
            )
        } else {
            ArchiveChannel(
                channel = channel,
                label = resources.getString(R.string.stream_compose_swipe_action_archive),
                onAction = onAction,
            )
        }

        val muteAction: ChannelAction? = if (canMute) {
            if (isMuted) {
                UnmuteChannel(
                    channel = channel,
                    label = resources.getString(R.string.stream_compose_swipe_action_unmute),
                    onAction = onAction,
                )
            } else {
                MuteChannel(
                    channel = channel,
                    label = resources.getString(R.string.stream_compose_swipe_action_mute),
                    onAction = onAction,
                )
            }
        } else {
            null
        }

        val pinAction: ChannelAction = if (isPinned) {
            UnpinChannel(
                channel = channel,
                label = resources.getString(R.string.stream_compose_swipe_action_unpin),
                onAction = onAction,
            )
        } else {
            PinChannel(
                channel = channel,
                label = resources.getString(R.string.stream_compose_swipe_action_pin),
                onAction = onAction,
            )
        }

        val candidates: List<ChannelAction?> = if (isDM) {
            listOf(archiveAction, muteAction, pinAction)
        } else {
            listOf(muteAction, archiveAction, pinAction)
        }

        resolved = candidates.firstOrNull { it != null }
        resolved
    }
}
