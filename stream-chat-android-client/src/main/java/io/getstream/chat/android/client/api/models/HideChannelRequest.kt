package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class HideChannelRequest(
    @SerializedName("clear_history")
    val clearHistory: Boolean = false,
)
