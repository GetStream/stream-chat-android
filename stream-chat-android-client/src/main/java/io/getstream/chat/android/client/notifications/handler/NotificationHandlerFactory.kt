package io.getstream.chat.android.client.notifications.handler

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import io.getstream.chat.android.client.R

/**
 * Factory for default [NotificationHandler].
 * Use it to customize an intent the user triggers when clicking on a notification.
 */
public object NotificationHandlerFactory {

    /**
     * Method that creates a [NotificationHandler].
     *
     * @param context The [Context] to build the [NotificationHandler] with.
     * @param newMessageIntent Lambda expression used to generate an [Intent] to open your app
     * @param notificationChannel Lambda expression used to generate a [NotificationChannel]. Used in SDK_INT >= VERSION_CODES.O.
     */
    @SuppressLint("NewApi")
    public fun createNotificationHandler(
        context: Context,
        newMessageIntent: ((messageId: String, channelType: String, channelId: String) -> Intent)? = null,
        notificationChannel: (() -> NotificationChannel)? = null,
    ): NotificationHandler {
        val notificationChannelFun = notificationChannel ?: getDefaultNotificationChannel(context)
        (newMessageIntent ?: getDefaultNewMessageIntentFun(context)).let { newMessageIntentFun ->
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MessagingStyleNotificationHandler(context, newMessageIntentFun, notificationChannelFun)
            } else {
                ChatNotificationHandler(context, newMessageIntentFun, notificationChannelFun)
            }
        }
    }

    private fun getDefaultNewMessageIntentFun(context: Context): (messageId: String, channelType: String, channelId: String) -> Intent {
        return { _, _, _ -> createDefaultNewMessageIntent(context) }
    }

    private fun createDefaultNewMessageIntent(context: Context): Intent =
        context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDefaultNotificationChannel(context: Context): (() -> NotificationChannel) {
        return {
            NotificationChannel(
                context.getString(R.string.stream_chat_notification_channel_id),
                context.getString(R.string.stream_chat_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        }
    }
}
