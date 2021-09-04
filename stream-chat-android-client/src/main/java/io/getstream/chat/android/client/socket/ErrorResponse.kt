package io.getstream.chat.android.client.socket

public data class ErrorResponse(
    val code: Int = -1,
    var message: String = "",
    var statusCode: Int = -1,
) {
    var duration: String = ""
}
