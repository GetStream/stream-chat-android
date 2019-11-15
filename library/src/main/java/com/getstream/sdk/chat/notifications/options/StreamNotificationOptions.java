package com.getstream.sdk.chat.notifications.options;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.getstream.sdk.chat.R;

/*
 * Created by Anton Bevza on 2019-11-15.
 */
public class StreamNotificationOptions implements NotificationOptions {
    private String notificationChannelId;
    private String notificationChannelName;
    private NotificationChannel notificationChannel;
    private NotificationCompat.Builder notificationBuilder;
    private Intent defaultLauncherIntent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public NotificationChannel getNotificationChannel(Context context) {
        if (notificationChannel == null) {
            NotificationChannel channel = new NotificationChannel(
                    getNotificationChannelId(context),
                    getNotificationChannelName(context),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setShowBadge(true);
            return channel;
        } else {
            return notificationChannel;
        }
    }

    @Override
    public String getNotificationChannelId(Context context) {
        if (notificationChannelId == null) {
            return context.getString(R.string.default_notification_channel_id);
        } else {
            return notificationChannelId;
        }
    }

    @Override
    public String getNotificationChannelName(Context context) {
        if (notificationChannelName == null) {
            return context.getString(R.string.default_notification_channel_name);
        } else {
            return notificationChannelName;
        }
    }

    @Override
    public NotificationCompat.Builder getNotificationBuilder(Context context) {
        if (notificationBuilder == null) {
            return new NotificationCompat.Builder(context,
                    getNotificationChannelId(context))
                    .setAutoCancel(true)
                    //TODO need default icons for notifications
                    .setSmallIcon(R.mipmap.icon)
                    //TODO need default icons for notifications
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon))
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
        } else {
            return notificationBuilder;
        }
    }

    public Intent getDefaultLauncherIntent(Context context) {
        if (defaultLauncherIntent == null) {
            return context.getPackageManager().getLaunchIntentForPackage(
                    context.getPackageName()
            );
        } else {
            return defaultLauncherIntent;
        }
    }

    public void setNotificationChannelId(String notificationChannelId) {
        this.notificationChannelId = notificationChannelId;
    }

    public void setNotificationChannelName(String notificationChannelName) {
        this.notificationChannelName = notificationChannelName;
    }

    public void setNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public void setNotificationBuilder(NotificationCompat.Builder notificationBuilder) {
        this.notificationBuilder = notificationBuilder;
    }

    public void setDefaultLauncherIntent(Intent defaultLauncherIntent) {
        this.defaultLauncherIntent = defaultLauncherIntent;
    }
}
