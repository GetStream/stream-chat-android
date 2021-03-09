package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.models.Channel

internal fun DownstreamChannelDto.toDomain(): Channel =
    Channel(
        cid = cid,
        id = id,
        type = type,
        watcherCount = watcher_count,
        frozen = frozen,
        lastMessageAt = last_message_at,
        createdAt = created_at,
        deletedAt = deleted_at,
        updatedAt = updated_at,
        memberCount = member_count,
        messages = messages.map(DownstreamMessageDto::toDomain),
        members = members.map(DownstreamMemberDto::toDomain),
        watchers = watchers.map(DownstreamUserDto::toDomain),
        read = read.map(DownstreamChannelUserRead::toDomain),
        config = config.toDomain(),
        createdBy = created_by.toDomain(),
        team = team,
        cooldown = cooldown,
        pinnedMessages = pinned_messages.map(DownstreamMessageDto::toDomain),
        extraData = extraData.toMutableMap(),
    )
