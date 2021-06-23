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

### ✅ Added
- Added `ChannelClient::sendEvent` method which allows to send custom events.
- Added nullable `User` field to `UnknownEvent`.

### ⚠️ Changed

### ❌ Removed
- Removed the `Message::attachmentsSyncStatus` field


## stream-chat-android-offline
### 🐞 Fixed
- Fixed `in` and `nin` filters when filtering by extra data field that is an array.
- Fixed crash when adding a reaction to a thread message.

### ⬆️ Improved
- Now attachments can be sent while being in offline

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- Made `AttachmentSelectionDialogFragment` public. Use `newInstance` to create instances of this Fragment.

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved
- Hide suggestion list popup when keyboard is hidden.

### ✅ Added
- Added the `MessageInputView::hideSuggestionList` method to hide the suggestion list popup.

### ⚠️ Changed

### ❌ Removed
