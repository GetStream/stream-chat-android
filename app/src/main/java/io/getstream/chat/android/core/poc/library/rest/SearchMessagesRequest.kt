package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.FilterObject
import java.util.logging.Filter


data class SearchMessagesRequest(
    val query: String,
    val offset: Int,
    val limit: Int,
    @SerializedName("filter_conditions")
    val filter: FilterObject = FilterObject()
)

