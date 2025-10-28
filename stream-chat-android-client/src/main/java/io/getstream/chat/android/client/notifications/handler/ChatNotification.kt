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

package io.getstream.chat.android.client.notifications.handler

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message

/**
 * Represents a chat notification with a specific type.
 */
public sealed class ChatNotification {

    /**
     * Notification for a new message in a channel.
     *
     * @property channel The channel where the new message was sent.
     * @property message The new message that triggered the notification.
     */
    public data class MessageNew(
        public val channel: Channel,
        public val message: Message,
    ) : ChatNotification()

    /**
     * Notification for an updated message in a channel.
     *
     * @property channel The channel where the message was updated.
     * @property message The updated message that triggered the notification.
     */
    public data class MessageUpdated(
        public val channel: Channel,
        public val message: Message,
    ) : ChatNotification()

    /**
     * Notification for a new reaction added to a message in a channel.
     *
     * @property title The default title of the notification as received in the PN.
     * @property body The default body of the notification as received in the PN.
     * @property type The type of reaction (e.g., "like", "love", etc.).
     * @property reactionUserId The ID of the user who added the reaction.
     * @property reactionUserImageUrl The (optional) image URL of the user who added the reaction.
     * @property channel The channel where the reaction was added.
     * @property message The message that received the new reaction.
     */
    public data class ReactionNew(
        public val title: String,
        public val body: String,
        public val type: String,
        public val reactionUserId: String,
        public val reactionUserImageUrl: String?,
        public val channel: Channel,
        public val message: Message,
    ) : ChatNotification()

    /**
     * Notification for a reminder due in a channel.
     *
     * @property channel The channel where the message was sent.
     * @property message The message associated with the reminder.
     */
    public data class NotificationReminderDue(
        public val channel: Channel,
        public val message: Message,
    ) : ChatNotification()

    public companion object {

        /**
         * Type for a new message notification.
         */
        public const val TYPE_MESSAGE_NEW: String = "message.new"

        /**
         * Type for a message update notification.
         */
        public const val TYPE_MESSAGE_UPDATED: String = "message.updated"

        /**
         * Type for a new reaction notification.
         */
        public const val TYPE_REACTION_NEW: String = "reaction.new"

        /**
         * Type for a notification indicating a reminder is due.
         */
        public const val TYPE_NOTIFICATION_REMINDER_DUE: String = "notification.reminder_due"

        /**
         * Creates a [ChatNotification] instance based on the provided payload, channel, and message.
         *
         * @param type The type of notification to create (if all relevant data is available).
         * @param payload The notification payload containing relevant data.
         * @param channel The channel associated with the notification.
         * @param message The message associated with the notification.
         * @return A [ChatNotification] instance if the type is recognized; otherwise, null.
         */
        internal fun create(
            type: String,
            payload: Map<String, Any?>,
            channel: Channel,
            message: Message,
        ): ChatNotification? = when (type) {
            TYPE_MESSAGE_NEW -> MessageNew(channel, message)
            TYPE_MESSAGE_UPDATED -> MessageUpdated(channel, message)
            TYPE_NOTIFICATION_REMINDER_DUE -> NotificationReminderDue(channel, message)
            TYPE_REACTION_NEW -> createReactionNewNotification(payload, channel, message)
            else -> null // Unknown notification type
        }

        @Suppress("ReturnCount")
        private fun createReactionNewNotification(
            payload: Map<String, Any?>,
            channel: Channel,
            message: Message,
        ): ReactionNew? {
            val title = payload["title"] as? String ?: return null
            val body = payload["body"] as? String ?: return null
            val reactionType = payload["reaction_type"] as? String ?: return null
            val reactionUserId = payload["reaction_user_id"] as? String ?: return null
            val reactionUserImageUrl = payload["reaction_user_image"] as? String?
            return ReactionNew(title, body, reactionType, reactionUserId, reactionUserImageUrl, channel, message)
        }
    }
}
