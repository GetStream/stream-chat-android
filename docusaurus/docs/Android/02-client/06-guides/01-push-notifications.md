# Push Notifications

Besides [Events](https://getstream.io/chat/docs/android/event_listening/?language=kotlin), push notifications are another way to stay up to date with changes to the chat.
The user will receive a push notification if they're watching a channel but don't have an open socket connection.
To receive push notification from Stream server the first step you need to do is register the device. When you register a device on Stream server you need to provide the "Push Provider" you want to use and the token that this Push Provider generated for this device.
```kotlin
val token: String = obtainTokenFromYourPushProvider()
val device: Device = Device(
  token = token,
  pushProvider = PushProvider.FIREBASE, // You can check the different Push Provider supported here: https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-client/src/main/java/io/getstream/chat/android/client/models/Device.kt
)
ChatClient.setDevice(device)
```

Now that you have your `device` registered, Stream Server will start to send you push notification. Push Notifications will be received by the implementation your Push Provider SDK provides you, and you will need to generated a `PushMessage` instance with the received data and send it to our `ChatClient`

:::note
Make sure _ChatClient_ is initialized before handling push notifications. We highly recommend initializing it in the `Application` class.
:::

```kotlin
val data: YourPushProviderContainer
val pushMessage: PushMessage = PushMessage(
  messageId = data.get("message_id"),
  channelId = data.get("channel_id"),
  channelType = data.get("channel_type")
)
ChatClient.handlePushMessage(pushMessage)
```

It is all you need to do client-side to integrate push notification by yourselves.  
We also provide some [Companion Artifacts](#companion-artifacts) that help you with the integration process.

## PushDeviceGenerator
Our SDK allows you to provide an implementation of `PushDeviceGenerator` and handle all the needed process to obtain and register a device into Stream.
The `PushDeviceGenerator` is an interface that asks you to implement two methods. After you implement it, you need inject it into `ChatClient`, let me show you an example:
```kotlin
class MyPushDeviceGenerator : PushDeviceGenerator {
    override fun isValidForThisDevice(context: Context): Boolean {
        // Some providers are not allowed to be used in some devices if they doesn't have the proper libraries installed on the device
        // Here you can check if your provider could be used on this device and return true on the case it can be used on this device
        return true
    }
    
    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        // Here you will need to impelemnt the logic to obtain the token from your push provider and then generate the device instance
        val token = obtainTokenFromYourPushProvider()
        onDeviceGenerated(
            Device(
                token = token,
                pushProvider = PushProvider.FIREBASE, // You can check the different Push Provider supported here: https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-client/src/main/java/io/getstream/chat/android/client/models/Device.kt
            )
        )
    }
}

val notificationConfig = NotificationConfig(
  pushDeviceGenerators = listOf(MyPushDeviceGenerator())
)
ChatClient.Builder("apiKey", context)
    .notifications(ChatNotificationHandler(context, notificationsConfig))
    .build()
```

## Companion Artifacts
The push notification integration process is not complicated, but some jobs that we can provide to you from our SDK.  
We are not adding this implementation into our main SDK for multiple reasons. The main one is adding this default implementation increases the size of our SDK because new dependencies are needed and some customers don't want push notifications on their chat implementation.  
From time to time we will provide implementation for different provider.

### Stream Firebase PushProvider implementation
We provide an artifact with all the implementation needed to work with Firebase. To use it follow the next steps:

#### Step 1
Configure your app to use Firebase Cloud Messaging as described on the [Firebase documentation](https://firebase.google.com/docs/cloud-messaging/android/client)
You only need to setup the dependencies and _google-services.json_ file to your project source directory.

#### Step 2
Add the new artifact to the build gradle
```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-pushprovider-firebase:$stream_version"
}
```

#### Step 3
Add the `FirebasePushDeviceGenerator` to your NotificationConfig
```kotlin
val notificationConfig = NotificationConfig(
  pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
)
ChatClient.Builder("apiKey", context)
    .notifications(ChatNotificationHandler(context, notificationsConfig))
    .build()
```

#### Implement Custom _FirebaseMessagingService_
If you already are using FCM in your App, you can use your custom `FirebaseMessagingService` to receive push notifications from Stream

```kotlin
class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Update device's token on Stream backend
        try {
            ChatClient.setDevice(
                Device(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                )
            )
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            // Check if the message from Firebase contains needed data and generate a PushMessage instance
            val pushMessage = PushMessage(
                channelId = data["channel_id"]!!,
                messageId = data["message_id"]!!,
                channelType = data["channel_type"]!!,
            )
            ChatClient.handlePushMessage(pushMessage)
        } catch (exception: IllegalStateException) {
            // ChatClient was not initialized
        }
    }
}
```

:::note
Make sure that CustomMessageFirebaseService's priority is higher than -1 to override the default service
:::


### Stream Huawei PushProvider implementation
It is not ready yet, but we will implement it soon and release on the following releases. Be tuned

## Customizing Push Notifications

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

:::note
Consider overriding _ChatNotificationHandler#getErrorCaseIntent_ for error case notifications
:::

### Create a custom notification
The SDK allows you to generate the notification that will be shown after a push notification is received.
All you need to do is override the `buildNotification()` method as shown on the following example:

```kotlin
    val notificationHandler = MyNotificationHandler(context, notificationsConfig)

    ChatClient.Builder("{{ api_key }}", context)
                .notifications(notificationHandler)
                .build()
                
    class MyNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
        ChatNotificationHandler(context, notificationConfig) {

        override fun buildNotification(
            notificationId: Int,
            channel: Channel,
            message: Message
        ): NotificationCompat.Builder {
            // Here you are able to build your own notification and return the builder that generates it
            return NotificationCompact.Builder(context, CHANNEL_NOTIFICATION_ID)
                .setDefaults(NotificationCompat.DDEFAULT_ALL)
                // Customize your notification builder here
        }
    }
```

## Configuring Push Notifications on Stream Dashboard
To be able to receive PushNotifications from Stream Server, you need to provide the PushProvider Credential to Stream.
It needs to be done in your Stream's Dashboard

### Configuring Firebase Push Notification on Stream Dashboard
In order to configure Firebase push notifications on Android devices, you need to:

#### Step 1
Go to the [Firebase Console](https://console.firebase.google.com/), and select the project your app belongs to.

#### Step 2
Click on the gear icon next to _Project Overview_ and navigate to _Project settings_:
![notifications step 2](../../assets/notifications_firebase_setup_step_2.jpeg)

#### Step 3
Navigate to the _Cloud Messaging_ tab. Under _Project Credentials_, locate the _Server key_ and copy it:
![notifications step 3](../../assets/notifications_firebase_setup_step_3.png)

#### Step 4
Enable _Android & Firebase_ push notifications and upload the _Server Key_ to the Chat Dashboard:
![notifications step 4.1](../../assets/notifications_firebase_setup_step_4_1.jpeg)
![notifications step 4.2](../../assets/notifications_firebase_setup_step_4_2.jpeg)

#### Step 5
Save the push notification settings changes:
![notifications step 5](../../assets/notifications_firebase_setup_step_5.jpeg)

:::note
Remember to add _google-services.json_ file to your project source directory. For more information take a look at [Firebase setup tutorial](https://firebase.google.com/docs/android/setup).
:::
