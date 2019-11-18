package com.getstream.sdk.chat.notifications.options;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/*
 * Created by Anton Bevza on 2019-11-15.
 */
public interface NotificationOptions {

    //TODO add java doc

    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getNotificationChannel(Context context);

    String getNotificationChannelId(Context context);

    String getNotificationChannelName(Context context);

    NotificationCompat.Builder getNotificationBuilder(Context context);

    Intent getDefaultLauncherIntent(Context context);

    ContentIntentProvider getContentIntentProvider();

    void setNotificationChannelId(String notificationChannelId);

    void setNotificationChannelName(String notificationChannelName);

    void setNotificationChannel(NotificationChannel notificationChannel);

    void setNotificationBuilder(NotificationCompat.Builder notificationBuilder);

    void setDefaultLauncherIntent(Intent defaultLauncherIntent);

    void setContentIntentProvider(ContentIntentProvider contentIntentProvider);

}
