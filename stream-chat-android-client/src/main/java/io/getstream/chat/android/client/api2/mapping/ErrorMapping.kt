package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ErrorDetailDto
import io.getstream.chat.android.client.api2.model.dto.ErrorDto
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorDetail

internal fun ErrorDto.toDomain(): ChatError {
    val dto = this
    return ChatError(
        code = dto.code,
        message = dto.message,
        statusCode = dto.StatusCode,
        exceptionFields = dto.exception_fields,
        moreInfo = dto.more_info,
        details = dto.details.map { it.toDomain() },
    ).apply {
        duration = dto.duration
    }
}

internal fun ErrorDetailDto.toDomain(): ChatErrorDetail {
    val dto = this
    return ChatErrorDetail(
        code = dto.code,
        messages = dto.messages,
    )
}