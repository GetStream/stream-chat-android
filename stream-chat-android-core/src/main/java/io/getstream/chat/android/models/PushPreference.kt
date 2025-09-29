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
 * - [PushPreferenceLevel.all]: Receive notifications for all messages.
 * - [PushPreferenceLevel.mentions]: Receive notifications only for mentions.
 * - [PushPreferenceLevel.none]: Do not receive any notifications.
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
public data class PushPreferenceLevel(public val value: String) {

    public companion object {

        /**
         * Receive notifications for all messages.
         */
        public val all: PushPreferenceLevel = PushPreferenceLevel(value = "all")

        /**
         * Receive notifications only for mentions.
         */
        public val mentions: PushPreferenceLevel = PushPreferenceLevel(value = "mentions")

        /**
         * Do not receive any notifications.
         */
        public val none: PushPreferenceLevel = PushPreferenceLevel(value = "none")

        /**
         * Returns the [PushPreferenceLevel] corresponding to the given string value.
         *
         * @param value The string representation of the chat level.
         * @return The corresponding [PushPreferenceLevel] or null if no match is found.
         */
        public fun fromValue(value: String?): PushPreferenceLevel? = when (value) {
            all.value -> all
            mentions.value -> mentions
            none.value -> none
            null -> null
            else -> PushPreferenceLevel(value)
        }
    }
}
