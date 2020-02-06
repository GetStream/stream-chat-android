# Push messages

To customize notifications you need to pass the configuration to ChatClient builder.

```kotlin
val notificationConfig = NotificationsManager.Builder()
            .setNotificationOptions(provideNotificationOptions())
            .setRegisterListener(provideDeviceRegisteredListener())
            .setNotificationMessageLoadListener(provideNotificationMessageLoadListener())
            .build()

client = ChatClient.init(
            ChatClient.Builder()
                .config(config)
                .logger(logger)
                .notification(**notificationConfig**)
        )
```

Set up notifications requires calling setNotificationOptions
```kotlin
fun provideNotificationOptions() = StreamNotificationOptions().apply {
        setNotificationIntentProvider(
            object : NotificationIntentProvider {
                override fun getIntentForFirebaseMessage(
                    context: Context,
                    remoteMessage: RemoteMessage
                ): PendingIntent {
                    val payload = remoteMessage.data
                    val intent = Intent(context, <TARGET_ACTIVITY>::class.java)
                    intent.apply {
                        putExtra(
                            EXTRA_CHANNEL_TYPE,
                            payload[StreamNotificationsManager.CHANNEL_TYPE_KEY]
                        )
                        putExtra(
                            EXTRA_CHANNEL_ID,
                            payload[StreamNotificationsManager.CHANNEL_ID_KEY]
                        )
                    }
                    return PendingIntent.getActivity(
                        context, 999,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                override fun getIntentForWebSocketEvent(
                    context: Context,
                    event: ChatEvent
                ): PendingIntent {
                    val intent = Intent(context, <TARGET_ACTIVITY>::class.java)
                    intent.apply {
                        putExtra(EXTRA_CHANNEL_TYPE, event.message.type)
                        putExtra(EXTRA_CHANNEL_ID, event.message.id)
                    }
                    return PendingIntent.getActivity(
                        context, 999,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            }
        )
}
```

DeviceRegisteredListener - interface through which a device registration callback is received to receive push notifications
```kotlin
private fun provideDeviceRegisteredListener() = object : DeviceRegisteredListener {
        override fun onDeviceRegisteredSuccess() { // Device successfully registered on server
            logger.logI(this, "Device registered successfully")
        }

        override fun onDeviceRegisteredError(error: ChatError) {
            logger.logE(this, "onDeviceRegisteredError: ${error.message}")
        }
}
```
NotificationMessageLoadListener - interface through which a callback is received to retrieve a received message
```kotlin
private fun provideNotificationMessageLoadListener() =
        object : NotificationMessageLoadListener {
            override fun onLoadMessageSuccess(message: Message) {
                logger.logD(this, "On message loaded. Message:$message")
            }

            override fun onLoadMessageFail(messageId: String) {
                logger.logD(this, "Message from notification load fails. MessageId:$messageId")
            }
        }
```