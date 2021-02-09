package io.getstream.chat.android.livedata.repository.domain.channelconfig

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.model.ChannelConfig

internal fun ChannelConfig.toEntity(): ChannelConfigEntity = ChannelConfigEntity(
    channelConfigInnerEntity = with(config) {
        ChannelConfigInnerEntity(
            channelType = this@toEntity.type,
            createdAt = created_at,
            updatedAt = updated_at,
            isTypingEvents = isTypingEvents,
            isReadEvents = isReadEvents,
            isConnectEvents = isConnectEvents,
            isSearch = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isRepliesEnabled = isRepliesEnabled,
            isMutes = isMutes,
            maxMessageLength = maxMessageLength,
            automod = automod,
            infinite = infinite,
            name = name,
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
            isTypingEvents = isTypingEvents,
            isReadEvents = isReadEvents,
            isConnectEvents = isConnectEvents,
            isSearch = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isRepliesEnabled = isRepliesEnabled,
            isMutes = isMutes,
            maxMessageLength = maxMessageLength,
            automod = automod,
            infinite = infinite,
            name = name,
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
