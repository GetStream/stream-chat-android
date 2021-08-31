package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.SearchWarningDto

@JsonClass(generateAdapter = true)
internal data class SearchMessagesResponse(
    val results: List<MessageResponse>,
    val next: String?,
    val previous: String?,
    val resultsWarning: SearchWarningDto?,
)
