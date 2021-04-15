package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage

internal fun SocketErrorResponse.toDomain(): SocketErrorMessage {
    return SocketErrorMessage(
        error = error?.toDomain()
    )
}

internal fun SocketErrorResponse.ErrorResponse.toDomain(): ErrorResponse {
    val dto = this
    return ErrorResponse(
        code = dto.code,
        message = dto.message,
        statusCode = dto.StatusCode,
    ).apply {
        duration = dto.duration
    }
}
