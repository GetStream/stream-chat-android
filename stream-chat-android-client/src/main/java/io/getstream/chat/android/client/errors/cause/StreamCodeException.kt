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

package io.getstream.chat.android.client.errors.cause

import io.getstream.chat.android.client.errors.ChatErrorCode

/**
 * Exceptions hierarchy based on backed error codes.
 */
public sealed class StreamCodeException : StreamException {
    protected constructor() : super()
    protected constructor(message: String?) : super(message)
    protected constructor(message: String?, cause: Throwable?) : super(message, cause)
    protected constructor(cause: Throwable?) : super(cause)
}

/**
 * Corresponding exception to [ChatErrorCode.MESSAGE_MODERATION_FAILED].
 */
public data class MessageModerationFailedException(
    val details: List<Detail>,
    override val message: String? = null,
) : StreamCodeException(message) {

    public data class Detail(
        public val code: Int,
        public val messages: List<String>,
    )
}
