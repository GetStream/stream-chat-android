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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Describes the details of a message which was moderated.
 *
 * @param originalText The original text of the moderated message.
 * @param action The moderation action that was performed on the message.
 * @param errorMsg The error message that was returned by the moderation service.
 */
@Immutable
public data class MessageModerationDetails(
    val originalText: String,
    val action: MessageModerationAction,
    val errorMsg: String,
)

/**
 * The type of moderation performed to a message.
 *
 * @property rawValue The raw value of the moderation action.
 */
@Immutable
public data class MessageModerationAction(
    public val rawValue: String,
) {
    public companion object {

        /**
         * A bounced message means it needs to be rephrased and sent again.
         */
        public val bounce: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_BOUNCE",
        )

        /**
         * A flagged message means it was sent for review in the dashboard but the message was still published.
         */
        public val flag: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_FLAG",
        )

        /**
         * A blocked message means it was not published and it was sent for review in the dashboard.
         */
        public val block: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_BLOCK",
        )

        /**
         * A set of all the available [MessageModerationAction] values.
         */
        public val values: Set<MessageModerationAction> = setOf(bounce, flag, block)

        /**
         * Creates a [MessageModerationAction] from a raw value.
         */
        public fun fromRawValue(rawValue: String): MessageModerationAction = values.find {
            it.rawValue == rawValue
        } ?: MessageModerationAction(rawValue = rawValue)
    }
}
