package io.getstream.chat.docs.java;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.models.Device;
import io.getstream.chat.android.client.models.PushProvider;
import io.getstream.chat.android.pushprovider.firebase.FirebaseMessagingDelegate;

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
            client.addDevice(
                    new Device(
                            "push-provider-token",
                            PushProvider.FIREBASE
                    )
            ).enqueue(result -> {
                if (result.isSuccess()) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
                }
            });
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=java#handling-notifications-from-multiple-providers">Handling notifications from multiple providers</a>
         */
        class CustomFirebaseMessagingService extends FirebaseMessagingService {

            @Override
            public void onNewToken(@NotNull String token) {
                // Update device's token on Stream backend
                try {
                    FirebaseMessagingDelegate.registerFirebaseToken(token);
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }

            @Override
            public void onMessageReceived(@NotNull RemoteMessage message) {
                try {
                    if (FirebaseMessagingDelegate.handleRemoteMessage(message)) {
                        // RemoteMessage was from Stream and it is already processed
                    } else {
                        // RemoteMessage wasn't sent from Stream and it needs to be handled by you
                    }
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }
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
            client.addDevice(
                    new Device(
                            "push-provider-token",
                            PushProvider.FIREBASE
                    )
            ).enqueue(result -> {
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
            client.deleteDevice(
                    new Device(
                            "push-provider-token",
                            PushProvider.FIREBASE
                    )
            ).enqueue(result -> {
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
