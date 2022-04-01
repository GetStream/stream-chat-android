/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
 
package io.getstream.chat.android.client.errors

public class ChatNetworkError private constructor(
    public val description: String,
    cause: Throwable? = null,
    public val streamCode: Int,
    public val statusCode: Int
) : ChatError(
    "Status code: $statusCode, with stream code: $streamCode, description: $description",
    cause
) {
    override fun toString(): String {
        return "ChatNetworkError http status $statusCode, stream error code $streamCode: $description"
    }

    public companion object {
        public fun create(
            code: ChatErrorCode,
            cause: Throwable? = null,
            statusCode: Int = -1
        ): ChatNetworkError {
            return ChatNetworkError(code.description, cause, code.code, statusCode)
        }

        public fun create(
            streamCode: Int,
            description: String,
            statusCode: Int,
            cause: Throwable? = null
        ): ChatNetworkError {
            return ChatNetworkError(description, cause, streamCode, statusCode)
        }
    }
}
