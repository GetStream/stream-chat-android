## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

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
- Improved `ChatClient::pinMessage` and `ChatClient::unpinMessage`. Now the methods use partial message updates and the data in other `Message` fields is not lost.

### ✅ Added
- Added `Channel::isMutedFor` extension function which might be used to check if the Channel is muted for User
- Added `ChatClient::partialUpdateMessage` method to update specific `Message` fields retaining the other fields

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed
- Fixed updating `ChannelController::muted` value

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

### ⬆️ Improved

### ✅ Added
- Added `MessageListItem.ThreadPlaceholderItem` and corresponding `THREAD_PLACEHOLDER` view type which can be used to implement an empty thread placeholder.
- Added `authorLink` to `Attachment` - the link to the website

### ⚠️ Changed

### ❌ Removed
- Removed `UrlSigner` class

## stream-chat-android-compose
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- Moved permissions and queries from the compose sample app `AndroidManifest.xml` to the SDK `AndroidManifest.xml` so users don't have to add permissions themselves.

### ❌ Removed
