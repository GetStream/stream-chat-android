package com.getstream.sdk.chat.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.options.NotificationOptions;
import com.getstream.sdk.chat.notifications.options.StreamNotificationOptions;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public class StreamNotificationsManager implements NotificationsManager {

    public static final String CHANNEL_ID_KEY = "channel_id";
    public static final String CHANNEL_TYPE_KEY = "channel_type";
    public static final String MESSAGE_ID_KEY = "message_id";
    private static final String CHANNEL_NAME_KEY = "channel_name";
    private static final String MESSAGE_TEXT_KEY = "message_text";

    private static final String TAG = StreamNotificationsManager.class.getSimpleName();

    private NotificationOptions notificationOptions;
    private NotificationCompat.Builder notificationBuilder;

    public StreamNotificationsManager(NotificationOptions notificationOptions) {
        this.notificationOptions = notificationOptions;
    }

    public StreamNotificationsManager() {
        this(new StreamNotificationOptions());
    }

    @Override
    public void setFirebaseToken(@NotNull String firebaseToken, @NotNull Context context) {
        StreamChat.getInstance(context).addDevice(firebaseToken, new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                // device is now registered!
            }

            @Override
            public void onError(String errMsg, int errCode) {
                // something went wrong registering this device, ouch!
            }
        });
    }

    @Override
    public void onReceiveFirebaseMessage(@NotNull RemoteMessage remoteMessage, @NotNull Context context) {
        Map<String, String> payload = remoteMessage.getData();
        Log.d(TAG, "onReceiveFirebaseMessage: " + remoteMessage.toString() + " data: " + payload);

        String channelName = remoteMessage.getData().get(CHANNEL_NAME_KEY);
        String massage = remoteMessage.getData().get(MESSAGE_TEXT_KEY);
        notificationBuilder = notificationOptions.getNotificationBuilder(context);
        notificationBuilder.setContentTitle(channelName)
                .setContentText(massage)
                .setContentIntent(
                        notificationOptions.getContentIntentProvider()
                                .getIntentForFirebaseMessage(context, remoteMessage)
                );
        showNotification(notificationBuilder.build(), context);
    }

    @Override
    public void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context) {
        NotificationCompat.Builder builder = notificationOptions.getNotificationBuilder(context);

        if (event.getMessage() != null) {
            builder.setContentTitle(event.getMessage().getUser().getName())
                    .setContentText(event.getMessage().getText())
                    .setContentIntent(
                            notificationOptions.getContentIntentProvider()
                                    .getIntentForWebSocketEvent(context, event)
                    );
            showNotification(builder.build(), context);
        }
    }

    @Override
    public void showNotification(@NotNull Notification notification, @NotNull Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && !isForeground()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(
                        notificationOptions.getNotificationChannel(context));
            }

            notificationManager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    @Override
    public NotificationOptions getNotificationsOptions() {
        return notificationOptions;
    }

    private boolean isForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

}
