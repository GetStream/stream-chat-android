/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

import java.util.Date

/**
 * Represents the push notification preference for a specific user or channel.
 *
 * @param level The chat level preference for notifications. Possible values are:
 * - [PushPreferenceLevel.ALL]: Receive notifications for all messages.
 * - [PushPreferenceLevel.MENTIONS]: Receive notifications only for mentions.
 * - [PushPreferenceLevel.NONE]: Do not receive any notifications.
 * @param disabledUntil Timestamp until which notifications are disabled. If null, notifications are not disabled.
 */
public data class PushPreference(
    public val level: PushPreferenceLevel?,
    public val disabledUntil: Date?,
)

/**
 * Represents the possible levels for chat push notifications.
 *
 * @param value The string representation of the chat level.
 */
public enum class PushPreferenceLevel(public val value: String) {
    ALL("all"),
    MENTIONS("mentions"),
    NONE("none"),
    ;

    public companion object Companion {

        /**
         * Returns the [PushPreferenceLevel] corresponding to the given string value.
         *
         * @param value The string representation of the chat level.
         * @return The corresponding [PushPreferenceLevel] or null if no match is found.
         */
        public fun fromValue(value: String?): PushPreferenceLevel? = entries.find { it.value == value }
    }
}
