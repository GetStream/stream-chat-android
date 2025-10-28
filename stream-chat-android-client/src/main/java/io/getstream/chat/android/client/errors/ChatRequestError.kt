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

import java.io.IOException

/**
 * Used to interrupt okhttp request.
 * Only descendant [IOException] of can propagate call execution
 */
internal class ChatRequestError(
    message: String,
    val streamCode: Int,
    val statusCode: Int,
    cause: Throwable? = null,
) : IOException(message, cause) {
    override fun toString(): String = "streamCode: $streamCode, statusCode: $statusCode, message: $message"
}
