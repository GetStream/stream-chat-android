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
  * `ChatDomain::replayEventsForActiveChannels` Adds the provided channel to the active channels and replays events for all active channels.
  * `ChatDomain::getChannelController` Returns a `ChannelController` for given cid.
  * `ChatDomain::watchChannel` Watches the given channel and returns a `ChannelController`.
  * `ChatDomain::queryChannels` Queries offline storage and the API for channels matching the filter. Returns a queryChannelsController.
  * `ChatDomain::getThread` Returns a thread controller for the given channel and message id.
  * `ChatDomain::loadOlderMessages` Loads older messages for the channel.
  * `ChatDomain::loadNewerMessages` Loads newer messages for the channel.
  * `ChatDomain::loadMessageById` Loads message for a given message id and channel id.
  * `ChatDomain::queryChannelsLoadMore` Load more channels for query.
  * `ChatDomain::threadLoadMore` Loads more messages for the specified thread.
  * `ChatDomain::createChannel` Creates a new channel.
  * `ChatDomain::sendMessage` Sends the message.
  * `ChatDomain::cancelMessage` Cancels the message of "ephemeral" type.
  * `ChatDomain::shuffleGiphy` Performs giphy shuffle operation.
  * `ChatDomain::sendGiphy` Sends selected giphy message to the channel.
  * `ChatDomain::editMessage` Edits the specified message.
  * `ChatDomain::deleteMessage` Deletes the specified message.
  * `ChatDomain::sendReaction` Sends the reaction.
  * `ChatDomain::deleteReaction` Deletes the specified reaction.
  * `ChatDomain::keystroke` It should be called whenever a user enters text into the message input.
  * `ChatDomain::stopTyping` It should be called when the user submits the text and finishes typing.
  * `ChatDomain::markRead` Marks all messages of the specified channel as read.
  * `ChatDomain::markAllRead` Marks all messages as read.
  * `ChatDomain::hideChannel` Hides the channel with the specified id.
  * `ChatDomain::showChannel` Shows a channel that was previously hidden.
  * `ChatDomain::leaveChannel` Leaves the channel with the specified id.
  * `ChatDomain::deleteChannel` Deletes the channel with the specified id.
  * `ChatDomain::setMessageForReply` Set the reply state for the channel.
  * `ChatDomain::downloadAttachment` Downloads the selected attachment to the "Download" folder in the public external storage directory.
  * `ChatDomain::searchUsersByName` Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name in local database.
  * `ChatDomain::queryMembers` Query members of a channel.
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
