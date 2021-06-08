---
id: client-handling_push_notifications_from_multiple_providers
title: Handling Push Notifications From Multiple Providers
sidebar_position: 2
---

_ChatClient_ provides following static methods that might be helpful for push notification handling:
* `ChatClient.isValidRemoteMessage` - checks if remote message can be handled by the SDK
* `ChatClient.handleRemoteMessage` - handles remote message internally
* `ChatClient.setFirebaseToken` - sets Firebase token

> **NOTE**: Each method should be called after initializing _ChatClient_

Push notifications from multiple providers can be handled in two different ways:

### Using Custom _ChatNotificationHandler_

```kotlin
class CustomChatNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
    ChatNotificationHandler(context, notificationConfig) {

    override fun onFirebaseMessage(message: RemoteMessage): Boolean {
        // Handle remote message and return true if message should be handled by SDK
        return true
    }
}

ChatClient.Builder("{{ api_key }}", context)
    // Pass custom chat notification handler to ChatClient
    .notifications(CustomChatNotificationHandler(context, notificationsConfig))
    .build()
```

### Implement Custom _FirebaseMessagingService_

```kotlin
class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Update device's token on Stream backend
        try {
            ChatClient.setFirebaseToken(token)
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            // Handle RemoteMessage sent from Stream backend
            ChatClient.handleRemoteMessage(message)
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }
}
```

> **NOTE**: Make sure that CustomMessageFirebaseService's priority is higher than -1 to override the default service
