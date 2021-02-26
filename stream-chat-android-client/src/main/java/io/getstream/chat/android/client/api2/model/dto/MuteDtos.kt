package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamMuteDto(
    val user: UpstreamUserDto,
    val target: UpstreamUserDto,
    val created_at: Date,
    val updated_at: Date,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamMuteDto(
    val user: DownstreamUserDto,
    val target: DownstreamUserDto,
    val created_at: Date,
    val updated_at: Date,
)
