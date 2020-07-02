**Setup**
To handle Firebase Push Notifications, a developer needs to register `StreamFirebaseMessagingService` in AndroidManifest:

```
<service
   android:name="io.getstream.chat.android.client.notifications.ChatFirebaseMessagingService"
   android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
</service>
```

In this case, the developer will have the default behavior.

Push notifications don't appear when the application in the foreground;
If the application goes to the background, Web Socket will keep the connection within 10 sec.
(Can be changed with WEB_SOCKET_DISCONNECT_DELAY constant in StreamChat)
It can be changed with `client.setWebSocketDisconnectDelay` method.

During this time push notification will show by WebSocket events.
By default channel name is the title of the notification. The message text is content of the notification.
Users photo is the large photo on notification. On click on notification will be opened the default launcher activity of the application.

***How to customize***
Developer have access to interfaces `NotificationsOptions` and `NotificationsManager`.
NotificationsOptions is interface and can be replaced by custom implementation.
NotificationsManager is interface and can be replaced by custom implementation.

Set notification channel id.
Default GetStreamChat.
`notificationsOptions.setNotificationChannelId("value");`

Set notification channel name.
Default GetStreamChat.
`notificationsOptions.setNotificationChannelName("value");`

Set custom notification channel
`notificationsOptions.setNotificationChannel(channel);`

To customize the notification
`notificationsOptions.setNotificationBuilder(notificationBuilder);`

Set default launcher activity
`notificationsOptions.setDefaultLauncherIntent(intent);`

Set notification small icon. Default white square.
`notificationsOptions.setSmallIcon(R.drawable.icon);`

Set notification large icon. Default white square.
`notificationsOptions.setLargeIcon(bitmap);`

Set custom `onClick` behaviour.
```
notificationOptions.setNotificationIntentProvider(
    new NotificationIntentProvider() {
        @Override
        public PendingIntent getIntentForFirebaseMessage(@NonNull Context context, @NonNull RemoteMessage remoteMessage) {
            Map<String, String> payload = remoteMessage.getData();
            Intent intent = new Intent(context, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, payload.get(StreamNotificationsManager.CHANNEL_TYPE_KEY));
            intent.putExtra(EXTRA_CHANNEL_ID, payload.get(StreamNotificationsManager.CHANNEL_ID_KEY));
            return PendingIntent.getActivity(context, 999,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            );}

            @Override
            public PendingIntent getIntentForWebSocketEvent(@NonNull Context context, @NonNull Event event) {
                Intent intent = new Intent(context, ChannelActivity.class);
                intent.putExtra(EXTRA_CHANNEL_TYPE, StringUtility.getChannelTypeFromCid(event.getCid()));
                intent.putExtra(EXTRA_CHANNEL_ID, StringUtility.getChannelIdFromCid(event.getCid()));
                return PendingIntent.getActivity(context, 999,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
    );
```

Also, you can detect is device registered on server with `DeviceRegisteredListener`
DeviceRegisteredListener is interface and can be replaced by custom implementation.

```
DeviceRegisteredListener onDeviceRegistered = new DeviceRegisteredListener() {
    @Override
    public void onDeviceRegisteredSuccess() {
        // Device successfully registered on server
        Log.i(TAG, "Device registered successfully");
    }

    @Override
    public void onDeviceRegisteredError(@NonNull String errorMessage, int errorCode) {
        // Some problem with registration
        Log.e(TAG, "onDeviceRegisteredError: " + errorMessage + " Code: " + errorCode);
    }
};
```

For displaying additional information like user photo, library load all received messages by default.
We can detect loading status via `NotificationMessageLoadListener`.
For example:
```
NotificationMessageLoadListener messageListener = new NotificationMessageLoadListener() {
    @Override
    public void onLoadMessageSuccess(@NonNull Message message) {
        Log.d(TAG, "On message loaded. Message:" + message);
    }

    @Override
    public void onLoadMessageFail(@NonNull String messageId) {
        Log.d(TAG, "Message from notification load fails. MessageId:" + messageId);
    }
};

StreamNotificationsManager notificationsManager = new StreamNotificationsManager(notificationOptions, onDeviceRegistered);
notificationsManager.setFailMessageListener(messageListener);
```

In the end of configuration we should use `setNotificationsManager`
`StreamChat.setNotificationsManager(notificationsManager);`
