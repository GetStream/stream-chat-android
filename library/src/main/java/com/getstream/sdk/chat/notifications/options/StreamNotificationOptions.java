package com.getstream.sdk.chat.notifications.options;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Event;
import com.google.firebase.messaging.RemoteMessage;

/*
 * Created by Anton Bevza on 2019-11-15.
 */
public class StreamNotificationOptions implements NotificationOptions {

    private static final String TAG = StreamNotificationOptions.class.getSimpleName();
    private static final int DEFAULT_REQUEST_CODE = 999;

    private String notificationChannelId;
    private String notificationChannelName;
    private NotificationChannel notificationChannel;
    private NotificationCompat.Builder notificationBuilder;
    private Intent defaultLauncherIntent;
    private NotificationIntentProvider notificationIntentProvider;
    private int smallIcon = R.drawable.stream_ic_notification;
    private Bitmap largeIcon;
    private boolean usedDefaultSmallIcon = true;
    private boolean usedDefaultLargeIcon = true;

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
            return context.getString(R.string.stream_default_notification_channel_id);
        } else {
            return notificationChannelId;
        }
    }

    @Override
    public String getNotificationChannelName(Context context) {
        if (notificationChannelName == null) {
            return context.getString(R.string.stream_default_notification_channel_name);
        } else {
            return notificationChannelName;
        }
    }

    @Override
    public NotificationCompat.Builder getNotificationBuilder(Context context) {
        if (notificationBuilder == null) {
            checkForDefaultIcons(context);

            return new NotificationCompat.Builder(context, getNotificationChannelId(context))
                    .setAutoCancel(true)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(largeIcon)
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

    @Override
    public NotificationIntentProvider getNotificationIntentProvider() {
        if (notificationIntentProvider != null) {
            return notificationIntentProvider;
        } else {
            return new NotificationIntentProvider() {
                @Override
                public PendingIntent getIntentForFirebaseMessage(@NonNull Context context,
                                                                 @NonNull RemoteMessage remoteMessage) {
                    return getDefaultContentIntent(context);
                }

                @Override
                public PendingIntent getIntentForWebSocketEvent(@NonNull Context context,
                                                                @NonNull Event event) {
                    return getDefaultContentIntent(context);
                }
            };
        }
    }

    @Override
    public void setSmallIcon(int iconRes) {
        this.usedDefaultSmallIcon = false;
        this.smallIcon = iconRes;
    }

    @Override
    public void setLargeIcon(Bitmap icon) {
        this.usedDefaultLargeIcon = false;
        this.largeIcon = icon;
    }

    @Override
    public void setNotificationChannelId(String notificationChannelId) {
        this.notificationChannelId = notificationChannelId;
    }

    @Override
    public void setNotificationChannelName(String notificationChannelName) {
        this.notificationChannelName = notificationChannelName;
    }

    @Override
    public void setNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    @Override
    public void setNotificationBuilder(NotificationCompat.Builder notificationBuilder) {
        this.notificationBuilder = notificationBuilder;
    }

    @Override
    public void setDefaultLauncherIntent(Intent defaultLauncherIntent) {
        this.defaultLauncherIntent = defaultLauncherIntent;
    }

    @Override
    public void setNotificationIntentProvider(NotificationIntentProvider notificationIntentProvider) {
        this.notificationIntentProvider = notificationIntentProvider;
    }

    private PendingIntent getDefaultContentIntent(Context context) {
        return PendingIntent.getActivity(context,
                DEFAULT_REQUEST_CODE,
                getDefaultLauncherIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void checkForDefaultIcons(Context context) {
        if (usedDefaultSmallIcon) {
            Log.w(TAG, "Stream notification small icon is not defined, default one is used!");
        }
        if (usedDefaultLargeIcon) {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.stream_ic_notification);
            Log.w(TAG, "Stream notification large icon is not defined, default one is used!");
        }
    }
}
