package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto

@JsonClass(generateAdapter = true)
internal data class RemoveMembersRequest(
    val remove_members: List<String>,
    val message: UpstreamMessageDto?,
)
