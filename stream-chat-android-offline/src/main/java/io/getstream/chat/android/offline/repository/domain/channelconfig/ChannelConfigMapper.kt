package io.getstream.chat.android.offline.repository.domain.channelconfig

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.offline.model.ChannelConfig

internal fun ChannelConfig.toEntity(): ChannelConfigEntity = ChannelConfigEntity(
    channelConfigInnerEntity = with(config) {
        ChannelConfigInnerEntity(
            channelType = this@toEntity.type,
            createdAt = created_at,
            updatedAt = updated_at,
            name = name,
            isTypingEvents = isTypingEvents,
            isReadEvents = isReadEvents,
            isConnectEvents = isConnectEvents,
            isSearch = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isRepliesEnabled = isRepliesEnabled,
            isMutes = isMutes,
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
            created_at = createdAt,
            updated_at = updatedAt,
            name = name,
            isTypingEvents = isTypingEvents,
            isReadEvents = isReadEvents,
            isConnectEvents = isConnectEvents,
            isSearch = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isRepliesEnabled = isRepliesEnabled,
            isMutes = isMutes,
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
