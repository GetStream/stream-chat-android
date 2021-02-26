package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto

@JsonClass(generateAdapter = true)
internal data class MuteUserResponse(
    val mute: DownstreamMuteDto,
    val own_user: DownstreamUserDto,
)
