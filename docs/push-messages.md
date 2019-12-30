# Push messages

```kotlin

val notificationsManager = StreamChatNotifications.Builder()
	.icon(R.drawable.app_icon)
	.build()

val client = StreamChatClient.Builder()
	.notifications(notificationsManager)
	.build()
```