package io.getstream.chat.docs.java;

import android.content.Context;
import android.util.Log;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler;
import io.getstream.chat.android.client.notifications.handler.NotificationConfig;
import io.getstream.chat.docs.R;

import static io.getstream.chat.docs.StaticInstances.TAG;

public class Push {
    private Context context;
    private ChatClient client;

    /**
     * @see <a href="https://getstream.io/chat/docs/push_android/?language=java">Android & Firebase</a>
     */
    class AndroidAndFirebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=java#registering-a-device-at-stream-backend">Registering a device at Stream Backend</a>
         */
        public void registeringDevice() {
            client.addDevice("firebase-token").enqueue(result -> {
                if (result.isSuccess()) {
                    // Device was successfully registered
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=java#setting-up-notification-data-payload-at-stream-dashboard">Setting up notification</a>
         */
        public void setupNotifications() {
            int notificationChannelId = R.string.stream_chat_notification_channel_id;
            int notificationChannelName = R.string.stream_chat_notification_channel_name;
            int smallIcon = R.drawable.stream_ic_notification;
            String firebaseMessageIdKey = "message_id";
            String firebaseMessageTextKey = "message_text";
            String firebaseChannelIdKey = "channel_id";
            String firebaseChannelTypeKey = "channel_type";
            String firebaseChannelNameKey = "channel_name";
            int errorCaseNotificationTitle = R.string.stream_chat_notification_title;
            int errorCaseNotificationContent = R.string.stream_chat_notification_content;
            boolean useProvidedFirebaseInstance = true;

            NotificationConfig notificationsConfig = new NotificationConfig(
                    notificationChannelId,
                    notificationChannelName,
                    smallIcon,
                    firebaseMessageIdKey,
                    firebaseMessageTextKey,
                    firebaseChannelIdKey,
                    firebaseChannelTypeKey,
                    firebaseChannelNameKey,
                    errorCaseNotificationTitle,
                    errorCaseNotificationContent,
                    useProvidedFirebaseInstance
            );

            new ChatClient.Builder("{{ api_key }}", context)
                    .notifications(new ChatNotificationHandler(context, notificationsConfig))
                    .build();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/push_devices/?language=java">Device</a>
     */
    class Device {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=java#register-a-device">Register a Device</a>
         */
        public void registerADevice() {
            client.addDevice("firebase-token").enqueue(result -> {
                if (result.isSuccess()) {
                    // Device was successfully registered
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=java#unregister-a-device">Unregister a Device</a>
         */
        public void unregisterADevice() {
            client.deleteDevice("firebase-token").enqueue(result -> {
                if (result.isSuccess()) {
                    // Device was successfully unregistered
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()), result.error().getCause());
                }
            });
        }
    }
}
