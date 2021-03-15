package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.models.Config

internal fun ConfigDto.toDomain(): Config =
    Config(
        created_at = created_at,
        updated_at = updated_at,
        name = name,
        isTypingEvents = typing_events,
        isReadEvents = read_events,
        isConnectEvents = connect_events,
        isSearch = search,
        isReactionsEnabled = reactions,
        isRepliesEnabled = replies,
        isMutes = mutes,
        isUploads = uploads,
        isUrlEnrichment = url_enrichment,
        isCustomEvents = custom_events,
        isPushNotifications = push_notifications,
        messageRetention = message_retention,
        maxMessageLength = max_message_length,
        automod = automod,
        automodBehavior = automod_behavior,
        blocklistBehavior = blocklist_behavior,
        commands = commands.map(CommandDto::toDomain),
    )
