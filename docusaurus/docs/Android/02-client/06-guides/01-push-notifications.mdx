# Push Notifications

Besides [Events](https://getstream.io/chat/docs/android/event_listening/?language=kotlin), push notifications are another way to stay up to date with changes in your chat.
The user will receive a push notification if they're watching a channel but don't have an open socket connection.

To receive push notifications from the Stream server the first step you need to do is register the device. When you register a device with the Stream server you need to provide two things:
* The "Push Provider" you want to use.
* The token that this Push Provider generated for this device.

To register the device, you can use the following approach:

```kotlin
val token: String = obtainTokenFromYourPushProvider()
val device: Device = Device(
  token = token,
  pushProvider = PushProvider.FIREBASE,
)
ChatClient.setDevice(device)
```

:::note
Make sure _ChatClient_ is initialized before handling push notifications. We highly recommend initializing it in the `Application` class.
:::

Once you have a token that your provider generated, you create a new `Device` that you set in our `ChatClient`. For the provider, you used `PushProvider.FIREBASE`.

Make sure to check out the [list of supported providers](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-client/src/main/java/io/getstream/chat/android/client/models/Device.kt).

Now that you have your `device` registered, the Stream Server will start sending you push notifications. Your Push Provider SDK implementation will receive the notifications and you will need to generate a `PushMessage` with the received data.

Once you have a `PushMessage`, you need to send it to our `ChatClient`, like so:

```kotlin
val data: YourPushProviderContainer
val pushMessage: PushMessage = PushMessage(
  messageId = data.get("message_id"),
  channelId = data.get("channel_id"),
  channelType = data.get("channel_type")
)
ChatClient.handlePushMessage(pushMessage)
```

It's important that you set the `messageId`, `channelId` and `channelType` properties of the message, before sending it to the `ChatClient`. This is all you need to do client-side to integrate push notifications by yourselves!

We also provide some [Companion Artifacts](#companion-artifacts) that help you with the integration process.

## PushDeviceGenerator
Our SDK allows you to provide an implementation of `PushDeviceGenerator` and handle all the logic to obtain and register a device with Stream. That way, you gain more control of push notifications.

Here's an example of a custom `PushDeviceGenerator`:

```kotlin
class MyPushDeviceGenerator : PushDeviceGenerator {
    override fun isValidForThisDevice(context: Context): Boolean {
        // return if the device is valid or not
        return true
    }
    
    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        // Generate the device token from your provider
        val token = obtainTokenFromYourPushProvider()
        
        // Pass the device to the ChatClient
        onDeviceGenerated(
            Device(
                token = token,
                pushProvider = PushProvider.FIREBASE,
            )
        )
    }
}
```

The `PushDeviceGenerator` is an interface that asks you to implement two methods:
* `isValidForThisDevice(Context): Boolean`: This function lets you decide if your provider is allowed on a particular device. For example, some providers are not allowed on some devices if they doesn't have certain libraries installed. Return `true` if your provider is allowed and `false` if it's not available for this device.
* `asyncGenerateDevice((Device) -> Unit)`: This function allows you control how you generate the `Device` that you'll register with the `ChatClient`. Once you create a `Device`, pass it to the `onDeviceGenerated()` callback.

Now that you've implemented your custom `PushDeviceGenerator` you need to inject it into the `ChatClient`:

```kotlin
val notificationConfig = NotificationConfig(
  pushDeviceGenerators = listOf(MyPushDeviceGenerator())
)
ChatClient.Builder("apiKey", context)
    .notifications(ChatNotificationHandler(context, notificationsConfig))
    .build()
```

Here you first create a `NotificationConfig` that describes how you want to configure push notifications. In this case, you passed in a custom `PushDeviceGenerator`.

Then you use the `ChatClient.Builder` to add `notifications()` configuration to the client, before building it.

## Companion Artifacts
The push notification integration process is not complicated, but we don't want to add it into our main SDK by default.

The two main reasons are the following:
* Adding this default implementation increases the size of our SDK.
* Some customers don't want push notifications in their chat implementation.

With time, we'll dedicate more time to building and exposing more providers for you to use.

Let's see which providers we currently expose and how to use them!

### Stream Firebase PushProvider implementation
We provide an artifact with all the implementation needed to work with **Firebase**. To use it follow the next steps:

#### Step 1
Configure your app to use Firebase Cloud Messaging as described in the [Firebase documentation](https://firebase.google.com/docs/cloud-messaging/android/client).

You just need to set up the FCM dependencies and add a _google-services.json_ file to your project source directory.

#### Step 2
Add the Stream Firebase `PushProvider` artifact to your app-level `build.gradle` file:

```groovy
dependencies {
    implementation "io.getstream:stream-chat-android-pushprovider-firebase:$stream_version"
}
```

#### Step 3
Add the `FirebasePushDeviceGenerator` to your `NotificationConfig` and inject it into the `ChatClient`:

```kotlin
val notificationConfig = NotificationConfig(
  pushDeviceGenerators = listOf(FirebasePushDeviceGenerator()) // <--
)
ChatClient.Builder("apiKey", context)
    .notifications(ChatNotificationHandler(context, notificationsConfig)) // <--
    .build()
```

#### Implement Custom _FirebaseMessagingService_
If you're already using FCM in your App, you can use your custom `FirebaseMessagingService` to receive push notifications from Stream, like so:

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
        } catch (exception: Exception) {
            // ChatClient was not initialized or the data is missing
        }
    }
}
```

:::note
Make sure that `CustomMessageFirebaseService`'s priority is higher than `-1` to override the default service.
:::

That's all you have to do to integrate our Firebase push provider artifact.

### Stream Huawei PushProvider implementation - WIP

:::note
The Huawei push provider is still in progress, but we're working hard to build it. Stay tuned for more!
:::

## Customizing Push Notifications

If you want, you can also customize how the push notifications work.

There are two ways to customize push notifications:
* `NotificationConfig`: Allows to customize notification resources and meta data.
* `ChatNotificationHandler`: Allows to customize the default push notifications handling behaviour.

Let's see how to create a custom `ChatNotificationHandler`.

### Redirecting From Notification To App

In order to redirect the user from notifications to a specific `Activity` in your app, you need to create a custom `ChatNotificationHandler`.

Here's an example of a custom handler:

```kotlin
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

You extended the `ChatNotificationHandler` to provide a custom implementation. Notice the `getNewMessageIntent()` override.

This function allows you to intercept a notification and build a custom `Intent` that can point to any `Activity` in your app. You can also add custom data to the `Intent` if you need it.

Now that you've created a custom handler, you need to apply it to the `ChatClient`, like so:

```kotlin
val notificationHandler = MyNotificationHandler(context, notificationsConfig)

ChatClient.Builder("{{ api_key }}", context)
            .notifications(notificationHandler) <--
            .build()
```

:::note
Consider overriding `ChatNotificationHandler.getErrorCaseIntent()` for error case notifications.
:::

### Create a custom notification

The SDK also allows you to generate the notification UI that users see after they receive a push notification.

All you need to do is override `buildNotification()` as shown in the following example:

```kotlin
    class MyNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
        ChatNotificationHandler(context, notificationConfig) {

        override fun buildNotification(
            notificationId: Int,
            channel: Channel,
            message: Message
        ): NotificationCompat.Builder {
            // Build your notification
            return NotificationCompact.Builder(context, CHANNEL_NOTIFICATION_ID)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Customize your notification builder here
        }
    }
```

Within `buildNotification()` you receive the `notificationId` and the `Channel` and `Message` that this notification came from.

You can use the `NotificationCompat.Builder()` to customize the notification appearance and default values and behaviour.

Finally, once you're done, inject the `ChatNotificationHandler` into the `ChatClient`:

```kotlin
val notificationHandler = MyNotificationHandler(context, notificationsConfig)

ChatClient.Builder("{{ api_key }}", context)
            .notifications(notificationHandler)
            .build()
```

## Configuring Push Notifications on Stream Dashboard
To be able to receive push notifications from the Stream Server, you need to provide the `PushProvider` **credential** to Stream.

To do that, you need to use the Stream Dashboard.

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
Open the Stream Dashboard.

Navigate to the Chat Dashboard and enable _Android & Firebase_ push notifications switch.

![notifications step 4.1](../../assets/notifications_firebase_setup_step_4_1.jpeg)

Finally, add the _Server Key_:

![notifications step 4.2](../../assets/notifications_firebase_setup_step_4_2.jpeg)

#### Step 5
Save the push notification settings changes:
![notifications step 5](../../assets/notifications_firebase_setup_step_5.jpeg)

:::note
Remember to add the `google-services.json` file to your project source directory. For more information take a look at the [Firebase setup tutorial](https://firebase.google.com/docs/android/setup).
:::
