## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
Message options list changed colour for dark version. The colour is a little lighters
now, what makes it easier to see. 
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

### ⚠️ Changed
- Refactored `FilterObject` class  - see the [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-FilterObject) for more info

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed
- Fixed refreshing channel list after removing member
- Fixed an issue that didn't find the user when obtaining the list of messages

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.
- Deprecated constructors for `ChatDomain.Builder` with the `User` type parameter, use constructor with `Context` and `ChatClient` instead.

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed some rare crashes when `MessageListView` was created without any attribute info present

### ⬆️ Improved
- Updated PhotoView to version 2.3.0

### ✅ Added
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Introduced `TextAndAttachmentsViewHolder` for any combination of attachment content and text

### ⚠️ Changed

### ❌ Removed
- Deleted `OnlyFileAttachmentsViewHolder`, `OnlyMediaAttachmentsViewHolder`,
  `PlainTextWithMediaAttachmentsViewHolder` and `PlainTextWithFileAttachmentsViewHolder`
