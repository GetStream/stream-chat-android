package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage

internal fun SocketErrorResponse.toDomain(): SocketErrorMessage {
    return SocketErrorMessage(
        error = error?.let {
            ErrorResponse(
                code = error.code,
                message = error.message,
                statusCode = error.StatusCode,
            ).apply {
                duration = error.duration
            }
        }
    )
}
