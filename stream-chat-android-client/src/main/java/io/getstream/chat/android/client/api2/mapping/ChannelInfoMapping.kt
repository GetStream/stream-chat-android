package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.models.ChannelInfo

internal fun ChannelInfoDto.toDomain(): ChannelInfo =
    ChannelInfo(
        cid = cid,
        id = id,
        memberCount = member_count,
        name = name,
        type = type,
    )
