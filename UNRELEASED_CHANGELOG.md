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
Fix for position of deleted messages for other users
### ⬆️ Improved

### ✅ Added
Now it is possible to customise when the avatar appears in the conversation. It is
possible to use an avatar in messages from other users and for messages of the current user. Use
`setShowAvatarPredicate` to change the default behaviour. Default: The avatar shows only
for messages of other users, in the bottom of a group of messages (Position.Bottom).
Also now it is possible to apply a custom margin to messages of MessageListView. You need
to add margins to avoid overlap between messages and avatars.

If you set a predicate that shows avatars for your own messages as well, use this value:

```
streamUiMessageEndMargin=">@dimen/stream_ui_message_viewholder_avatar_missing_margin"
```

If your predicate doesn't show avatars for your own messages (this is the default behavior), remove the end margin:

```
streamUiMessageEndMargin="0dp"
```


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
