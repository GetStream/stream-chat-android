package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import android.content.Intent
import android.os.Build

public class NotificationHandlerFactory(
    public val newMessageIntent: ((messageId: String, channelType: String, channelId: String) -> Intent)? = null
){

    /**
     * Method that create a [NotificationHandler].
     *
     * @param context The [Context] to build the [NotificationHandler] with.
     */
    public fun createNotificationHandler(context: Context): NotificationHandler {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MessagingStyleNotificationHandler(context, getNewMessageIntent(context))
        } else {
            ChatNotificationHandler(context, getNewMessageIntent(context))
        }
    }

    private fun getNewMessageIntent(context: Context): (messageId: String, channelType: String, channelId: String) -> Intent {
        return newMessageIntent
            ?: { _, _, _ -> createDefaultNewMessageIntent(context) }
    }

    private fun createDefaultNewMessageIntent(context: Context): Intent =
        context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
}
