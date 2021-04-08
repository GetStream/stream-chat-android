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

## stream-chat-android-offline
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- Added the following use case functions to `ChatDomain` which are supposed to replace `ChatDomain.useCases` property:
* `ChatDomain::replayEventsForActiveChannels`
* `ChatDomain::getChannelController`
* `ChatDomain::watchChannel`
* `ChatDomain::queryChannels`
* `ChatDomain::getThread`
* `ChatDomain::loadOlderMessages`
* `ChatDomain::loadNewerMessages`
* `ChatDomain::loadMessageById`
* `ChatDomain::queryChannelsLoadMore`
* `ChatDomain::threadLoadMore`
* `ChatDomain::createChannel`
* `ChatDomain::sendMessage`
* `ChatDomain::sendMessage`
* `ChatDomain::cancelMessage`
* `ChatDomain::shuffleGiphy`
* `ChatDomain::sendGiphy`
* `ChatDomain::editMessage`
* `ChatDomain::deleteMessage`
* `ChatDomain::sendReaction`
* `ChatDomain::deleteReaction`
* `ChatDomain::keystroke`
* `ChatDomain::stopTyping`
* `ChatDomain::markRead`
* `ChatDomain::markAllRead`
* `ChatDomain::hideChannel`
* `ChatDomain::showChannel`
* `ChatDomain::leaveChannel`
* `ChatDomain::deleteChannel`
* `ChatDomain::setMessageForReply`
* `ChatDomain::downloadAttachment`
* `ChatDomain::searchUsersByName`
* `ChatDomain::queryMembers`
- Add `ChatDomain::removeMembers` method

### ⚠️ Changed
- Deprecated `ChatDomain.useCases`. It has `DeprecationLevel.Warning` and still can be used. However, it will be not available in the future, so please consider migrating to use `ChatDomain` use case functions instead.

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

### ⚠️ Changed

### ❌ Removed
