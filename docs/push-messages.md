# Push messages

To customize notifications instance of `ChatNotificationConfig` should be created and passed to `ChatClient.Builder`:
```kotlin
val notificationsConfig = object : ChatNotificationConfig(context) {

}
client = ChatClient.Builder("api-key", context)
    .notifications(notificationsConfig)
    .build()
```
`ChatNotificationConfig` is an open class with predefined default implementation. There're several ways to customise notification by overriding default implementation.
## Building your own Notification
```kotlin
val notificationsConfig = object : ChatNotificationConfig(context) {
    override fun buildNotification(
        notificationId: Int,
        channelName: String,
        messageText: String,
        messageId: String,
        channelType: String,
        channelId: String
    ): Notification {
        // defining pending intents
        // defining notification behaviour with NotificationCompat.Builder
        // adding actions
        // etc
    }
}
```
## Intercepting Firebase remote message
```kotlin
val notificationsConfig = object : ChatNotificationConfig(context) {
    override fun onFirebaseMessage(message: RemoteMessage): Boolean {
        return true // to override default implementation
    }
}
```
## Create new message intent
```kotlin
val notificationsConfig = object : ChatNotificationConfig(context) {
    override fun getNewMessageIntent(messageId: String, channelType: String, channelId: String): Intent {
        val result = Intent(context, Activity::class.java)
        result.putExtra("channel-type", channelType)
        result.putExtra("channel-id", channelId)
        return result
    }
}
```