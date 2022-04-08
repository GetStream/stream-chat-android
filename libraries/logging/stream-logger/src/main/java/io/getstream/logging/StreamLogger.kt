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

public typealias Priority = Int

/**
 * Low-level Logger interface.
 */
public interface StreamLogger {

    public companion object Level {
        /** Priority constant for the [log] method; use [StreamLog.v]. */
        public const val VERBOSE: Priority = 2
        /** Priority constant for the [log] method; use [StreamLog.d]. */
        public const val DEBUG: Priority = 3
        /** Priority constant for the [log] method; use [StreamLog.i]. */
        public const val INFO: Priority = 4
        /** Priority constant for the [log] method; use [StreamLog.w]. */
        public const val WARN: Priority = 5
        /** Priority constant for the [log] method; use [StreamLog.e]. */
        public const val ERROR: Priority = 6
        /** Priority constant for the [log] method. */
        public const val ASSERT: Priority = 7
    }

    /**
     * Low-level logging call.
     *
     * @param priority The priority/type of this log message.
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     * @param throwable An exception to log.
     *
     * @see Priority
     * @see java.util.Formatter
     */
    public fun log(priority: Priority, tag: String, message: () -> String, throwable: Throwable? = null)
}

/**
 * Mock [StreamLogger] implementation for release builds.
 */
public object SilentStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, message: () -> String, throwable: Throwable?) {}
}
