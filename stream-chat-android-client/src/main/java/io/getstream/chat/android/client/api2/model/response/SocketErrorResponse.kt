package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SocketErrorResponse(
    val error: ErrorResponse? = null,
) {

    @JsonClass(generateAdapter = true)
    data class ErrorResponse(
        val code: Int = -1,
        val message: String = "",
        val StatusCode: Int = -1,
        val duration: String = "",
    )
}
