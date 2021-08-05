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
- The following `Message` fields are now persisted to the database: `pinned`, `pinnedAt`, `pinExpires`, `pinnedBy`, `channelInfo`, `replyMessageId`.

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
- Added a fix for default view for empty state of ChannelListView.
- Fixed memory leaks for FileAttachmentsView.
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
- Exposed `DefaultMessageContainer` as a public component so users can use it as a fallback
- Exposed an `isMine` property on `MessageItem`s, for ease of use.
- Allowed for customization of `MessageList` (specifically `Messages`) component background, through a `modifier.background()` parameter.
- Allowed for better message customization before sending the message.

### ✅ Added

### ⚠️ Changed
- Moved permissions and queries from the compose sample app `AndroidManifest.xml` to the SDK `AndroidManifest.xml` so users don't have to add permissions themselves.
- Changed the exposed type of the `MessageComposer`'s `onSendMessage` handler. This way people can customize messages before we send them to the API.

### ❌ Removed
- Removed `currentUser` parameter from `DefaultMessageContainer` and some other components that relied on ID comparison to know which message is ours/theirs.
- Removed default background color on `Messages` component, so that users can customize it by passing in a `modifier`.
