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

package io.getstream.chat.android.state.event.handler.chat

import io.getstream.chat.android.models.Channel

/**
 * Resolves the set of group keys a [Channel] belongs to for the purposes of grouped channel
 * lists driven by `queryGroupedChannels`.
 *
 * Used by [GroupAwareChatEventHandler] to decide whether an incoming event-bearing channel
 * should be added to, removed from, or skipped by a query identified by
 * [io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier.Grouped].
 * Classification is performed against the channel's own `extraData`.
 */
internal fun interface ChannelGroupResolver {

    /**
     * @param channel The channel whose group membership is being resolved.
     * @param currentGroup The group key of the query asking. Most resolvers will not need this,
     * but it allows a single resolver instance to be shared across multiple grouped queries and
     * still differentiate behavior per asker (e.g. logging, short-circuiting, per-group rules).
     * @return The set of group keys this channel belongs to. A channel can belong to multiple
     * groups (e.g. an explicit group plus an `"all"` sentinel).
     */
    fun resolve(channel: Channel, currentGroup: String): Set<String>
}
