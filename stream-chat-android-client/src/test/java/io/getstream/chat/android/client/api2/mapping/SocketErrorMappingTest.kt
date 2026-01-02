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

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.socket.ErrorDetail
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class SocketErrorMappingTest {

    @Test
    fun `SocketErrorResponse is correctly mapped to SocketErrorMessage`() {
        val dto = Mother.randomSocketErrorResponse()
        val expected = SocketErrorMessage(
            error = ErrorResponse(
                code = dto.error!!.code,
                message = dto.error.message,
                statusCode = dto.error.StatusCode,
                exceptionFields = dto.error.exception_fields,
                moreInfo = dto.error.more_info,
                details = dto.error.details.map {
                    ErrorDetail(
                        code = it.code,
                        messages = it.messages,
                    )
                },
            ).apply {
                duration = dto.error.duration
            },
        )
        val socketErrorMessage = dto.toDomain()
        socketErrorMessage shouldBeEqualTo expected
    }
}
