---
id: client-customizing-push-notifications
title: Customizing Push Notifications
sidebar_position: 3
---

### Customizing Notifications
Notifications can be customized using:
* _NotificationsConfig_ - allows to customize notification's resources and meta data
* _ChatNotificationHandler_ - allows to customize default push notifications handling behaviour

### Redirecting From Notification To App
In order to redirect from notification to the specific _Activity_ in your app, you need to create custom _ChatNotificationHandler_ and override _ChatNotificationHandler#getNewMessageIntent_:

```kotlin
    val notificationHandler = MyNotificationHandler(context, notificationsConfig)

    ChatClient.Builder("{{ api_key }}", context)
                .notifications(notificationHandler)
                .build()
                
    class MyNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
        ChatNotificationHandler(context, notificationConfig) {

        override fun getNewMessageIntent(
            messageId: String,
            channelType: String,
            channelId: String
        ): Intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_CHANNEL_TYPE, channelType)
            putExtra(EXTRA_MESSAGE_ID, messageId)
        }

        companion object {
            const val EXTRA_CHANNEL_ID = "extra_channel_id"
            const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
            const val EXTRA_MESSAGE_ID = "extra_message_id"
        }
    }
```

> **NOTE**: Consider overriding _ChatNotificationHandler#getErrorCaseIntent_ for error case notifications
