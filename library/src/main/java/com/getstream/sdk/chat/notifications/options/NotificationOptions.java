package com.getstream.sdk.chat.notifications.options;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/*
 * Created by Anton Bevza on 2019-11-15.
 */
public interface NotificationOptions {

    /**
     * @param context - App context
     * @return NotificationChannel
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getNotificationChannel(Context context);

    /**
     * Get channel ID
     *
     * @param context
     * @return channel ID
     */
    String getNotificationChannelId(Context context);

    /**
     * Get channel name
     *
     * @param context - App context
     * @return Channel name
     */
    String getNotificationChannelName(Context context);

    /**
     * Get notification builder object
     *
     * @param context - App context
     * @return NotificationBuilder
     */
    NotificationCompat.Builder getNotificationBuilder(Context context);

    /**
     * Get intent from launched activity
     *
     * @param context - App context
     * @return Intent with extras
     */
    Intent getDefaultLauncherIntent(Context context);

    /**
     * Interface witch pass event from firebase or WebSocket
     *
     * @return
     */
    NotificationIntentProvider getNotificationIntentProvider();

    /**
     * Set small icon for notification
     *
     * @param iconRes - small icon res
     */
    void setSmallIcon(@DrawableRes int iconRes);

    /**
     * Set large icon for notification
     *
     * @param icon - large icon
     */
    void setLargeIcon(Bitmap icon);

    /**
     * Set custom id for notification channel
     *
     * @param notificationChannelId - id of channel
     */
    void setNotificationChannelId(String notificationChannelId);

    /**
     * Set custom name for notification channel
     *
     * @param notificationChannelName - name of channel
     */
    void setNotificationChannelName(String notificationChannelName);

    /**
     * Set notification channel object
     *
     * @param notificationChannel - Configured object for notification channel
     */
    void setNotificationChannel(NotificationChannel notificationChannel);

    /**
     * Set builder for notifications
     *
     * @param notificationBuilder
     */
    void setNotificationBuilder(NotificationCompat.Builder notificationBuilder);

    /**
     * Set which component should start by default
     *
     * @param defaultLauncherIntent
     */
    void setDefaultLauncherIntent(Intent defaultLauncherIntent);

    /**
     * Set object of
     *
     * @param notificationIntentProvider
     */
    void setNotificationIntentProvider(NotificationIntentProvider notificationIntentProvider);

}
