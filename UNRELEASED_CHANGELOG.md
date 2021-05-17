## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed
Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelsView`.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-client
### 🐞 Fixed
- Fixed querying banned users using new serialization.
- Fixed the bug when wrong credentials lead to inability to login

### ⬆️ Improved

### ✅ Added
- Added `ChatClient::truncateChannel` and `ChannelClient::truncate` methods to remove messages from a channel.
- Added enum `DisconnectCause` to `DisconnectedEvent`
- Added method `SocketListener::onDisconnected(cause: DisconnectCause)`

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
Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelListView`.
### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
