## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved
- Now we provide SNAPSHOT versions of our SDK for every commit arrives to the `develop` branch.
They shouldn't be used for a production release because they could contains some known bugs or breaking changes that will be fixed before a normal version is released, but you can use them to fetch last changes from our SDK
To use them you need add a new maven repository to your `build.gradle` file and use the SNAPSHOT.
```
 maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
```
Giving that our last SDK version is `X.Y.Z`, the SNAPSHOT version would be `X.Y.(Z+1)-SNAPSHOT`

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
