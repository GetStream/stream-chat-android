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

private const val ALL_SEVERITY = 0
private const val DEBUG_SEVERITY = 1
private const val WARN_SEVERITY = 2
private const val ERROR_SEVERITY = 3
private const val NOTHING_SEVERITY = 4

/**
 * An enumeration used for tracking which logs should be shown.
 */
public enum class ChatLogLevel(private val severity: Int) {
    /**
     * Show all Logs.
     */
    ALL(ALL_SEVERITY),

    /**
     * Show DEBUG, WARNING, ERROR logs
     */
    DEBUG(DEBUG_SEVERITY),

    /**
     * Show WARNING and ERROR logs
     */
    WARN(WARN_SEVERITY),

    /**
     * Show ERRORs only
     */
    ERROR(ERROR_SEVERITY),

    /**
     * Don't show any Logs.
     */
    NOTHING(NOTHING_SEVERITY),
    ;

    internal fun isMoreOrEqualsThan(level: ChatLogLevel): Boolean {
        return level.severity >= severity
    }
}
