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
