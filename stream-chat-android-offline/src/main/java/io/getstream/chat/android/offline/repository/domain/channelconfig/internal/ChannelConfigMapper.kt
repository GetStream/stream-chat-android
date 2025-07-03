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

package io.getstream.chat.android.offline.repository.domain.channelconfig.internal

import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config

internal fun ChannelConfig.toEntity(): ChannelConfigEntity = ChannelConfigEntity(
    channelConfigInnerEntity = with(config) {
        ChannelConfigInnerEntity(
            channelType = this@toEntity.type,
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            isTypingEvents = typingEventsEnabled,
            isReadEvents = readEventsEnabled,
            isConnectEvents = connectEventsEnabled,
            isSearch = searchEnabled,
            isReactionsEnabled = isReactionsEnabled,
            isThreadEnabled = isThreadEnabled,
            isMutes = muteEnabled,
            uploadsEnabled = uploadsEnabled,
            urlEnrichmentEnabled = urlEnrichmentEnabled,
            customEventsEnabled = customEventsEnabled,
            pushNotificationsEnabled = pushNotificationsEnabled,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior,
            messageRemindersEnabled = messageRemindersEnabled,
        )
    },
    commands = config.commands.map { it.toEntity(type) },
)

internal fun ChannelConfigEntity.toModel(): ChannelConfig = ChannelConfig(
    channelConfigInnerEntity.channelType,
    with(channelConfigInnerEntity) {
        Config(
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            typingEventsEnabled = isTypingEvents,
            readEventsEnabled = isReadEvents,
            connectEventsEnabled = isConnectEvents,
            searchEnabled = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isThreadEnabled = isThreadEnabled,
            muteEnabled = isMutes,
            uploadsEnabled = uploadsEnabled,
            urlEnrichmentEnabled = urlEnrichmentEnabled,
            customEventsEnabled = customEventsEnabled,
            pushNotificationsEnabled = pushNotificationsEnabled,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior,
            commands = commands.map(CommandInnerEntity::toModel),
            messageRemindersEnabled = messageRemindersEnabled,
        )
    },
)

private fun CommandInnerEntity.toModel() = Command(
    name = name,
    description = description,
    args = args,
    set = set,
)

private fun Command.toEntity(channelType: String) = CommandInnerEntity(
    name = name,
    description = description,
    args = args,
    set = set,
    channelType = channelType,
)
