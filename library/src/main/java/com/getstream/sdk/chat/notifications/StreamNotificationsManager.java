package com.getstream.sdk.chat.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.options.NotificationOptions;
import com.getstream.sdk.chat.notifications.options.StreamNotificationOptions;
import com.getstream.sdk.chat.receiver.NotificationMessageReceiver;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public class StreamNotificationsManager implements NotificationsManager {

    public static final String CHANNEL_ID_KEY = "channel_id";
    public static final String CHANNEL_TYPE_KEY = "channel_type";
    private static final String CHANNEL_NAME_KEY = "channel_name";
    private static final String MESSAGE_TEXT_KEY = "message_text";
    private static final String MESSAGE_ID_KEY = "message_id";

    private static final String TAG = StreamNotificationsManager.class.getSimpleName();

    private NotificationOptions notificationOptions;
    private DeviceRegisteredListener registerListener;
    private String lastNotificationId = null;

    public StreamNotificationsManager(NotificationOptions notificationOptions, @Nullable DeviceRegisteredListener listener) {
        this.notificationOptions = notificationOptions;
        this.registerListener = listener;
    }

    public StreamNotificationsManager() {
        this(new StreamNotificationOptions(), null);
    }

    @Override
    public void setFirebaseToken(@NotNull String firebaseToken, @NotNull Context context) {
        Log.d(TAG, "setFirebaseToken: " + firebaseToken);

        StreamChat.getInstance(context).addDevice(firebaseToken, new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                // device is now registered!
                if (registerListener != null) {
                    registerListener.onDeviceRegisteredSuccess();
                }
                Log.i(TAG, "DeviceRegisteredSuccess");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                if (registerListener != null) {
                    registerListener.onDeviceRegisteredError(errMsg, errCode);
                }
                Log.e(TAG, errMsg);
            }
        });
    }

    @Override
    public void onReceiveFirebaseMessage(@NotNull RemoteMessage remoteMessage, @NotNull Context context) {
        Map<String, String> payload = remoteMessage.getData();
        Log.d(TAG, "onReceiveFirebaseMessage: " + remoteMessage.toString() + " data: " + payload);

        String channelName = remoteMessage.getData().get(CHANNEL_NAME_KEY);
        String message = remoteMessage.getData().get(MESSAGE_TEXT_KEY);
        String messageId = remoteMessage.getData().get(MESSAGE_ID_KEY);
        String type = remoteMessage.getData().get(CHANNEL_TYPE_KEY);
        String id = remoteMessage.getData().get(CHANNEL_ID_KEY);


        if (hasRequireFields(remoteMessage)) {
            NotificationCompat.Builder notificationBuilder = notificationOptions.getNotificationBuilder(context);
            notificationBuilder.setContentTitle(channelName)
                    .setContentText(message)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .addAction(android.R.drawable.ic_menu_agenda, context.getString(R.string.default_notification_read), getReadPendingIntent(context, id, type))
                    .setContentIntent(
                            notificationOptions.getNotificationIntentProvider()
                                    .getIntentForFirebaseMessage(context, remoteMessage)
                    );

            if (lastNotificationId == null || !lastNotificationId.equals(messageId)) {
                lastNotificationId = messageId;
                showNotification(notificationBuilder.build(), context);
            } else {
                Log.i(TAG, "Notification with id:" + messageId + " already showed");
            }
        } else {
            Log.e(TAG, "Firebase message has empty fields");
        }
    }

    @Override
    public void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context) {
        NotificationCompat.Builder builder = notificationOptions.getNotificationBuilder(context);
        Log.d(TAG, "onReceiveWebSocketEvent: " + event);

        if (event.getType() == EventType.MESSAGE_NEW) {
            builder.setContentTitle(event.getMessage().getUser().getName())
                    .setContentText(event.getMessage().getText())
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentIntent(
                            notificationOptions.getNotificationIntentProvider()
                                    .getIntentForWebSocketEvent(context, event)
                    );

            String messageId = event.getMessage().getId();

            if (lastNotificationId == null || !lastNotificationId.equals(messageId)) {
                lastNotificationId = messageId;
                showNotification(builder.build(), context);
            } else {
                Log.i(TAG, "Notification with id:" + messageId + " already showed");
            }
        }
    }

    public boolean hasRequireFields(RemoteMessage remoteMessage) {
        String channelName = remoteMessage.getData().get(CHANNEL_NAME_KEY);
        String message = remoteMessage.getData().get(MESSAGE_TEXT_KEY);

        return channelName != null && !channelName.isEmpty()
                && message != null && !message.isEmpty();
    }

    public boolean hasRequireFields(@NotNull Event event) {
        return true;
    }

    private PendingIntent getReadPendingIntent(Context context, String id, String type) {
        Intent notifyIntent = new Intent(context, NotificationMessageReceiver.class);
        notifyIntent.setAction(NotificationMessageReceiver.ACTION_READ);
        notifyIntent.putExtra(NotificationMessageReceiver.KEY_CHANNEL_ID, id);
        notifyIntent.putExtra(NotificationMessageReceiver.KEY_CHANNEL_TYPE, type);

        return PendingIntent.getBroadcast(
                context,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void showNotification(@NotNull Notification notification, @NotNull Context context) {
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

    private boolean isForeground() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }
}
