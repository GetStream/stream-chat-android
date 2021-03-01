package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto

@JsonClass(generateAdapter = true)
internal data class UpdateUsersResponse(
    val users: Map<String, DownstreamUserDto>,
)
