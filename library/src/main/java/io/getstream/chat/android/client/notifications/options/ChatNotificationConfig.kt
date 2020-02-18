package io.getstream.chat.android.client.notifications.options

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationMessageLoadListener

open class ChatNotificationConfig(val context: Context) {

    open fun getDeviceRegisteredListener(): DeviceRegisteredListener? {
        return null
    }

    open fun getFailMessageListener(): NotificationMessageLoadListener? {
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    open fun getNotificationChannel(): NotificationChannel {
        val channel = NotificationChannel(
            getNotificationChannelId(),
            getNotificationChannelName(),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.setShowBadge(true)
        return channel
    }

    open fun getNotificationChannelId() =
        context.getString(R.string.stream_default_notification_channel_id)

    open fun getNotificationChannelName() =
        context.getString(R.string.stream_default_notification_channel_name)

    open fun getNotificationBuilder(): NotificationCompat.Builder {

        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setAutoCancel(true)
            .setSmallIcon(getSmallIcon())
            .setLargeIcon(getLargeIcon())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
    }

    open fun getLauncherIntent(): Intent? {
        return context.packageManager?.getLaunchIntentForPackage(context.packageName)
    }

    open fun getIntentForFirebaseMessage(remoteMessage: RemoteMessage): PendingIntent {
        return getDefaultContentIntent()
    }

    open fun getIntentForSocketEvent(event: ChatEvent): PendingIntent {
        return getDefaultContentIntent()
    }

    open fun getSmallIcon(): Int {
        return R.drawable.stream_ic_notification
    }

    open fun getLargeIcon(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.stream_ic_notification)
    }

    open fun getRequestCode(): Int {
        return 1220999987
    }

    open fun getFirebaseMessageKey(): String {
        return "stream-chat-message-id"
    }

    private fun getDefaultContentIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            getRequestCode(),
            getLauncherIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}