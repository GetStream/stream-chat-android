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
- `TooManyRequestsException` caused to be subscribed multiple times to the `ConnectivityManager`

### ⬆️ Improved
- Reconnection process

### ✅ Added

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
- Fixed a bug in state handling for anonymous users.

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
Fix for position of deleted messages for other users
### ⬆️ Improved

### ✅ Added
- Now it is possible to customize when the avatar appears in the conversation. It is possible to use an avatar in messages from other users and for messages of the current user. You can check it here:  https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#configure-when-avatar-appears
- Added support for slow mode. Users are no longer able to send messages during cooldown interval.
- Added possibility to customize the appearance of cooldown timer in the `MessageInputView` using the following attributes:
  - `streamUiCooldownTimerTextSize`, `streamUiCooldownTimerTextColor`, `streamUiCooldownTimerFontAssets`, `streamUiCooldownTimerFont`, `streamUiCooldownTimerTextStyle` attributes to customize cooldown timer text
  - `cooldownTimerBackgroundDrawable`- the background drawable for cooldown timer

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed

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
