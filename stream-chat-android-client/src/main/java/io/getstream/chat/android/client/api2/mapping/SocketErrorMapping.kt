/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.socket.ErrorDetail
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage

internal fun SocketErrorResponse.toDomain(): SocketErrorMessage {
    return SocketErrorMessage(
        error = error?.toDomain(),
    )
}

internal fun SocketErrorResponse.ErrorResponse.toDomain(): ErrorResponse {
    val dto = this
    return ErrorResponse(
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

internal fun SocketErrorResponse.ErrorResponse.ErrorDetail.toDomain(): ErrorDetail {
    val dto = this
    return ErrorDetail(
        code = dto.code,
        messages = dto.messages,
    )
}
