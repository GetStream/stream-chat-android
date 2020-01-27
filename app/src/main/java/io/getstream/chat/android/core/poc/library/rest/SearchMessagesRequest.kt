package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.FilterObject


class SearchMessagesRequest(
    val query: String,
    @SerializedName("filter_conditions")
    val filter: FilterObject,
    val limit: Int,
    val offset: Int
)

