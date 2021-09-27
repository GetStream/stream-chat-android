package io.getstream.chat.android.client.notifications.handler

import android.content.Context

internal object NotificationHandlerFactory {

    internal fun createNotificationHandler(context: Context, notificationConfig: NotificationConfig): NotificationHandler {
        return ChatNotificationHandler(context, notificationConfig)
    }
}
