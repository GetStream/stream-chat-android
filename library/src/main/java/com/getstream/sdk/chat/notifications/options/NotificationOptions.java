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
}
