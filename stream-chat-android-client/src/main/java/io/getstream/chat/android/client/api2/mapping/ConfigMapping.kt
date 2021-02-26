package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.models.Config

internal fun ConfigDto.toDomain(): Config =
    Config(
        created_at = created_at,
        updated_at = updated_at,
        isTypingEvents = typing_events,
        isReadEvents = read_events,
        isConnectEvents = connect_events,
        isSearch = search,
        isReactionsEnabled = reactions,
        isRepliesEnabled = replies,
        isMutes = mutes,
        maxMessageLength = max_message_length,
        automod = automod,
        infinite = infinite,
        name = name,
        commands = commands.map(CommandDto::toDomain),
    )
