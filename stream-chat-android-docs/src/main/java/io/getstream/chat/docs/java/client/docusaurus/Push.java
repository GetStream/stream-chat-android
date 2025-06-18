package io.getstream.chat.docs.java.client.docusaurus;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.huawei.hms.push.HmsMessageService;
import com.xiaomi.channel.commonutils.android.Region;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.getstream.android.push.PushDeviceGenerator;
import io.getstream.android.push.firebase.FirebaseMessagingDelegate;
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator;
import io.getstream.android.push.huawei.HuaweiMessagingDelegate;
import io.getstream.android.push.huawei.HuaweiPushDeviceGenerator;
import io.getstream.android.push.permissions.NotificationPermissionStatus;
import io.getstream.android.push.xiaomi.XiaomiMessagingDelegate;
import io.getstream.android.push.xiaomi.XiaomiPushDeviceGenerator;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.events.NotificationReminderDueEvent;
import io.getstream.chat.android.client.notifications.handler.NotificationConfig;
import io.getstream.chat.android.client.notifications.handler.NotificationHandler;
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.Device;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.models.PushMessage;
import io.getstream.chat.android.models.PushProvider;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/">Push Notifications</a>
 */
public class Push {

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#overview">Overview</a>
     */
    public void configureNotification(Context context, NotificationHandler notificationHandler) {
        boolean pushNotificationEnabled = true;
        boolean ignorePushMessagesWhenUserOnline = true;
        List<PushDeviceGenerator> pushDeviceGeneratorList = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig(pushNotificationEnabled, ignorePushMessagesWhenUserOnline, pushDeviceGeneratorList);

        new ChatClient.Builder("api-key", context)
                .notifications(notificationConfig, notificationHandler)
                .build();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#using-our-notificationhandlerfactory">Using our NotificationHandlerFactory</a>
     */
    public void customNotificationHandler(Context context) {
        boolean pushNotificationEnabled = true;
        boolean ignorePushMessagesWhenUserOnline = true;
        List<PushDeviceGenerator> pushDeviceGeneratorList = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig(pushNotificationEnabled, ignorePushMessagesWhenUserOnline, pushDeviceGeneratorList);

        NotificationHandler notificationHandler = NotificationHandlerFactory.createNotificationHandler(context, notificationConfig, (message, channel) -> {
            // Return the intent you want to be triggered when the notification is clicked
            Intent intent = new Intent();

            return intent;
        });

        new ChatClient.Builder("api-key", context)
                .notifications(notificationConfig, notificationHandler)
                .build();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#customize-notification-style">Customizing Notification Style</a>
     */
    public void customizeNotificationStyle(Context context, NotificationConfig notificationConfig) {
        String notificationChannelId = "";
        int notificationId = 1;

        class MyNotificationHandler implements NotificationHandler {

            NotificationManager notificationManager;

            public MyNotificationHandler(Context context) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            @Override
            public void onNotificationPermissionStatus(@NonNull NotificationPermissionStatus status) {
                switch (status) {
                    case REQUESTED:
                        // invoked when POST_NOTIFICATIONS permission is requested
                        break;
                    case GRANTED:
                        // invoked when POST_NOTIFICATIONS permission is granted
                        break;
                    case DENIED:
                        // invoked when POST_NOTIFICATIONS permission is denied
                        break;
                    case RATIONALE_NEEDED:
                        // invoked when POST_NOTIFICATIONS permission requires rationale
                        break;
                }
            }

            @Override
            public void showNotification(@NonNull String type, @NonNull Channel channel, @NonNull Message message) {
                showNotification(channel, message);
            }

            @Override
            public void showNotification(@NonNull Channel channel, @NonNull Message message) {
                Notification notification = new NotificationCompat.Builder(context, notificationChannelId)
                        .build();
                notificationManager.notify(notificationId, notification);
            }

            @Override
            public void dismissChannelNotifications(@NonNull String channelType, @NonNull String channelId) {
                // Dismiss all notification related with this channel
            }

            @Override
            public void dismissAllNotifications() {
                // Dismiss all notifications
            }

            @Override
            public boolean onChatEvent(@NonNull NewMessageEvent event) {
                return true;
            }

            @Override
            public boolean onNotificationReminderDueEvent(@NonNull NotificationReminderDueEvent event) {
                return false;
            }

            @Override
            public boolean onPushMessage(@NonNull PushMessage message) {
                return false;
            }
        }

        NotificationHandler notificationHandler = new MyNotificationHandler(context);

        new ChatClient.Builder("api-key", context)
                .notifications(notificationConfig, notificationHandler)
                .build();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#dismissing-notifications">Dismissing Notifications</a>
     */
    public void dismissingNotifications() {
        ChatClient.instance().dismissChannelNotifications("messaging", "general");
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#multi-bundle">Multi Bundle</a>
     */
    public void multiBundle() {
        new Device(
                "token-generated-by-provider",
                PushProvider.FIREBASE, // your push provider
                "providerName"
        );
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/">Firebase Cloud Messaging</a>
     */
    class Firebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        public void configureFirebaseNotifications(Context context) {
            boolean pushNotificationEnabled = true;
            boolean ignorePushMessagesWhenUserOnline = true;
            List<PushDeviceGenerator> pushDeviceGeneratorList = Collections.singletonList(
                    new FirebasePushDeviceGenerator(
                            FirebaseMessaging.getInstance(),
                            "providerName",
                            context
                    )
            );
            NotificationConfig notificationConfig = new NotificationConfig(pushNotificationEnabled, ignorePushMessagesWhenUserOnline, pushDeviceGeneratorList);
            new ChatClient.Builder("apiKey", context)
                    .notifications(notificationConfig)
                    .build();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service">Using a Custom Firebase Messaging Service</a>
         */
        public final class CustomFirebaseMessagingService extends FirebaseMessagingService {

            @Override
            public void onNewToken(@NonNull String token) {
                // Update device's token on Stream backend
                try {
                    FirebaseMessagingDelegate.registerFirebaseToken(token, "providerName");
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }

            @Override
            public void onMessageReceived(@NonNull RemoteMessage message) {
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
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei/#huawei-push-kit">Huawei Push Kit</a>
     */
    class Huawei {

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        public void configureHuaweiNotifications(Context context) {
            boolean pushNotificationEnabled = true;
            boolean ignorePushMessagesWhenUserOnline = true;
            List<PushDeviceGenerator> pushDeviceGeneratorList = Collections.singletonList(
                    new HuaweiPushDeviceGenerator(context, "YOUR HUAWEI APP ID", "providerName")
            );
            NotificationConfig notificationConfig = new NotificationConfig(pushNotificationEnabled, ignorePushMessagesWhenUserOnline, pushDeviceGeneratorList);
            new ChatClient.Builder("apiKey", context)
                    .notifications(notificationConfig)
                    .build();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service">Using a Custom Service</a>
         */
        public final class CustomHuaweiMessagingService extends HmsMessageService {
            @Override
            public void onNewToken(String token) {
                // Update device's token on Stream backend
                try {
                    HuaweiMessagingDelegate.registerHuaweiToken(token, "providerName");
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }

            @Override
            public void onMessageReceived(com.huawei.hms.push.RemoteMessage remoteMessage) {
                try {
                    if (HuaweiMessagingDelegate.handleRemoteMessage(remoteMessage)) {
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
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#xiaomi-mi-push">Xiaomi Mi Push</a>
     */
    class Xiaomi {
        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        public void configureXiaomiNotifications(Context context) {
            boolean pushNotificationEnabled = true;
            boolean ignorePushMessagesWhenUserOnline = true;
            List<PushDeviceGenerator> pushDeviceGeneratorList = Collections.singletonList(new XiaomiPushDeviceGenerator(context, "YOUR HUAWEI APP ID", "YOUR XIAOMI APP KEY", "providerName", Region.Global));
            NotificationConfig notificationConfig = new NotificationConfig(pushNotificationEnabled, ignorePushMessagesWhenUserOnline, pushDeviceGeneratorList);
            new ChatClient.Builder("apiKey", context)
                    .notifications(notificationConfig)
                    .build();
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#using-a-custom-pushmessagereceiver">Using a Custom PushMessageReceiver</a>
         */
        class CustomPushMessageReceiver extends PushMessageReceiver {

            @Override
            public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
                // Update device's token on Stream backend
                try {
                    XiaomiMessagingDelegate.registerXiaomiToken(miPushCommandMessage, "providerName");
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }

            @Override
            public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
                try {
                    if (XiaomiMessagingDelegate.handleMiPushMessage(miPushMessage)) {
                        // MiPushMessage was from Stream and it is already processed
                    } else {
                        // MiPushMessage wasn't sent from Stream and it needs to be handled by you
                    }
                } catch (IllegalStateException exception) {
                    // ChatClient was not initialized
                }
            }
        }
    }
}
