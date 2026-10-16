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

package io.getstream.chat.android.state.utils.internal

import io.getstream.chat.android.client.events.NotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData

/**
 * Channel response fields that the channel model does not declare, so they arrive in the extra data next to the
 * custom fields. A partial update carries only the custom fields and `disabled`, so the local values of the others
 * are kept.
 */
private val serverFieldsInExtraData = setOf(
    "auto_translation_enabled",
    "auto_translation_language",
    "blocked",
    "disabled",
    "hidden",
    "hide_messages_before",
    "mute_expires_at",
    "muted",
    "truncated_at",
    "truncated_by",
)

/**
 * Applies the fields carried by [NotificationChannelPartialUpdatedEvent.channel] to this channel and keeps all
 * the others, such as members, reads and [Channel.lastMessageAt].
 */
internal fun Channel.mergePartialUpdate(partial: Channel): Channel = copy(
    name = partial.name,
    image = partial.image,
    frozen = partial.frozen,
    team = partial.team,
    createdAt = partial.createdAt,
    updatedAt = partial.updatedAt,
    extraData = mergePartialExtraData(current = extraData, partial = partial.extraData),
)

/**
 * Applies the fields carried by [NotificationChannelPartialUpdatedEvent.channel] to this channel data and keeps
 * all the others.
 */
internal fun ChannelData.mergePartialUpdate(partial: Channel): ChannelData = copy(
    name = partial.name,
    image = partial.image,
    frozen = partial.frozen,
    team = partial.team,
    createdAt = partial.createdAt,
    updatedAt = partial.updatedAt,
    extraData = mergePartialExtraData(current = extraData, partial = partial.extraData),
)

// The partial update holds the complete custom data, so a custom field it lacks was removed.
private fun mergePartialExtraData(current: Map<String, Any>, partial: Map<String, Any>): Map<String, Any> =
    current.filterKeys(serverFieldsInExtraData::contains) + partial
