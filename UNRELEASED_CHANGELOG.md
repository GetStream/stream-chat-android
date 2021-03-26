## Common changes for all artifacts
### 🐞 Fixed
Group channels with 1<>1 behaviour the same way as group channels with many users
It is not possible to remove users from distinct channels anymore.
### ⬆️ Improved
it is now possible to configure the max lines of a link description. Just use
`app:streamUiLinkDescriptionMaxLines` when defining MessageListView

It is now possible to configure the max size of files and an alert is shown when
a files bigger than this is selected.
### ✅ Added
Configure enable/disable of replies using XML in `MessageListView`
Option `app:streamUiReactionsEnabled` in `MessageListView` to enable or disable reactions
It is possible now to configure the max size of the file upload using
`app:streamUiAttachmentMaxFileSizeMb`

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed
- Fixed crash when sending GIF from Samsung keyboard

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-client
### 🐞 Fixed
- Fixed parsing of `createdAt` property in `MessageDeletedEvent`

### ⬆️ Improved
- Postponed queries as run as non-blocking

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
- Fix initial controls state in `MessageInputView`
- Fix crashing when open attachments destination

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
- Add the default font field to `TextStyle`
- Add new method `ChatFonts::setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface)`
- Add attributes for `MessageListView` in order to customize styles of:
    - Mine message text
    - Theirs message text
    - User name text in footer of Message
    - Message date in footer of Message
    - Thread replies counter in footer of Message
    - Link title text
    - Link description text
    - Date separator text
    - Deleted message text and background
    - Reactions style in list view and in options view
    - Indicator icons in footer of Message
It is now possible to customize the following attributes for `ChannelListView`:
- `streamUiChannelOptionsIcon` - customize options icon
- `streamUiChannelDeleteIcon` - customize delete icon
- `streamUiChannelOptionsEnabled` - hide/show options icon
- `streamUiChannelDeleteEnabled` - hide/show delete button
- `streamUiSwipeEnabled` - enable/disable swipe action
- `streamUiBackgroundLayoutColor` - customize the color of "background layout"
- `streamUiChannelTitleTextSize` - customize channel name text size
- `streamUiChannelTitleTextColor` - customize channel name text color
- `streamUiChannelTitleTextFont` - customize channel name text font
- `streamUiChannelTitleFontAssets` - customize channel name font asset
- `streamUiChannelTitleTextStyle` - customize channel name text style (normal / bold / italic)
- `streamUiLastMessageTextSize` - customize last message text size
- `streamUiLastMessageTextColor` - customize last message text color
- `streamUiLastMessageTextFont` - customize last message text font
- `streamUiLastMessageFontAssets` - customize last message font asset
- `streamUiLastMessageTextStyle` - customize last message text style (normal / bold / italic)
- `streamUiLastMessageDateTextSize` - customize last message date text size
- `streamUiLastMessageDateTextColor` - customize last message date text color
- `streamUiLastMessageDateTextFont` - customize last message date text font
- `streamUiLastMessageDateFontAssets` - customize last message date font asset
- `streamUiLastMessageDateTextStyle` - customize last message date text style (normal / bold / italic)
- `streamUiIndicatorSentIcon` - customize drawable indicator for sent
- `streamUiIndicatorReadIcon` - customize drawable indicator for read
- `streamUiIndicatorPendingSyncIcon` - customize drawable indicator for pending sync
- `streamUiForegroundLayoutColor` - customize the color of "foreground layout"
- `streamUiUnreadMessageCounterBackgroundColor` - customize the color of message counter badge
- `streamUiUnreadMessageCounterTextSize` - customize message counter text size
- `streamUiUnreadMessageCounterTextColor` - customize message counter text color
- `streamUiUnreadMessageCounterTextFont` - customize message counter text font
- `streamUiUnreadMessageCounterFontAssets` - customize message counter font asset
- `streamUiUnreadMessageCounterTextStyle` - customize message counter text style (normal / bold / italic)

- Option `app:streamUiReactionsEnabled` in `MessageListView` to enable or disable reactions

It is now possible to configure new fields in MessageInputView:
- `streamUiMessageInputTextStyle` - customize message input text style.
- `streamUiMessageInputFont` - customize message input text font.
- `streamUiMessageInputFontAssets` - customize message input text font assets.
- `streamUiMessageInputEditTextBackgroundDrawable` - customize message input EditText drawable.
- `streamUiMessageInputCustomCursorDrawable` - customize message input EditText cursor drawable.
- `streamUiCommandsTitleTextSize` - customize command title text size
- `streamUiCommandsTitleTextColor` - customize command title text color
- `streamUiCommandsTitleFontAssets` - customize command title text color
- `streamUiCommandsTitleTextColor` - customize command title font asset
- `streamUiCommandsTitleFont` - customize command title text font
- `streamUiCommandsTitleStyle` - customize command title text style
- `streamUiCommandsNameTextSize` - customize command name text size
- `streamUiCommandsNameTextColor` - customize command name text color
- `streamUiCommandsNameFontAssets` - customize command name text color
- `streamUiCommandsNameTextColor` - customize command name font asset
- `streamUiCommandsNameFont` - customize command name text font
- `streamUiCommandsNameStyle` - customize command name text style
- `streamUiCommandsDescriptionTextSize` - customize command description text size
- `streamUiCommandsDescriptionTextColor` - customize command description text color
- `streamUiCommandsDescriptionFontAssets` - customize command description text color
- `streamUiCommandsDescriptionTextColor` - customize command description font asset
- `streamUiCommandsDescriptionFont` - customize command description text font
- `streamUiCommandsDescriptionStyle` - customize command description text style
- `streamUiSuggestionBackgroundColor` - customize suggestion view background



### ⚠️ Changed
- Deprecated `ChatUI` class

### ❌ Removed
