package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class BannedUserResponse(
    val user: DownstreamUserDto,
    val banned_by: DownstreamUserDto?,
    val channel: DownstreamChannelDto?,
    val created_at: Date?,
    val expires: Date?,
    val shadow: Boolean,
    val reason: String?,
)
