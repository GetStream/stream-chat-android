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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.core.internal.StreamHandsOff

@JsonClass(generateAdapter = true)
internal data class SocketErrorResponse(
    val error: ErrorResponse? = null,
) {

    @StreamHandsOff(
        reason = "Field `StatusCode` name is right, even when it doesn't follow camelCase nor snake_case rules",
    )
    @JsonClass(generateAdapter = true)
    data class ErrorResponse(
        val code: Int = -1,
        val message: String = "",
        val StatusCode: Int = -1,
        val duration: String = "",
        val exception_fields: Map<String, String> = mapOf(),
        val more_info: String = "",
        val details: List<ErrorDetail> = emptyList(),
    ) {

        @JsonClass(generateAdapter = true)
        data class ErrorDetail(
            val code: Int = -1,
            val messages: List<String> = emptyList(),
        )
    }
}
