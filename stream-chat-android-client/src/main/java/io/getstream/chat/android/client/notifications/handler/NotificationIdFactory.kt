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

package io.getstream.chat.android.client.notifications.handler

/**
 * Factory for creating custom notification IDs based on the [ChatNotification] type.
 *
 * Implement this interface to customize how notification IDs are generated for different types of
 * chat notifications. This allows you to control notification grouping behavior.
 *
 * If [getNotificationId] returns `null`, the SDK will use its default notification ID generation logic.
 *
 * Example usage:
 * ```kotlin
 * val notificationIdFactory = NotificationIdFactory { notification ->
 *     if (notification is ChatNotification.MessageNew && notification.message.isSystem()) {
 *         // Use a stable hash code for system messages to avoid default grouping by channel
 *         "system:${notification.message.id}".hashCode()
 *     } else {
 *         // Use default SDK behavior for other notification types
 *         null
 *     }
 * }
 * ```
 */
public fun interface NotificationIdFactory {

    /**
     * Generates a notification ID for the given [ChatNotification].
     *
     * @param notification The chat notification to generate an ID for.
     * @return A unique integer ID for the notification, or `null` to use the SDK's default notification ID
     * generation logic.
     */
    public fun getNotificationId(notification: ChatNotification): Int?
}
