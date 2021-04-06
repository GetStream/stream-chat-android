## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed
- Fix Attachment Gravity

### ⬆️ Improved

### ✅ Added
- Provide AvatarView class

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
- Fix Crash on some devices that are not able to create an Encrypted SharedPreferences
- Fixed the message read indicator in the message list
- Added missing `team` field to `ChannelEntity` and `ChannelData`
### ⬆️ Improved

### ✅ Added
- Add `ChatDomain::removeMembers` method
- Added the following use case functions to `ChatDomain` which are supposed to replace `ChatDomain.useCases` property:
* `ChatDomain::replayEventsForActiveChannelsCall`
* `ChatDomain::getChannelControllerCall`
* `ChatDomain::watchChannelCall`
* `ChatDomain::queryChannelsCall`
* `ChatDomain::getThreadCall`
* `ChatDomain::loadOlderMessagesCall`
* `ChatDomain::loadNewerMessagesCall`
* `ChatDomain::loadMessageByIdCall`
* `ChatDomain::queryChannelsLoadMoreCall`
* `ChatDomain::threadLoadMoreCall`
* `ChatDomain::createChannelCall`
* `ChatDomain::sendMessageCall`
* `ChatDomain::sendMessageCall`
* `ChatDomain::cancelMessageCall`
* `ChatDomain::shuffleGiphyCall`
* `ChatDomain::sendGiphyCall`
* `ChatDomain::editMessageCall`
* `ChatDomain::deleteMessageCall`
* `ChatDomain::sendReactionCall`
* `ChatDomain::deleteReactionCall`
* `ChatDomain::keystrokeCall`
* `ChatDomain::stopTypingCall`
* `ChatDomain::markReadCall`
* `ChatDomain::markAllReadCall`
* `ChatDomain::hideChannelCall`
* `ChatDomain::showChannelCall`
* `ChatDomain::leaveChannelCall`
* `ChatDomain::deleteChannelCall`
* `ChatDomain::setMessageForReplyCall`
* `ChatDomain::downloadAttachmentCall`
* `ChatDomain::searchUsersByNameCall`
* `ChatDomain::queryMembersCall`

### ⚠️ Changed
- Deprecated `ChatDomain.useCases`. It has `DeprecationLevel.Warning` and still can be used. However, it will be not available in the future, so please consider migrating to use `ChatDomain` use case functions instead.

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed getting files provided by content resolver.

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
Added theme to all activities all the SDK. You can override then in your project by redefining the styles:
- StreamUiAttachmentGalleryActivityStyle
- StreamUiAttachmentMediaActivityStyle
- StreamUiAttachmentActivityStyle

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attr streamUiCopyMessageActionEnabled. From color to boolean.
- Now it is possible to change the color of `MessageListHeaderView` from the XML.
- Fixed the `MessageListView::setUserClickListener` method.
- Fixed bugs in handling empty states for `ChannelListView`. Deprecated manual methods for showing/hiding empty state changes.
- Fix `ChannelListHeaderView`'s title position when user avatar or action button is invisible
- Fix UI behaviour for in-progress file uploads
- Fix extension problems with file uploads when attachment names contain spaces

### ⬆️ Improved

### ✅ Added
- Now it is possible to change the back button of MessageListHeaderView using `app:streamUiMessageListHeaderBackButtonIcon`
### ⚠️ Changed

### ❌ Removed
