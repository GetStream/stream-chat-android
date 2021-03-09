package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto

@JsonClass(generateAdapter = true)
internal data class QueryMembersResponse(
    val members: List<DownstreamMemberDto>,
)
