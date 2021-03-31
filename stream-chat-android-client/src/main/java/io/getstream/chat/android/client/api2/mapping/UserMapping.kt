package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.User

internal fun User.toDto(): UpstreamUserDto =
    UpstreamUserDto(
        banned = banned,
        id = id,
        invisible = invisible,
        role = role,
        devices = devices.map(Device::toDto),
        teams = teams,
        extraData = extraData,
    )

internal fun DownstreamUserDto.toDomain(): User =
    User(
        id = id,
        role = role,
        invisible = invisible,
        banned = banned,
        devices = devices.orEmpty().map(DeviceDto::toDomain),
        online = online,
        createdAt = created_at,
        updatedAt = updated_at,
        lastActive = last_active,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        mutes = mutes.map(DownstreamMuteDto::toDomain),
        teams = teams,
        channelMutes = channel_mutes.map(DownstreamChannelMuteDto::toDomain),
        extraData = extraData.toMutableMap(),
    )
