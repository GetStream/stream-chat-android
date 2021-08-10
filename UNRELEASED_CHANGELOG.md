## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- Updated the Kotlin version to latest supported - `1.5.21`.

### ❌ Removed


## stream-chat-android
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-client
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- `PushMessage` class created to store Push Notification data
- `PushDeviceGenerator` interface to obtain the Push Token and create the `Device`

### ⚠️ Changed
- `Device` class has an extra attribute with the `PushProvider` used on this device
- Breaking change: `ChatClient.setDevice()` and `ChatClient.addDevice()` now receive a `device` instance, instead of only receive the push provider token
- `RemoteMessage` from Firebase is not used anymore inside of our SDK, now it needs to be used with `PushMessage` class
- `NotificationConfig` has a new list of `PushDeviceGenerator` instance to be used for generating the Push Notification Token. If you were using `Firebase` as your Push Notification Provider, you need to add `FirebasePushDeviceGenerator` to your `NotificationConfig` object to continue working as before. `FirebasePushDeviceGenerator` receive by constructor the default `FirebaseMessaging` instance to be used, if you would like to use your own instance and no the default one, you can inject it by constructor. Unneeded Firebase properties have been removed from this class.

### ❌ Removed
- 🚨 Breaking change: Remove `ChatClient.isValidRemoteMessage()` method. It needs to be handled outside
- 🚨 Breaking change: Remove `ChatClient.handleRemoteMessage(RemoteMessage)`. Now it needs to be used `ChatClient.handlePushMessage(PushMessage)`


## stream-chat-android-offline
### 🐞 Fixed
- Fixed the event sync process when connection is recovered

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
- Removed unnecessary "draft" filter from the default channel list filter as it is only relevant to the sample app

## stream-chat-android-ui-components
### 🐞 Fixed
Fixed attachments of camera. Now multiple videos and pictures can be taken from the camera.
### ⬆️ Improved

### ✅ Added
- Added `MessageListView::setDeletedMessageListItemPredicate` function. It's responsible for adjusting visibility of the deleted `MessageListItem.MessageItem` elements.

- Positions of messages are now a mutableList.
- Possibility to transform MessageItems before the are displayed in the screen.
Use the `MessageListView.setMessageItemTransformer` for make the necessary transformation. This example makes groups of messages if they were created less then an hours apart:
```
binding.messageListView.setMessageItemTransformer { list ->
    list.forEachIndexed { i, messageItem ->
        if (i == list.lastIndex) {
            messageItem.positions.clear()
            messageItem.positions.add(MessageListItem.Position.BOTTOM)
        } else if (i < list.lastIndex - 1) {
            val thisInstant = messageItem.message.createdAt?.time?.let(Instant::ofEpochMilli)
            val nextInstant = list[i + 1].message.createdAt?.time?.let(Instant::ofEpochMilli)

            if (nextInstant?.isAfter(thisInstant?.plus(1, ChronoUnit.HOURS)) == true) {
                messageItem.positions.clear()
                messageItem.positions.add(MessageListItem.Position.BOTTOM)
            } else {
                messageItem.positions.remove(MessageListItem.Position.BOTTOM)
            }
        }
    }
}
```

### ⚠️ Changed
- 🚨 Breaking change: the deleted `MessageListItem.MessageItem` elements are now displayed by default to all the users. This default behavior can be customized using `MessageListView::setDeletedMessageListItemPredicate` function. This function takes an instance of `MessageListItemPredicate`. You can pass one of the following objects:
    * `DeletedMessageListItemPredicate.VisibleToEveryone`
    * `DeletedMessageListItemPredicate.NotVisibleToAnyone`
    * or `DeletedMessageListItemPredicate.VisibleToAuthorOnly`
    Alternatively you can pass your custom implementation by implementing the `MessageListItemPredicate` interface if you need to customize it more deeply.

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where we didn't use the `Channel.getDisplayName()` logic for the `MessageListHeader`.

### ⬆️ Improved
- Updated Jetpack Compose to `1.0.1`
- Updated Accompanist libraries to `0.16.0`
- Updated KTX Activity to `1.3.1`
- Exposed functionality for getting the `displayName` of `Channel`s.
- Added updated logic to Link preview attachments, which chooses either the `titleLink` or the `ogUrl` when loading the data, depending on which exists .

### ✅ Added

### ⚠️ Changed
- `ViewModel`s now initialize automatically, so you no longer have to call `start()` on them. This is aimed to improve the consistency between our SDKs.

### ❌ Removed
