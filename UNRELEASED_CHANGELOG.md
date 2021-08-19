## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved
- Updated Target API Level to 30
- Updated dependency versions
  - Coil 1.3.2
  - AndroidX Activity 1.3.1
  - AndroidX Startup 1.1.0
  - AndroidX ConstraintLayout 2.1.0
  - Dokka 1.5.0
  - Lottie 4.0.0
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
- Added the `expiration` parameter to `ChatClient::muteChannel`, `ChannelClient:mute` methods
- Added the `timeout` parameter to `ChatClient::muteUser`, `ChannelClient:mute::muteUser` methods

### ⚠️ Changed
- Allow specifying multiple attachment's type when getting messages with attachments:
  - Deprecated `ChatClient::getMessagesWithAttachments` with `type` parameter. Use `ChatClient::getMessagesWithAttachments` function with types list instead
  - Deprecated `ChannelClient::getMessagesWithAttachments` with `type` parameter. Use `ChannelClient::getMessagesWithAttachments` function with types list instead

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

### ⬆️ Improved

### ✅ Added
- Added self-contained higher-level UI components:
  - `ChannelListFragment` - channel list screen which internally contains `ChannelListHeaderView`, `ChannelListView`, `SearchInputView`, `SearchResultListView`.
  - `ChannelListActivity` - thin wrapper around `ChannelListFragment`
  - `MessageListFragment` - message list screen which internally contains `MessageListHeaderView`, `MessageListView`, `MessageInputView`.
  - `MessageListActivity` - thin wrapper around `MessageListFragment`
    
### ⚠️ Changed

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed
- Added missing `emptyContent` and `loadingContent` parameters to `MessageList` inner components.

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
