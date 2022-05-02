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

package io.getstream.logging

/**
 * Low-level Logger interface.
 */
public interface StreamLogger {

    /**
     * Low-level logging call.
     *
     * @param priority The priority/type of this log message.
     * @param tag Used to identify the source of a log message.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     *
     * @see Priority
     * @see java.util.Formatter
     */
    public fun log(priority: Priority, tag: String, message: String, throwable: Throwable? = null)
}

/**
 * The priority/type of a log message.
 */
public enum class Priority(
    public val level: Int,
) {
    /** Priority for the [StreamLogger.log] method; use [StreamLog.v]. */
    VERBOSE(level = 2),

    /** Priority for the [StreamLogger.log] method; use [StreamLog.d]. */
    DEBUG(level = 3),

    /** Priority for the [StreamLogger.log] method; use [StreamLog.i]. */
    INFO(level = 4),

    /** Priority for the [StreamLogger.log] method; use [StreamLog.w]. */
    WARN(level = 5),

    /** Priority for the [StreamLogger.log] method; use [StreamLog.e]. */
    ERROR(level = 6),

    /** Priority for the [StreamLogger.log] method. */
    ASSERT(level = 7),
}

/**
 * Mock [StreamLogger] implementation for release builds.
 */
public object SilentStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {}
}
