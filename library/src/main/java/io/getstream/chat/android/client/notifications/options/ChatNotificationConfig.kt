package io.getstream.chat.android.client.notifications.options

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener


open class ChatNotificationConfig(val context: Context) {

    open fun onChatEvent(event: ChatEvent) {

    }

    open fun onFirebaseMessage(message: RemoteMessage) {

    }

    open fun getDeviceRegisteredListener(): DeviceRegisteredListener? {
        return null
    }

    open fun getDataLoadListener(): NotificationLoadDataListener? {
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
        val drawable = context.resources.getDrawable(R.drawable.stream_ic_notification, context.theme)
        return drawableToBitmap(drawable)
    }

    open fun getRequestCode(): Int {
        return 1220999987
    }

    open fun getFirebaseMessageIdKey(): String {
        return "stream-chat-message-id"
    }

    open fun getFirebaseChannelIdKey(): String {
        return "stream-chat-channel-id"
    }

    open fun getFirebaseChannelTypeKey(): String {
        return "stream-chat-channel-type"
    }

    private fun getDefaultContentIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            getRequestCode(),
            getLauncherIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


}