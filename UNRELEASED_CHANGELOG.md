## Common changes for all artifacts
### 🐞 Fixed
Group channels with 1<>1 behaviour the same way as group channels with many users
It is not possible to remove users from distinct channels anymore.
### ⬆️ Improved
it is now possible to configure the max lines of a link description. Just use
`app:streamUiLinkDescriptionMaxLines` when defining MessageListView
### ✅ Added
Configure enable/disable of replies using XML in `MessageListView`
Option `app:streamUiReactionsEnabled` in `MessageListView` to enable or disable reactions

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
- Fixed parsing of `createdAt` property in `MessageDeletedEvent`

### ⬆️ Improved

### ✅ Added
- **Added a brand new serialization implementation, available as an opt-in API for now.** This can be enabled by making a `useNewSerialization(true)` call on the `ChatClient.Builder`.
    - This new implementation will be more performant and greatly improve type safety in the networking code of the SDK.
    - The old implementation remains the default for now, while we're making sure the new one is bug-free.
    - We recommend that you opt-in to the new implementation and test your app with it, so that you can report any issues early and we can get them fixed before a general rollout.
- Added `unflagMessage(messageId)` and `unflagUser(userId)` methods to `ChatClient`
- Added support for querying banned users - added `ChatClient::queryBannedUsers` and `ChannelClient::queryBannedUsers`
- Added `uploadsEnabled`, `urlEnrichmentEnabled`, `customEventsEnabled`, `pushNotificationsEnabled`, `messageRetention`, `automodBehavior` and `blocklistBehavior` fields to channel config

### ⚠️ Changed
- Renamed `ChannelId` property to `channelId` in both `ChannelDeletedEvent` and `NotificationChannelDeletedEvent`
- Deprecated `ChatClient::unMuteChannel`, the `ChatClient::unmuteChannel` method should be used instead
- Deprecated `ChatClient::unBanUser`, the `ChatClient::unbanUser` method should be used instead
- Deprecated `ChannelClient::unBanUser`, the `ChannelClient::unbanUser` method should be used instead
- Deprecated `ChannelController::unBanUser`, the `ChannelController::unbanUser` method should be used instead

### ❌ Removed

## stream-chat-android-offline
### 🐞 Fixed
- Fixed an issue that didn't find the user when obtaining the list of messages
- Fix refreshing not messaging channels which don't contain current user as a member

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved
- Show AttachmentMediaActivity for video attachments

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-components
### 🐞 Fixed
- Now replied messages are shown correctly with the replied part in message options
- `MessageListView::enterThreadListener` is properly notified when entering into a thread

### ⬆️ Improved
- Add support of non-image attachment types to the default attachment click listener.

### ✅ Added
- Replacing `ChatUI` with new `io.getstream.chat.android.ui.ChatUI` implementation
- Added possibility to configure delete message option visibility using `streamUiDeleteMessageEnabled` attribute, and `MessageListView::setDeleteMessageEnabled` method
- Add `streamUiEditMessageEnabled` attribute to `MessageListView` and `MessageListView::setEditMessageEnabled` method to enable/disable the message editing feature
- Add `streamUiMentionsEnabled` attribute to `MessageInputView` and `MessageInputView::setMentionsEnabled` method to enable/disable mentions
- Add `streamUiThreadsEnabled` attribute to `MessageListView` and `MessageListView::setThreadsEnabled` method to enable/disable the thread replies feature
- Add `streamUiCommandsEnabled` attribute to `MessageInputView` and `MessageInputView::setCommandsEnabled` method to enable/disable commands
- Add `ChannelListItemPredicate` to our `channelListView` to allow filter `ChannelListItem` before they are rendered
- Open `AvatarBitmapFactory` class
- Add `AvatarBitmapFactory::setInstance` method to allow custom implementation of `AvatarBitmapFactory`
- Add `StyleTransformer` class to allow application-wide style customizations

### ⚠️ Changed
- Deprecated `ChatUI` class

### ❌ Removed
