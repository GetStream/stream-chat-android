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

### ⚠️ Changed

### ❌ Removed
- Removed `Channel::isMuted` extension. Use `User::channelMutes` or subscribe for `NotificationChannelMutesUpdatedEvent` to get information about muted channels.


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
- Fixed crash caused by missing `streamUiReplyAvatarStyle`

### ⬆️ Improved

### ✅ Added
- Now you can configure the style of `AttachmentMediaActivity`.
- Added new properties allowing customizing `MessageInputView` using `MessageInputViewStyle` and `AttachmentSelectionDialogStyle`:
    - `MessageInputViewStyle.fileNameTextStyle`
    - `MessageInputViewStyle.fileSizeTextStyle`
    - `MessageInputViewStyle.fileCheckboxSelectorDrawable`
    - `MessageInputViewStyle.fileCheckboxTextColor`
    - `MessageInputViewStyle.fileAttachmentEmptyStateTextStyle`
    - `MessageInputViewStyle.mediaAttachmentEmptyStateTextStyle`
    - `MessageInputViewStyle.fileAttachmentEmptyStateText`
    - `MessageInputViewStyle.mediaAttachmentEmptyStateText`
    - `MessageInputViewStyle.dismissIconDrawable`
    - `AttachmentSelectionDialogStyle.allowAccessToGalleryText`
    - `AttachmentSelectionDialogStyle.allowAccessToFilesText`
    - `AttachmentSelectionDialogStyle.allowAccessToCameraText`
    - `AttachmentSelectionDialogStyle.allowAccessToGalleryIcon`
    - `AttachmentSelectionDialogStyle.allowAccessToFilesIcon`
    - `AttachmentSelectionDialogStyle.allowAccessToCameraIcon`
    - `AttachmentSelectionDialogStyle.grantPermissionsTextStyle`
    - `AttachmentSelectionDialogStyle.recentFilesTextStyle`
    - `AttachmentSelectionDialogStyle.recentFilesText`
    - `AttachmentSelectionDialogStyle.fileManagerIcon`
    - `AttachmentSelectionDialogStyle.videoDurationTextStyle`
    - `AttachmentSelectionDialogStyle.videoIconDrawable`
    - `AttachmentSelectionDialogStyle.videoIconVisible`
    - `AttachmentSelectionDialogStyle.videoLengthLabelVisible`

### ⚠️ Changed

### ❌ Removed
