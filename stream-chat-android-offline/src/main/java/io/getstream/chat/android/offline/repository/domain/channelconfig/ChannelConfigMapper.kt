package io.getstream.chat.android.offline.repository.domain.channelconfig

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.model.ChannelConfig

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
            isRepliesEnabled = isRepliesEnabled,
            isMutes = muteEnabled,
            uploadsEnabled = uploadsEnabled,
            urlEnrichmentEnabled = urlEnrichmentEnabled,
            customEventsEnabled = customEventsEnabled,
            pushNotificationsEnabled = pushNotificationsEnabled,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior
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
            isRepliesEnabled = isRepliesEnabled,
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
        )
    }
)

private fun CommandInnerEntity.toModel() = Command(
    name = name,
    description = description,
    args = args,
    set = set
)

private fun Command.toEntity(channelType: String) = CommandInnerEntity(
    name = name,
    description = description,
    args = args,
    set = set,
    channelType = channelType
)
