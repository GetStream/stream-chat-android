package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto

@JsonClass(generateAdapter = true)
internal data class UpdateUsersRequest(
    val users: Map<String, UpstreamUserDto>,
)
