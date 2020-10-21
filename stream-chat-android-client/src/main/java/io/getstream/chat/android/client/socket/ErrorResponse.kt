package io.getstream.chat.android.client.socket

import com.google.gson.annotations.SerializedName

public data class ErrorResponse(
    val code: Int = -1,
    var message: String = "",
    @SerializedName("StatusCode")
    var statusCode: Int = -1
) {
    var duration: String = ""
}
