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

package io.getstream.chat.android.client.logger

import io.getstream.log.Priority
import io.getstream.log.Priority.ASSERT
import io.getstream.log.Priority.DEBUG
import io.getstream.log.Priority.ERROR
import io.getstream.log.Priority.INFO
import io.getstream.log.Priority.VERBOSE
import io.getstream.log.Priority.WARN
import io.getstream.log.StreamLogger

/**
 * A connection layer between [StreamLogger] and [ChatLoggerHandler].
 */
internal class StreamLoggerHandler(
    private val handler: ChatLoggerHandler?,
) : StreamLogger {

    /**
     * Passes log messages to the specified [handler].
     */
    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        handler?.run {
            when (priority) {
                VERBOSE -> logV(tag, message)
                DEBUG -> logD(tag, message)
                INFO -> logI(tag, message)
                WARN -> logW(tag, message)
                ERROR, ASSERT -> when (throwable) {
                    null -> logE(tag, message)
                    else -> logE(tag, message, throwable)
                }
            }
        }
    }
}
