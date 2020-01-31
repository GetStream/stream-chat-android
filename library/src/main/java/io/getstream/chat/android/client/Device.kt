package io.getstream.chat.android.client

import com.google.gson.annotations.SerializedName


data class Device(
    @SerializedName("id")
    val id: String
) {
    @SerializedName("push_provider")
    var push_provider: String = ""
}
