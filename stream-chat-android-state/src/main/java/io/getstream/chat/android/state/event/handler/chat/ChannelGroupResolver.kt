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

/**
 * Resolves the set of group keys carried by an event for the purposes of grouped channel lists
 * driven by `queryGroupedChannels`.
 *
 * Used by [GroupAwareChatEventHandler] to decide whether an incoming event-bearing channel
 * should be added to, removed from, or skipped by a query identified by
 * [io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier.Grouped].
 * The classification is read from `event.channelCustom` rather than `channel.extraData` because
 * the cached/event-bearing channel can lag the server while the event itself carries the
 * authoritative custom map.
 */
internal fun interface ChannelGroupResolver {

    /**
     * @param channelCustom The `channel_custom` map from the inbound event, or `null` when the
     * event does not carry one.
     * @param currentGroup The group key of the query asking. Most resolvers will not need this,
     * but it allows a single resolver instance to be shared across multiple grouped queries and
     * still differentiate behavior per asker (e.g. logging, short-circuiting, per-group rules).
     * @return The set of group keys this channel belongs to. A channel can belong to multiple
     * groups (e.g. an explicit group plus an `"all"` sentinel).
     */
    fun resolve(channelCustom: Map<String, Any>?, currentGroup: String): Set<String>
}
