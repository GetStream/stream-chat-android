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

import io.getstream.chat.android.client.api2.model.dto.ErrorDto
import io.getstream.chat.android.client.errors.ChatError

// `APIError.details` is wire-typed `List<Int>` per the generated spec (Go's
// `DetailsOrArray` always marshals to `[]`), so the historic structured-detail
// path is dropped: ChatError.details stays at its default empty list.
internal fun ErrorDto.toDomain(): ChatError {
    val dto = this
    return ChatError(
        code = dto.code,
        message = dto.message,
        statusCode = dto.statusCode,
        exceptionFields = dto.exceptionFields.orEmpty(),
        moreInfo = dto.moreInfo,
    ).apply {
        duration = dto.duration
    }
}
