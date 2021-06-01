---
id: client-configuring-push-notifications
title: Configuring Push Notifications
sidebar_position: 1
---

Android SDK comes up with the default, customizable push notifications handling mechanism which includes:
1. Receiving push notification using _FirebaseMessagingService_
2. Loading require data using _Worker_
3. Showing notification with default actions

See [Customizing Push Notifications](./client-customizing-push-notifications) for customization options.

> **NOTE:** Make sure _ChatClient_ is initialized before handling push notification. We highly recommend initializing it in the `Application` class.

In order to configure push notifications on Android devices, you need to:

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

> **NOTE:** Remember to add _google-services.json_ file to your project source directory. For more information take a look at [Firebase setup tutorial](https://firebase.google.com/docs/android/setup).

#### Step 6
Setup the following push notification data payload at Stream Dashboard:
```json
{
  "message_id": "{{ message.id }}",
  "channel_id": "{{ channel.id }}",
  "channel_type": "{{ channel.type }}"
}
```

#### Step 7
Initialize _ChatClient_ with _NotificationConfig_:

```kotlin
val notificationsConfig = NotificationConfig(
    firebaseMessageIdKey = "message_id",
    firebaseChannelIdKey = "channel_id",
    firebaseChannelTypeKey = "channel_type",
)

ChatClient.Builder("apiKey", context)
    .notifications(ChatNotificationHandler(context, notificationsConfig))
    .build()
```
