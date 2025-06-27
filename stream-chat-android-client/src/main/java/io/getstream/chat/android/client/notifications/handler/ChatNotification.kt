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
         * Type for a notification indicating a reminder is due.
         */
        public const val TYPE_NOTIFICATION_REMINDER_DUE: String = "notification.reminder_due"
    }
}
