package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.PartialUpdateUserDto

@JsonClass(generateAdapter = true)
internal data class PartialUpdateUsersRequest(val users: List<PartialUpdateUserDto>)
