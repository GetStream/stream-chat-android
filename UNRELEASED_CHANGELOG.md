## Common changes for all artifacts
### 🐞 Fixed
- Fixed channel list sorting
### ⬆️ Improved
- Updated to Kotlin 1.5.10, coroutines 1.5.0
- Updated to Android Gradle Plugin 4.2.1
- Updated Room version to 2.3.0
- Updated Firebase, AndroidX, and other dependency versions to latest, [see here](https://github.com/GetStream/stream-chat-android/pull/1895) for more details
- Marked many library interfaces that should not be implemented by clients as [sealed](https://kotlinlang.org/docs/sealed-classes.html)

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed
- Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelsView`.
- Fixed link preview UI issues in old-ui package
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-client
### 🐞 Fixed
- Fixed querying banned users using new serialization.
- Fixed the bug when wrong credentials lead to inability to login
- Fixed issues with Proguard stripping response classes in new serialization implementation incorrectly

### ⬆️ Improved
- Improved handling push notifications:
    - Added `ChatClient.handleRemoteMessage` for remote message handling
    - Added `ChatClient.setFirebaseToken` for setting Firebase token
    - Deprecated `ChatClient.handleRemoteMessage`
    - Deprecated `ChatClient.onNewTokenReceived`
    - Deprecated `ChatNotificationHandler.getSmallIcon`
    - Deprecated `ChatNotificationHandler.getFirebaseMessageIdKey`
    - Deprecated `ChatNotificationHandler.getFirebaseChannelIdKey`
    - Deprecated `ChatNotificationHandler.getFirebaseChannelTypeKey`
- Improved error description provided by `ChatClient::sendImage`, `ChatClient::sendFile`, `ChannelClient::sendImage` and `ChannelClient::sendFile` methods if upload fails. 

### ✅ Added
- Added `ChatClient::truncateChannel` and `ChannelClient::truncate` methods to remove messages from a channel.
- Added `DisconnectCause` to `DisconnectedEvent`
- Added method `SocketListener::onDisconnected(cause: DisconnectCause)`
- Added possibility to group notifications:
    - Notifications grouping is disabled by default and can be enabled using `NotificationConfig::shouldGroupNotifications`
    - If enabled, by default notifications are grouped by Channel's cid
    - Notifications grouping can be configured using `ChatNotificationHandler` and `NotificationConfig`
- Added `ChatNotificationHandler::getFirebaseInstallations()` method in place of `ChatNotificationHandler::getFirebaseInstanceId()`. 
It should be used now to fetch Firebase token in the following way: `handler.getFirebaseInstallations()?.getToken(true)?.addOnCompleteListener {...}`.

### ⚠️ Changed
- Changed the return type of `FileUploader` methods from nullable string to `Result<String>`.
- Updated `firebase-messaging` library to the version `22.0.0`. Removed deprecated `FirebaseInstanceId` invocations from the project. 

### ❌ Removed
- `ChatNotificationHandler::getFirebaseInstanceId()` due to `FirebaseInstanceId` being deprecated. It's replaced now with `ChatNotificationHandler::getFirebaseInstallations()`.

## stream-chat-android-offline
### 🐞 Fixed

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

## stream-chat-android-ui-components
### 🐞 Fixed
Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelListView`.
Fixed bug when for some video attachments activity with media player wasn't shown.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- Swipe options of `ChannelListView` component:
    - "Channel more" option is now not shown by default because we are not able to provide generic, default implementation for it. 
    - "Channel delete" option has now default implementation. Clicking on the "delete" icon shows AlertDialog asking to confirm Channel deletion operation.

### ❌ Removed
