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

package io.getstream.chat.android.client.logger

import io.getstream.log.IsLoggableValidator
import io.getstream.log.Priority

/**
 * Validates if a message can be logged in accordance with the provided [logLevel].
 *
 * @see ChatLogLevel
 */
internal class StreamLogLevelValidator(
    private val logLevel: ChatLogLevel,
) : IsLoggableValidator {

    /**
     * Validates [priority] and [tag] of a message you would like logged.
     */
    override fun isLoggable(priority: Priority, tag: String): Boolean = when (logLevel) {
        ChatLogLevel.NOTHING -> false
        ChatLogLevel.ALL -> true
        ChatLogLevel.DEBUG -> priority.level >= Priority.DEBUG.level
        ChatLogLevel.WARN -> priority.level >= Priority.WARN.level
        ChatLogLevel.ERROR -> priority.level >= Priority.ERROR.level
    }
}
