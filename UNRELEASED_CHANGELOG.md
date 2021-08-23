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
- Fixed a serialization error when editing messages that are replies

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
- Fixed a bug in state handling for anonymous users. 
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
- Fixed a bug where selected File attachment icons were clipped.
- Fixed a bug where image file attachments weren't shown as thumbnails.

### ⬆️ Improved
- Added default date and time formatting to Channel and Message items.
- Improved attachments API by providing cleaner examples of attachment factories.
- Updated documentation & examples.
- Decoupled attachment content to specific attachment files.
- Decouple message attachment content to a `MessageAttachmentsContent` component.
- Re-structured SDK module to accommodate a new `attachment` package.

### ✅ Added
- Added `DateFormatter` option to the `ChatTheme`, to allow for date format customization across the app.
- Added a `Timestamp` component that encapsulates date formatting.
- Added a way to customize and override if messages use unique reactions.

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
