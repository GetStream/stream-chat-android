package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import android.content.Intent
import android.os.Build

public object NotificationHandlerFactory {

    /**
     * Method that create a [NotificationHandler].
     *
     * @param context The [Context] to build the [NotificationHandler] with.
     * @param newMessageIntent Lambda expression used to generate an [Intent] to open your app
     */
    public fun createNotificationHandler(
        context: Context,
        newMessageIntent: ((messageId: String, channelType: String, channelId: String) -> Intent)? = null
    ): NotificationHandler {
        (newMessageIntent ?: getDefaultNewMessageIntentFun(context)).let {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MessagingStyleNotificationHandler(context, it)
            } else {
                ChatNotificationHandler(context, it)
            }
        }
    }

    private fun getDefaultNewMessageIntentFun(context: Context): (messageId: String, channelType: String, channelId: String) -> Intent {
        return { _, _, _ -> createDefaultNewMessageIntent(context) }
    }

    private fun createDefaultNewMessageIntent(context: Context): Intent =
        context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
}
