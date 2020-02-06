package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.utils.FilterObject


data class SearchMessagesRequest(
    val query: String,
    val offset: Int,
    val limit: Int,
    @SerializedName("filter_conditions")
    val filter: FilterObject = FilterObject()
)

