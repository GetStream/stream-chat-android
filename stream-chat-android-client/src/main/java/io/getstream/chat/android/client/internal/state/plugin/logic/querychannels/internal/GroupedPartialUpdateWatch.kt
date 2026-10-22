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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.client.internal.state.utils.internal.mergePartialUpdate
import io.getstream.chat.android.models.Channel

/**
 * Turns a [EventHandlingResult.WatchAndAdd] for a partial channel update into [EventHandlingResult.Skip] when the
 * channel cannot be shown in the grouped list of [state] without the pages before it. A channel below the loaded
 * channels is shown when its page loads, so a partial update on an old channel does not watch it. Other results and
 * events are returned unchanged.
 *
 * @param event The event that produced this result.
 * @param cachedChannel The local copy of the event's channel, or `null` when there is none.
 * @param state The state of the grouped query.
 */
internal fun EventHandlingResult.skipWatchBelowLoadedChannels(
    event: ChatEvent,
    cachedChannel: Channel?,
    state: QueryChannelsState,
): EventHandlingResult {
    if (this !is EventHandlingResult.WatchAndAdd || event !is NotificationChannelPartialUpdatedEvent) return this
    val channel = cachedChannel?.mergePartialUpdate(event.channel) ?: event.channel
    return if (state.isWithinLoadedChannels(channel)) this else EventHandlingResult.Skip
}

/**
 * Drops the watch from a [EventHandlingResult.WatchAndAdd] for a partial channel update when the grouped list was not
 * queried with `watch`. A channel with a local copy is added from that copy with the update applied. A channel without
 * one is skipped and shown when its page loads. Other results and events are returned unchanged.
 *
 * @param event The event that produced this result.
 * @param cachedChannel The local copy of the event's channel, or `null` when there is none.
 * @param watch Whether the grouped list was queried with `watch`.
 */
internal fun EventHandlingResult.withoutWatchUnlessListWatches(
    event: ChatEvent,
    cachedChannel: Channel?,
    watch: Boolean,
): EventHandlingResult {
    if (this !is EventHandlingResult.WatchAndAdd || event !is NotificationChannelPartialUpdatedEvent) return this
    if (watch) return this
    return cachedChannel?.mergePartialUpdate(event.channel)?.let(EventHandlingResult::Add) ?: EventHandlingResult.Skip
}

/**
 * Whether [channel] sorts within the loaded channels: the group has no more pages, or the channel sorts at or above
 * the last loaded channel. Without local data the channel has no last message time, so its position is a lower bound:
 * a channel with recent activity but no local copy can be skipped, and it then shows after the next refresh.
 */
private fun QueryChannelsState.isWithinLoadedChannels(channel: Channel): Boolean {
    if (endOfChannels.value) return true
    val lastLoaded = channels.value?.lastOrNull() ?: return true
    return sort.comparator.compare(channel, lastLoaded) <= 0
}
