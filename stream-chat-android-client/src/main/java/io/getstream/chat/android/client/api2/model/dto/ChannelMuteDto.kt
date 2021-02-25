package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class DownstreamChannelMuteDto(
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
    val created_at: Date,
)
