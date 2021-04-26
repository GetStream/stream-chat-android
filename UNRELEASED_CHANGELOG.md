## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed

### ⬆️ Improved
* Updated coil dependency to the latest version. This fixes problem with .heic, and .heif attachment metadata parsing. 

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-client
### 🐞 Fixed
- Optimized the number of `ChatClient::addDevice` API calls

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-offline
### 🐞 Fixed
- Fixed offline reactions sync

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed crash related to accessing `ChatDomain::currentUser` in `MessageListViewModel` before user is connected

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved
* Updated coil dependency to the latest version. This fixes problem with .heic, and .heif attachment metadata parsing. 

### ✅ Added
Customization of icons in Attachment selection dialog
you can use:
- app:streamUiPictureAttachmentIcon
Change the icon for the first item in the list of icons
- app:streamUiPictureAttachmentIconTint
Change the tint color for icon of picture selection
- app:streamUiFileAttachmentIcon
Change the icon for the second item in the list of icons
- app:streamUiFileAttachmentIconTint
Change the tint color for icon of file selection
- app:streamUiCameraAttachmentIcon
Change the icon for the third item in the list of icons
- app:streamUiCameraAttachmentIconTint
Change the tint color for icon of camera selection

### ⚠️ Changed

### ❌ Removed
