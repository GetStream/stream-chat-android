package io.getstream.chat.android.client.notifications.options

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.poc.R

class StreamNotificationOptions : NotificationOptions {

    private val TAG = StreamNotificationOptions::class.java.simpleName
    private val DEFAULT_REQUEST_CODE = 999

    private var notificationChannelId: String? = null
    private var notificationChannelName: String? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var usedDefaultSmallIcon = true
    private var usedDefaultLargeIcon = true
    private var smallIcon = R.drawable.stream_ic_notification
    private var largeIcon: Bitmap? = null
    private var notificationIntentProvider: NotificationIntentProvider? = null
    private var defaultLauncherIntent: Intent? = null
    private var notificationChannel: NotificationChannel? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun getNotificationChannel(context: Context): NotificationChannel? {
        return if (notificationChannel == null) {
            val channel = NotificationChannel(
                getNotificationChannelId(context),
                getNotificationChannelName(context),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(true)
            notificationChannel = channel
            notificationChannel
        } else {
            notificationChannel
        }
    }

    override fun getNotificationChannelId(context: Context) =
        notificationChannelId ?: context.getString(R.string.stream_default_notification_channel_id)


    override fun getNotificationChannelName(context: Context) =
        notificationChannelName
            ?: context.getString(R.string.stream_default_notification_channel_name)

    override fun getNotificationBuilder(context: Context): NotificationCompat.Builder? {
        return if (notificationBuilder == null) {
            checkForDefaultIcons(context)

            notificationBuilder =
                NotificationCompat.Builder(context, getNotificationChannelId(context))
                    .setAutoCancel(true)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(largeIcon)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationBuilder
        } else {
            notificationBuilder
        }
    }

    override fun getDefaultLauncherIntent(context: Context): Intent? {
        return defaultLauncherIntent
            ?: context.packageManager?.getLaunchIntentForPackage(context.packageName)
    }

    override fun getNotificationIntentProvider(): NotificationIntentProvider? {
        return if (notificationIntentProvider != null) {
            notificationIntentProvider
        } else {
            object : NotificationIntentProvider {
                override fun getIntentForFirebaseMessage(
                    context: Context,
                    remoteMessage: RemoteMessage
                ): PendingIntent {
                    return getDefaultContentIntent(context)
                }

                override fun getIntentForWebSocketEvent(
                    context: Context,
                    event: ChatEvent
                ): PendingIntent {
                    return getDefaultContentIntent(context)
                }
            }
        }
    }

    override fun setSmallIcon(iconRes: Int) {
        usedDefaultSmallIcon = false
        smallIcon = iconRes
    }

    override fun setLargeIcon(icon: Bitmap?) {
        usedDefaultLargeIcon = false
        largeIcon = icon
    }

    override fun setNotificationChannelId(notificationChannelId: String?) {
        this.notificationChannelId = notificationChannelId
    }

    override fun setNotificationChannelName(notificationChannelName: String?) {
        this.notificationChannelName = notificationChannelName
    }

    override fun setNotificationChannel(notificationChannel: NotificationChannel) {
        this.notificationChannel = notificationChannel
    }

    override fun setNotificationBuilder(notificationBuilder: NotificationCompat.Builder?) {
        this.notificationBuilder = notificationBuilder
    }

    override fun setDefaultLauncherIntent(defaultLauncherIntent: Intent?) {
        this.defaultLauncherIntent = defaultLauncherIntent
    }

    override fun setNotificationIntentProvider(notificationIntentProvider: NotificationIntentProvider?) {
        this.notificationIntentProvider = notificationIntentProvider
    }

    private fun getDefaultContentIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            DEFAULT_REQUEST_CODE,
            getDefaultLauncherIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun checkForDefaultIcons(context: Context?) {
        if (usedDefaultSmallIcon) {
            Log.w(TAG, "Stream notification small icon is not defined, default one is used!")
        }
        if (usedDefaultLargeIcon) {
            largeIcon = BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.stream_ic_notification
            )
            Log.w(TAG, "Stream notification large icon is not defined, default one is used!")
        }
    }
}