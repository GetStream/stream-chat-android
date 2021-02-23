package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.MuteDto
import io.getstream.chat.android.client.api2.model.dto.UserDto

@JsonClass(generateAdapter = true)
internal data class MuteUserResponse(
    val mute: MuteDto,
    val own_user: UserDto,
)
