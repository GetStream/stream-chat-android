## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed
Fixing filter for hidden and draft channels. Those channels were not showing in the results,
even when the user asked for then. Now this is fixed and the draft and hidden channels can be included
in the ChannelsView
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-client
### 🐞 Fixed

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

### ✅ Added
- Added `ChatClient::truncateChannel` and `ChannelClient::truncate` methods to remove messages from a channel.
### ⚠️ Changed

### ❌ Removed

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
Fixing filter for hidden and draft channels. Those channels were not showing in the results,
even when the user asked for then. Now this is fixed and the draft and hidden channels can be included
in the ChannelListView.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
