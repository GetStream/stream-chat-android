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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.core.internal.ExcludeFromCoverageGeneratedReport
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message

@ExcludeFromCoverageGeneratedReport
@Composable
internal fun ChatPreviewTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    ChatClient.Builder(
        apiKey = "stream-api-key",
        appContext = context,
    ).notifications(
        notificationConfig = NotificationConfig(pushNotificationsEnabled = false),
        notificationsHandler = NoOpNotificationHandler(),
    ).build()

    ChatTheme { content.invoke() }
}

private class NoOpNotificationHandler : NotificationHandler {
    override fun showNotification(channel: Channel, message: Message) {
        // No-op
    }

    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        // No-op
    }

    override fun dismissAllNotifications() {
        // No-op
    }

    override fun onNotificationPermissionStatus(status: NotificationPermissionStatus) {
        // No-op
    }
}
