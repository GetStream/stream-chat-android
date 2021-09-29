package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import android.content.Intent
import android.os.Build

public object NotificationHandlerFactory {

    public var newMessageIntent: ((messageId: String, channelType: String, channelId: String) -> Intent)? = null

    public fun createNotificationHandler(context: Context, notificationConfig: NotificationConfig): NotificationHandler {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MessagingStyleNotificationHandler(context, notificationConfig, getNewMessageIntent(context))
        } else {
            ChatNotificationHandler(context, notificationConfig, getNewMessageIntent(context))
        }
    }

    private fun getNewMessageIntent(context: Context): (messageId: String, channelType: String, channelId: String) -> Intent {
        return newMessageIntent
            ?: { _, _, _ -> createDefaultNewMessageIntent(context) }
    }

    private fun createDefaultNewMessageIntent(context: Context): Intent =
        context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
}
