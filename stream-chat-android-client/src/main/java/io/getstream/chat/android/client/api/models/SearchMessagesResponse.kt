package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.SearchWarning

internal data class SearchMessagesResponse(
    val results: List<MessageResponse> = emptyList(),
    val next: String?,
    val previous: String?,
    @SerializedName("results_warning")
    val resultsWarning: SearchWarning?,
)
