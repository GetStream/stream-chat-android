package io.getstream.chat.android.client.notifications.options

import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.getstream.chat.android.client.notifications.options.NotificationIntentProvider

interface NotificationOptions {

    /**
     * @param context - App context
     * @return NotificationChannel
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotificationChannel(context: Context): NotificationChannel?

    /**
     * Get channel ID
     *
     * @param context
     * @return channel ID
     */
    fun getNotificationChannelId(context: Context): String?

    /**
     * Get channel name
     *
     * @param context - App context
     * @return Channel name
     */
    fun getNotificationChannelName(context: Context): String?

    /**
     * Get notification builder object
     *
     * @param context - App context
     * @return NotificationBuilder
     */
    fun getNotificationBuilder(context: Context): NotificationCompat.Builder?

    /**
     * Get intent from launched activity
     *
     * @param context - App context
     * @return Intent with extras
     */
    fun getDefaultLauncherIntent(context: Context): Intent?

    /**
     * Interface witch pass event from firebase or WebSocket
     *
     * @return
     */
    fun getNotificationIntentProvider(): NotificationIntentProvider?

    /**
     * Set small icon for notification
     *
     * @param iconRes - small icon res
     */
    fun setSmallIcon(@DrawableRes iconRes: Int)

    /**
     * Set large icon for notification
     *
     * @param icon - large icon
     */
    fun setLargeIcon(icon: Bitmap?)

    /**
     * Set custom id for notification channel
     *
     * @param notificationChannelId - id of channel
     */
    fun setNotificationChannelId(notificationChannelId: String?)

    /**
     * Set custom name for notification channel
     *
     * @param notificationChannelName - name of channel
     */
    fun setNotificationChannelName(notificationChannelName: String?)

    /**
     * Set notification channel object
     *
     * @param notificationChannel - Configured object for notification channel
     */
    fun setNotificationChannel(notificationChannel: NotificationChannel)

    /**
     * Set builder for notifications
     *
     * @param notificationBuilder
     */
    fun setNotificationBuilder(notificationBuilder: NotificationCompat.Builder?)

    /**
     * Set which component should start by default
     *
     * @param defaultLauncherIntent
     */
    fun setDefaultLauncherIntent(defaultLauncherIntent: Intent?)

    /**
     * Set object of
     *
     * @param notificationIntentProvider
     */
    fun setNotificationIntentProvider(notificationIntentProvider: NotificationIntentProvider?)
}