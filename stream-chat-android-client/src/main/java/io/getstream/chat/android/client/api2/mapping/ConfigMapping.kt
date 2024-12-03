/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.models.Config

internal fun ConfigDto.toDomain(): Config =
    Config(
        createdAt = created_at,
        updatedAt = updated_at,
        name = name ?: "",
        typingEventsEnabled = typing_events,
        readEventsEnabled = read_events,
        connectEventsEnabled = connect_events,
        searchEnabled = search,
        isReactionsEnabled = reactions,
        isThreadEnabled = replies,
        muteEnabled = mutes,
        uploadsEnabled = uploads,
        urlEnrichmentEnabled = url_enrichment,
        customEventsEnabled = custom_events,
        pushNotificationsEnabled = push_notifications,
        skipLastMsgUpdateForSystemMsgs = skip_last_msg_update_for_system_msgs ?: false,
        pollsEnabled = polls,
        messageRetention = message_retention,
        maxMessageLength = max_message_length,
        automod = automod,
        automodBehavior = automod_behavior,
        blocklistBehavior = blocklist_behavior ?: "",
        commands = commands.map(CommandDto::toDomain),
    )
