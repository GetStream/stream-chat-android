package com.getstream.sdk.chat.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.aminography.redirectglide.RedirectGlideUrl;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.options.NotificationOptions;
import com.getstream.sdk.chat.notifications.options.StreamNotificationOptions;
import com.getstream.sdk.chat.receiver.NotificationMessageReceiver;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public class StreamNotificationsManager implements NotificationsManager {

    public static final String CHANNEL_ID_KEY = "channel_id";
    public static final String CHANNEL_TYPE_KEY = "channel_type";
    private static final String CHANNEL_NAME_KEY = "channel_name";
    private static final String MESSAGE_TEXT_KEY = "message_text";
    private static final String MESSAGE_ID_KEY = "message_id";

    private String channelName;
    private String message;
    private PendingIntent pendingIntent;
    private RemoteMessage remoteMessage;
    private Integer notificationId;

    private static final String TAG = StreamNotificationsManager.class.getSimpleName();

    private NotificationOptions notificationOptions;
    private DeviceRegisteredListener registerListener;
    private String lastNotificationId;

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

        handleRemoteMessage(context, remoteMessage);
    }

    @Override
    public void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context) {
        NotificationCompat.Builder builder = notificationOptions.getNotificationBuilder(context);
        Log.d(TAG, "onReceiveWebSocketEvent: " + event);

        int notificationId = (int) System.currentTimeMillis();

        if (event.getType() == EventType.MESSAGE_NEW) {
            builder.setContentTitle(event.getMessage().getUser().getName())
                    .setContentText(event.getMessage().getText())
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setShowWhen(true)
                    .setContentIntent(
                            notificationOptions.getNotificationIntentProvider()
                                    .getIntentForWebSocketEvent(context, event)
                    );

            String messageId = event.getMessage().getId();

            if (lastNotificationId == null || !lastNotificationId.equals(messageId)) {
                lastNotificationId = messageId;
                showNotification(notificationId, builder.build(), context);
            } else {
                Log.i(TAG, "Notification with id:" + messageId + " already showed");
            }
        }
    }

    /**
     * Handle Firebase message, check and show notifications
     *
     * @param context       - Context
     * @param remoteMessage - msg from Firebase
     */
    public void handleRemoteMessage(Context context, RemoteMessage remoteMessage) {
        String messageId = remoteMessage.getData().get(MESSAGE_ID_KEY);
        channelName = remoteMessage.getData().get(CHANNEL_NAME_KEY);
        message = remoteMessage.getData().get(MESSAGE_TEXT_KEY);
        String type = remoteMessage.getData().get(CHANNEL_TYPE_KEY);
        String id = remoteMessage.getData().get(CHANNEL_ID_KEY);
        this.remoteMessage = remoteMessage;

        if (checkSentNotificationWithId(messageId)) {

            lastNotificationId = messageId;
            notificationId = (int) System.currentTimeMillis();

            if (checkRequireFields(channelName, message, messageId)) {
                pendingIntent = preparePendingIntent(context, id, type, notificationId);

                StreamChat.getInstance(context).getMessage(messageId, new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        loadUserImage(context, response.getMessage().getUser().getImage());
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        Log.e(TAG, "Can\'t load user image. Error: " + errMsg);
                    }
                });
            } else {
                Log.e(TAG, "One of required fields is null: channelName = " + channelName +
                        ", message = " + message + ", messageId = " + messageId);
            }
        }
    }

    private boolean checkSentNotificationWithId(@Nullable String messageId) {
        return lastNotificationId == null || !lastNotificationId.equals(messageId);
    }

    private boolean checkRequireFields(@Nullable String channelName, @Nullable String message,
                                       @Nullable String messageId) {
        return channelName != null && !channelName.isEmpty()
                && message != null && !message.isEmpty()
                && messageId != null && !messageId.isEmpty();
    }

    private Notification prepareNotification(Context context, String title, String message, PendingIntent pendingIntent,
                                             RemoteMessage remoteMessage, @Nullable Bitmap image) {
        NotificationCompat.Builder notificationBuilder = notificationOptions.getNotificationBuilder(context);
        notificationBuilder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(getReadAction(context, pendingIntent))
                .addAction(getReplyAction(context, pendingIntent))
                .setShowWhen(true)
                .setContentIntent(
                        notificationOptions.getNotificationIntentProvider()
                                .getIntentForFirebaseMessage(context, remoteMessage)
                );

        if (image != null) {
            notificationBuilder.setLargeIcon(image);
        }

        return notificationBuilder.build();
    }

    private NotificationCompat.Action getReadAction(Context context, PendingIntent pendingIntent) {
        return new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_view,
                context.getString(R.string.stream_default_notification_read), pendingIntent).build();
    }

    private NotificationCompat.Action getReplyAction(Context context, PendingIntent replyPendingIntent) {
        RemoteInput remoteInput = new RemoteInput.Builder(NotificationMessageReceiver.KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.stream_default_notification_type))
                .build();

        return new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, context.getString(R.string.stream_default_notification_reply), replyPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();
    }

    private PendingIntent preparePendingIntent(Context context, String id, String type, int notificationId) {
        Intent notifyIntent = new Intent(context, NotificationMessageReceiver.class);
        notifyIntent.setAction(NotificationMessageReceiver.ACTION_REPLY);
        notifyIntent.putExtra(NotificationMessageReceiver.KEY_NOTIFICATION_ID, notificationId);
        notifyIntent.putExtra(NotificationMessageReceiver.KEY_CHANNEL_ID, id);
        notifyIntent.putExtra(NotificationMessageReceiver.KEY_CHANNEL_TYPE, type);

        return PendingIntent.getBroadcast(
                context,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void loadUserImage(Context context, String photoUrl) {
        Glide.with(context)
                .asBitmap()
                .load(new RedirectGlideUrl(photoUrl, 10))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Notification notification = prepareNotification(context,
                                channelName,
                                message,
                                pendingIntent,
                                remoteMessage,
                                resource
                        );
                        showNotification(notificationId, notification, context);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Notification notification = prepareNotification(context,
                                channelName,
                                message,
                                pendingIntent,
                                remoteMessage,
                                null
                        );
                        showNotification(notificationId, notification, context);
                    }
                });
    }

    private void showNotification(@NotNull Integer notificationId, @NotNull Notification notification, @NotNull Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null && !isForeground()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(
                        notificationOptions.getNotificationChannel(context));
            }

            notificationManager.notify(notificationId, notification);
        }
    }

    private boolean isForeground() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }
}
