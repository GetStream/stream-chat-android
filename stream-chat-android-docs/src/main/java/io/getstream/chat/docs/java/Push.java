package io.getstream.chat.docs.java;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.models.Device;
import io.getstream.chat.android.client.notifications.PushNotificationRenderer;
import io.getstream.chat.android.livedata.service.sync.PushMessageSyncHandler;
import io.getstream.chat.docs.MainActivity;

public class Push {
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
                    // Handle result.error()
                }
            });
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=java#redirection-from-notification-to-app">Redirection from notification to app</a>
     *
     * <pre>
     * <receiver
     *     android:name=".application.SamplePushNotificationRenderer"
     *     android:enabled="true"
     *     android:exported="false">
     *         <intent-filter>
     *             <action android:name="io.getstream.chat.SHOW_NOTIFICATION" />
     *         </intent-filter>
     *  </receiver>
     * </pre>
     */
    class MyPushNotificationRenderer extends PushNotificationRenderer {

        final static String EXTRA_CHANNEL_ID = "extra_channel_id";
        final static String EXTRA_CHANNEL_TYPE = "extra_channel_type";
        final static String EXTRA_MESSAGE_ID = "extra_message_id";

        @NotNull
        @Override
        public Intent getNewMessageIntent(
                @NotNull Context context,
                @NotNull String messageId,
                @NotNull String channelType,
                @NotNull String channelId
        ) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(EXTRA_CHANNEL_ID, channelId);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
            intent.putExtra(EXTRA_MESSAGE_ID, messageId);
            return intent;
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=java#handling-notifications-from-multiple-backend-services">Handling notifications from multiple backend services</a>
     */
    class CustomFirebaseMessagingService extends FirebaseMessagingService {
        private PushMessageSyncHandler pushDataSyncHandler = new PushMessageSyncHandler(this);

        @Override
        public void onNewToken(String token) {
            // update device's token on Stream backend
            pushDataSyncHandler.onNewToken(token);
        }

        @Override
        public void onMessageReceived(RemoteMessage message) {
            if (pushDataSyncHandler.isStreamMessage(message)) {
                // handle RemoteMessage sent from Stream backend
                pushDataSyncHandler.onMessageReceived(message);
            } else {
                // handle RemoteMessage from other source
            }
            stopSelf();
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/push_devices/?language=java">Device</a>
     */
    class Device_ {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=java#register-a-device">Register a Device</a>
         */
        public void registerADevice() {
            client.addDevice("firebase-token").enqueue(result -> {
                if (result.isSuccess()) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
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
                    // Handle result.error()
                }
            });
        }

        public void listDevices() {
            client.getDevices().enqueue(result -> {
                if (result.isSuccess()) {
                    List<Device> devices = result.data();
                } else {
                    // Handle result.error()
                }
            });
        }
    }
}
