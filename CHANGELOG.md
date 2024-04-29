# UNRELEASED CHANGELOG
## Common changes for all artifacts
### 🐞 Fixed
- Shadowed messages are filtered. [#5234](https://github.com/GetStream/stream-chat-android/pull/5234)

### ⬆️ Improved
- `Channel.lastMessageAt` is not updated when there is a new message within a thread. [#5245](https://github.com/GetStream/stream-chat-android/pull/5245)

### ✅ Added
- Added `reason` and `custom` fields to flag message endpoint.[#5242](https://github.com/GetStream/stream-chat-android/pull/5242)
- Added `reason` and `custom` fields to flag user endpoint.[#5242](https://github.com/GetStream/stream-chat-android/pull/5242)
- Added `reactionGroups` field to `Message` entity. [#5247](https://github.com/GetStream/stream-chat-android/pull/5247)

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

## stream-chat-android-state
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

### ⬆️ Improved
- Channel List is not updated with new messages within a thread. [#5245](https://github.com/GetStream/stream-chat-android/pull/5245)

### ✅ Added
- Added a Button to jump to the first unread message in the channel. [#5236](https://github.com/GetStream/stream-chat-android/pull/5236)

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-compose
### 🐞 Fixed
- Fixed `ChannelsState.isLoadingMore` being stuck. [#5239](https://github.com/GetStream/stream-chat-android/pull/5239)

### ⬆️ Improved
- Channel List is not updated with new messages within a thread. [#5245](https://github.com/GetStream/stream-chat-android/pull/5245)

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-markdown-transformer
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

# April 19th, 2024 - 6.3.1
## stream-chat-android-compose
### 🐞 Fixed
- Fix issue with the keyboard gets stuck when typing a message. [#5235](https://github.com/GetStream/stream-chat-android/pull/5235)

# April 05th, 2024 - 6.3.0
## Common changes for all artifacts
### 🐞 Fixed
- Shadowed messages are not increasing the unread count. [#5229](https://github.com/GetStream/stream-chat-android/pull/5229)

## stream-chat-android-client
### ✅ Added
- Added `ChatClient.getChannel` to fetch a channel with no side effects. [#5227](https://github.com/GetStream/stream-chat-android/pull/5227)
  * Added `ChannelClient.get` to fetch a channel with no side effects.
- Added `UploadedFile.extraData` map field to include custom data to updated file attachments. [#5230](https://github.com/GetStream/stream-chat-android/pull/5230)

### ❌ Removed
- Removed `UploadedImage` class, replace with `UploadedFile` class. [#5230](https://github.com/GetStream/stream-chat-android/pull/5230)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed crash when providing a custom message view holder. [#5232](https://github.com/GetStream/stream-chat-android/pull/5232)

### ⚠️ Changed
- 🚨Breaking change: Exposed `MessageListItemViewHolderFactory.getItemViewType` which returns the view type for the given view holder. [#5232](https://github.com/GetStream/stream-chat-android/pull/5232)
  * You have to implement this method in your custom `MessageListItemViewHolderFactory` implementation along with another `getItemViewType` method, which returns the view type for the given message item.

# March 26th, 2024 - 6.2.3
## stream-chat-android-compose
### 🐞 Fixed
- Fixed `TextStyle` mismatch in `MessageTextFormmater` and `QuotedMessageTextFormatter`. [#5221](https://github.com/GetStream/stream-chat-android/pull/5221)

# March 22th, 2024 - 6.2.2
## stream-chat-android-core
### ✅ Added
- Added `Member.notificationsMuted` property support. [#5217](https://github.com/GetStream/stream-chat-android/pull/5217)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the crash happening while editing a message with a recording attachment. [#5220](https://github.com/GetStream/stream-chat-android/pull/5220)
- Fixed intermittent crash when opening a channel. [#5219](https://github.com/GetStream/stream-chat-android/pull/5219)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed `MessageListViewModel.selectExtendedReactions` not calling the proper function in `MessageListController`. [#5218](https://github.com/GetStream/stream-chat-android/pull/5218)

### ✅ Added
- Added `MessageTextFormatter` to format the message text. [#5214](https://github.com/GetStream/stream-chat-android/pull/5214)
  * Can be overridden by `ChatTheme.messageTextFormatter`
- Added `QuotedMessageTextFormatter` to format the quoted message text. [#5214](https://github.com/GetStream/stream-chat-android/pull/5214)
  * Can be overridden by `ChatTheme.quotedMessageTextFormatter` 
- Added `itemModifier` parameter into both `MessageList` and `Messages` components to allow customizing the message item. [#5209](https://github.com/GetStream/stream-chat-android/pull/5209)

# March 20th, 2024 - 6.2.1
## stream-chat-android-ui-components
### ⬆️ Improved
- Added `flag-message` capability support. [#5211](https://github.com/GetStream/stream-chat-android/pull/5211)

## stream-chat-android-compose
### ✅ Added
- Added `flag-message` capability support. [#5211](https://github.com/GetStream/stream-chat-android/pull/5211)

# March 15th, 2024 - 6.2.0
## Common changes for all artifacts
### ✅ Added
- `Message` entity has a new `messageTextUpdatedAt` property to represent the last time the message text was updated. [#5200](https://github.com/GetStream/stream-chat-android/pull/5200)
- `FileUploadConfig` entity has a new `sizeLimitInBytes:` property to represent the size limit for attachments. [#5205](https://github.com/GetStream/stream-chat-android/pull/5205)

## stream-chat-android-ui-common
### ⚠️ Changed
- Attachment size limit is not configured client-side anymore, you should configure it on the dashboard. [#5205](https://github.com/GetStream/stream-chat-android/pull/5205)

### ❌ Removed
- Removed `MessagesViewModelFactory.maxAttachmentSize` property. [#5205](https://github.com/GetStream/stream-chat-android/pull/5205)

## stream-chat-android-ui-components
### ✅ Added
- Edited message will show info about when they were edited. [#5200](https://github.com/GetStream/stream-chat-android/pull/5200)

### ❌ Removed
- Removed `MessagesViewModelFactory.maxAttachmentSize` property. [#5205](https://github.com/GetStream/stream-chat-android/pull/5205)

## stream-chat-android-compose
### ✅ Added
- Edited message will show info about when they were edited. [#5200](https://github.com/GetStream/stream-chat-android/pull/5200)
- Added `SearchMode` to `ChannelsScreen` to allow searching for channels or messages. [#5203](https://github.com/GetStream/stream-chat-android/pull/5203)
- Added `ChannelsList.searchResultContent` comoposable lambda to render the search result content. [#5203](https://github.com/GetStream/stream-chat-android/pull/5203)

### ❌ Removed
- Removed `MessagesViewModelFactory.maxAttachmentSize` property. [#5205](https://github.com/GetStream/stream-chat-android/pull/5205)

# March 05th, 2024 - 6.1.1
## stream-chat-android-ui-common
### ✅ Added
- Restored user mention customization which was removed during v5-v6 migration. [#5193](https://github.com/GetStream/stream-chat-android/pull/5193)  

# March 01th, 2024 - 6.1.0
## Common changes for all artifacts
### ⬆️ Improved
- Migrated the internal PhotoView library into the [photoview-android](https://github.com/GetStream/photoview-android), and now the `stream-chat-android-ui-components` doesn't need to depend on Jitpack.

### ⚠️ Changed
- Bump Compose UI to 1.6.2 and Compose compiler to 1.5.10.

## stream-chat-android-core
### ✅ Added
- Added `LinkPreview` model to represent the link preview data. [#5184](https://github.com/GetStream/stream-chat-android/pull/5184)

## stream-chat-android-client
### 🐞 Fixed
- Fixed `ArrayIndexOutOfBoundsException` in `ApiRequestsDumper`. [#5187](https://github.com/GetStream/stream-chat-android/pull/5187)

### ⬆️ Improved
- Ensure fresh token is used to establish WS connection. [#5185](https://github.com/GetStream/stream-chat-android/pull/5185)

### ✅ Added
- Added `ChatClient.enrichUrl` to enrich the URL with the preview data. [#5184](https://github.com/GetStream/stream-chat-android/pull/5184)

## stream-chat-android-ui-common
### ⚠️ Changed
- Deprecated `AttachmentSelectionListener` class, use `AttachmentsSelectionListener` instead. [#5178](https://github.com/GetStream/stream-chat-android/pull/5178)

## stream-chat-android-ui-components
### ✅ Added
- Added `MessageComposerView.attachmentsPickerDialogBuilder` lambda that allow you to create your own Picker Dialog. [#5178](https://github.com/GetStream/stream-chat-android/pull/5178)

## stream-chat-android-compose
### ✅ Added
- Added `MessageComposerTheme` to customize the message composer. [#5183](https://github.com/GetStream/stream-chat-android/pull/5183)
- Added `ComposerLinkPreview` to show link previews in the message composer. [#5184](https://github.com/GetStream/stream-chat-android/pull/5184)

# February 13th, 2024 - 6.0.14
## stream-chat-android-client
### 🐞 Fixed
- Prevent inserting reaction which violates `ForeignKey` constraint in local DB. [#5164](https://github.com/GetStream/stream-chat-android/pull/5164)
- Fixed crash in `ChatEventsObservable.onNext`. [#5165](https://github.com/GetStream/stream-chat-android/pull/5165)

### ✅ Added
- Added `DeleteChannelListener`. [#5164](https://github.com/GetStream/stream-chat-android/pull/5164)

## stream-chat-android-offline
### 🐞 Fixed
- Prevent sending reaction for non-existing message. [#5164](https://github.com/GetStream/stream-chat-android/pull/5164)
  * `SendReactionListener.onSendReactionPrecondition` is now suspendable function.
- Remove the deleted `Message` from cached `Channel.messages` collection. [#5170](https://github.com/GetStream/stream-chat-android/pull/5170)

## stream-chat-android-state
### 🐞 Fixed
- Clear stale cache inside `StateRegistry`. [#5164](https://github.com/GetStream/stream-chat-android/pull/5164)
- Unread messages count is now updated properly. [#5175](https://github.com/GetStream/stream-chat-android/pull/5175)

## stream-chat-android-ui-common
### ⚠️ Changed
- 🚨Breaking change: Changed `MessagePositionHandler.handleMessagePosition` signature. [#5168](https://github.com/GetStream/stream-chat-android/pull/5168)
  * Added `isInThread: Boolean` parameter.

## stream-chat-android-ui-components
### ✅ Added
- Added new listeners and corresponding setters to `MessageListView` to allow better behaviour customization. [#5161](https://github.com/GetStream/stream-chat-android/pull/5161)
  * `OnEnterThreadListener` and `MessageListView.setOnEnterThreadListener`
  * `OnMessageClickListener` and `MessageListView.setOnMessageClickListener`
  * `OnReplyMessageClickListener` and `MessageListView.setOnReplyMessageClickListener`
  * `OnMessageRetryListener` and `MessageListView.setOnMessageRetryListener`
  * `OnMessageLongClickListener` and `MessageListView.setOnMessageLongClickListener`
  * `OnModeratedMessageLongClickListener` and `MessageListView.setOnModeratedMessageLongClickListener`
  * `OnThreadClickListener` and `MessageListView.setOnThreadClickListener`
  * `OnAttachmentClickListener` and `MessageListView.setOnAttachmentClickListener`
  * `OnAttachmentDownloadClickListener` and `MessageListView.setOnAttachmentDownloadClickListener`
  * `OnGiphySendListener` and `MessageListView.setOnGiphySendListener`
  * `OnLinkClickListener` and `MessageListView.setOnLinkClickListener`
  * `OnUserClickListener` and `MessageListView.setOnUserClickListener`
  * `OnReactionViewClickListener` and `MessageListView.setOnReactionViewClickListener`
  * `OnUserReactionClickListener` and `MessageListView.setOnUserReactionClickListener`
- Added `messageBuilder` parameter to `MessageComposerViewModel.bindView` to allow customizing the message builder. [#5169](https://github.com/GetStream/stream-chat-android/pull/5169)

## stream-chat-android-compose
### 🐞 Fixed
- Fix annotated messages not being building proper links. [#5163](https://github.com/GetStream/stream-chat-android/pull/5163)

# January 24th, 2024 - 6.0.13
## stream-chat-android-client
### 🐞 Fixed
- Ensure PushNotification Permissions are only requested on the case `NotificationConfig.requestPermissionOnAppLaunch` lambda returns `true`.[#5158](https://github.com/GetStream/stream-chat-android/pull/5158)

### ⚠️ Changed
- Exposed `Decorator` and related classes for the better `MessageListView` customization. [#5144](https://github.com/GetStream/stream-chat-android/pull/5144) 

## stream-chat-android-state
### ✅ Added
- Open `ChatClient.getMessageUsingCache()` extension method. [#5153](https://github.com/GetStream/stream-chat-android/pull/5153

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed unread count not being cleared while user is inside a channel. [#5146](https://github.com/GetStream/stream-chat-android/pull/5146)

### ✅ Added
- Added `StartOfTheChannelItemState`, a new `MessageListItemState` that represent the start of the channel inside a message list. [#5145](https://github.com/GetStream/stream-chat-android/pull/5145)

### ⚠️ Changed
- Changed `AttachmentConstants.MAX_ATTACHMENTS_COUNT` to align with the API limits. [#5159](https://github.com/GetStream/stream-chat-android/pull/5159)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed blinking of the message list. [#5150](https://github.com/GetStream/stream-chat-android/pull/5150)

### ✅ Added
- Added `StartOfTheChannelItem`, a new `MessageListItem` that represent the start of the channel inside a message list. [#5145](https://github.com/GetStream/stream-chat-android/pull/5145)

## stream-chat-android-compose
### ✅ Added
- Added `startOfTheChannelItemState` composable function to `MessageContainer` to be able to render the start of the channel. [#5145](https://github.com/GetStream/stream-chat-android/pull/5145)

# January 09th, 2024 - 6.0.12
## stream-chat-android-core
### ❌ Removed
- Removed field name notation transformation from `QuerySortByField`. Now `QuerySortByField` does not transform field name from snake case to camel case. [#5140](https://github.com/GetStream/stream-chat-android/pull/5140)

## stream-chat-android-client
### 🐞 Fixed
- Fixed crash when playing completed voice message. [#5135](https://github.com/GetStream/stream-chat-android/pull/5135)
- Fixed send attachment without the offline plugin. [#5142](https://github.com/GetStream/stream-chat-android/pull/5142)

## stream-chat-android-ui-components
### ✅ Added
- Added `UserAvatarRenderer` and `ChannelAvatarRenderer` to customize avatars. [#5141](https://github.com/GetStream/stream-chat-android/pull/5141)

# December 22th, 2023 - 6.0.11
## Common changes for all artifacts
### ⬆️ Improved
- Improved unread count behavior, it relies on `read` property. [#5117](https://github.com/GetStream/stream-chat-android/pull/5117)

### ✅ Added
- Added `Channel.currentUserUnreadCount` extension property to get the unread count for the current user. [#5117](https://github.com/GetStream/stream-chat-android/pull/5117)

### ⚠️ Changed
- Deprecated `Channel.unreadCount` property, use `Channel.currentUserUnreadCount` instead. [#5117](https://github.com/GetStream/stream-chat-android/pull/5117)

## stream-chat-android-client
### 🐞 Fixed
- Fixed crash in `StreamMediaPlayer` when playing audio after re-login. [#5120](https://github.com/GetStream/stream-chat-android/pull/5120)

### ⬆️ Improved
- Protected PN related requests from repeated usage. [#5130](https://github.com/GetStream/stream-chat-android/pull/5130)
  * Prevented `ChatClient.getDevices` from duplicate requests.
  * Prevented `ChatClient.addDevice` from duplicate requests.
  * Prevented `ChatClient.deleteDevice` from duplicate requests.
  * Added debouncing logic into `PushTokenUpdateHandler`

### ✅ Added
- Create new feature to mark a channel as unread. [#5103](https://github.com/GetStream/stream-chat-android/pull/5103)
- Added a new `NotificationMarkUnreadEvent` event type. [#5103](https://github.com/GetStream/stream-chat-android/pull/5103)

## stream-chat-android-state
### ⬆️ Improved
- `SyncManager` handles "Too many events to sync" error properly. [#5126](https://github.com/GetStream/stream-chat-android/pull/5126) 

## stream-chat-android-ui-components
### ✅ Added
- Added a new menu option to mark a channel as unread. [#5103](https://github.com/GetStream/stream-chat-android/pull/5103)
- Added a new Unread Separator component. [#5122](https://github.com/GetStream/stream-chat-android/pull/5122)
- Added `AudioRecordPlayerViewStyle` to customize the audio record player view. [#5119](https://github.com/GetStream/stream-chat-android/pull/5119)
- Added support for automatic translations [#5123](https://github.com/GetStream/stream-chat-android/pull/5123)
  * Enabled by `ChatUI.autoTranslationEnabled`
- Added `MessageListItemStyle.textStyleReadCounter` to customize the read counter text style. [#5131](https://github.com/GetStream/stream-chat-android/pull/5131)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed unread count not being cleared. [#5115](https://github.com/GetStream/stream-chat-android/pull/5115)

### ✅ Added
- Added a new menu option to mark a channel as unread. [#5129](https://github.com/GetStream/stream-chat-android/pull/5129)
- Added a new Unread Separator component. [#5122](https://github.com/GetStream/stream-chat-android/pull/5122)

# December 11th, 2023 - 6.0.10
## stream-chat-android-core
### ⚠️ Changed
- 🚨Breaking change: The following properties in `User` class are now **nullable**: `banned`, `invisible`. [#5107](https://github.com/GetStream/stream-chat-android/pull/5107)
  * Please use `User.isBanned` as non-nullable version.
  * Please use `User.isInvisible` as non-nullable version.
  * Properties such as `invisible`, `banned`, `teams` and `role` are not being used to establish WS connection flow if not specified.

## stream-chat-android-state
### 🐞 Fixed
- Fix wrong Message.ownReactions. [#5106](https://github.com/GetStream/stream-chat-android/pull/5106)

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed broken date formatting. [#5101](https://github.com/GetStream/stream-chat-android/pull/5101)
- Fixed thread separator ui order. [#5098](https://github.com/GetStream/stream-chat-android/pull/5098)
  * `MessageListController.showThreadSeparatorInEmptyThread` was added to control the visibility of the thread separator in empty threads.
- Fixed `MessageList` scrolling behaviour while receiving a new message. [#5112](https://github.com/GetStream/stream-chat-android/pull/5112)
  * `NewMessageState.MyOwn` and `NewMessageState.Other` are now data classes.
- Fixed old messages being marked as read by freshly added members. [#5132](https://github.com/GetStream/stream-chat-android/pull/5132)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed `MessageClickListener` never being called. [#5096](https://github.com/GetStream/stream-chat-android/pull/5096)
- Fixed thread separator ui order. [#5098](https://github.com/GetStream/stream-chat-android/pull/5098)
  * `MessageListViewModelFactory.showThreadSeparatorInEmptyThread` was added to control the visibility of the thread separator in empty threads.
- Fixed: Regex was not correctly escaped in getOccurrenceRanges [#5109](https://github.com/GetStream/stream-chat-android/pull/5109)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed thread separator ui order. [#5098](https://github.com/GetStream/stream-chat-android/pull/5098)
  * `MessagesViewModelFactory.showThreadSeparatorInEmptyThread` was added to control the visibility of the thread separator in empty threads.

### ⬆️ Improved
- Removed attachment picker customization limitation for `AttachmentsPickerTabFactory` non-file implementations. [#5104](https://github.com/GetStream/stream-chat-android/pull/5104)

# November 24th, 2023 - 6.0.9
## stream-chat-android-client
### 🐞 Fixed
- Fixed audio recording not being uploaded [#5066](https://github.com/GetStream/stream-chat-android/pull/5066)
- All sent messages are initialized with a non-null `createdLocallyAt` property. [#5086](https://github.com/GetStream/stream-chat-android/pull/5086)
- Fix GZIP compression not working [5068](https://github.com/GetStream/stream-chat-android/pull/5068)

### ⬆️ Improved
- Performance fixes:
  - Faster ISO Date parser (5070)[https://github.com/GetStream/stream-chat-android/pull/5070]
  - Preload KClass classes for parsing [5074](https://github.com/GetStream/stream-chat-android/pull/5074)
  - Faster asynchronous `ChannelMutableState` creation [5076](https://github.com/GetStream/stream-chat-android/pull/5076)
  - Delay reflection in `NotificationHandlerFactory` (5078)[https://github.com/GetStream/stream-chat-android/pull/5078]
  - Faster `SocketListener` callback delivery (5082)[https://github.com/GetStream/stream-chat-android/pull/5082]
  - Use `DerivedStateFlow` instead of `stateIn` (5083[https://github.com/GetStream/stream-chat-android/pull/5083]

### ✅ Added
- Added `SocketListener.deliverOnMainthread`. This allows you to disabled the default delivery on Main thread in case you are already handling it asynchronously in your code. `SocketListener` with `deliverOnMainthread` set to `false` will deliver the events a bit faster because there is no overhead of thread switching.

## stream-chat-android-offline
### ✅ Added
- Support for `skip_slow_mode` in the `ChannelCapabilities`. This allows the server to completely disable slow mode in messaging for specific users.

## stream-chat-android-state
### 🐞 Fixed
- Fix unread count, muted channel list and banned user list being incorrect in some cases [5084](https://github.com/GetStream/stream-chat-android/pull/5084)

### ⬆️ Improved
- Fix issue on the pagination process when querying a channel by filling the messages list gap. [#5086](https://github.com/GetStream/stream-chat-android/pull/5086)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fix channel title position when typing. [#5073](https://github.com/GetStream/stream-chat-android/pull/5073)

### ✅ Added
- Added `MessageComposerViewModel.bindViewDefaults` which preserves the default view bindings. [#5060](https://github.com/GetStream/stream-chat-android/pull/5060)
- Added UI customizations for message composer. [#5064](https://github.com/GetStream/stream-chat-android/pull/5064)
  * `MessageComposerViewStyle.commandSuggestionsTitleIconDrawableTintColor`
  * `MessageComposerViewStyle.mentionSuggestionItemIconDrawableTintColor`
  * `MessageComposerViewStyle.attachmentsButtonIconTintList`
  * `MessageComposerViewStyle.commandsButtonIconTintList`
  * `MessageComposerViewStyle.sendMessageButtonIconTintList`
  * `MessageComposerViewStyle.audioRecordingButtonIconTintList`

### ⚠️ Changed
- Made `MessageReplyView` publicly available. [#5058](https://github.com/GetStream/stream-chat-android/pull/5058)
- Deprecated `MessageListItemStyle.textStyleMessageDeleted`. Use `MessageListItemStyle.textStyleMessageDeletedMine` and `MessageListItemStyle.textStyleMessageDeletedTheirs` instead.  [#5050](https://github.com/GetStream/stream-chat-android/pull/5050)
- Deprecated `MessageListItemStyle.messageDeletedBackground`. Use `MessageListItemStyle.messageDeletedBackgroundMine` and `MessageListItemStyle.messageDeletedBackgroundTheirs` instead.  [#5050](https://github.com/GetStream/stream-chat-android/pull/5050)
- Deprecated `MessageListItemStyle.buttonIconDrawableTintColor`. Use one of the params listed below instead. [#5064](https://github.com/GetStream/stream-chat-android/pull/5064)
  * `MessageComposerViewStyle.commandSuggestionsTitleIconDrawableTintColor`
  * `MessageComposerViewStyle.mentionSuggestionItemIconDrawableTintColor`
  * `MessageComposerViewStyle.attachmentsButtonIconTintList`
  * `MessageComposerViewStyle.commandsButtonIconTintList`
  * `MessageComposerViewStyle.sendMessageButtonIconTintList`
  * `MessageComposerViewStyle.audioRecordingButtonIconTintList`
- Made `MessageComposerContent` descendants extensible/reusable. [#5065](https://github.com/GetStream/stream-chat-android/pull/5065)

# November 09th, 2023 - 6.0.8
## Common changes for all artifacts
### ✅ Added
- Added `Message.moderationDetails` due to support of the new moderation API. [#5035](https://github.com/GetStream/stream-chat-android/pull/5035)
- Added `MessageModerationDetails` class.

### ❌ Removed
- Removed `Message.syncDescription` due to removal of the old moderation API. [#5035](https://github.com/GetStream/stream-chat-android/pull/5035)
- Removed `MessageSyncContent` class and its subclasses.
- Removed `MessageSyncType` class.

## stream-chat-android-client
### 🐞 Fixed
- Fixed duplicate send message requests. [5039](https://github.com/GetStream/stream-chat-android/pull/5039)

### ⬆️ Improved
- Pass `message` with `result` in `SendMessageDebugger`. [#5037](https://github.com/GetStream/stream-chat-android/pull/5037)
- Use shortService instead of dataSync for our workmanager job. [#5041](https://github.com/GetStream/stream-chat-android/pull/5041)

## stream-chat-android-ui-common
### ✅ Added
- Added `callback` param to `MessageComposerController.sendMessage` method. [#5038](https://github.com/GetStream/stream-chat-android/pull/5038)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed channel title not being centered vertically when mo last message exists. [#5043](https://github.com/GetStream/stream-chat-android/pull/5043)

### ✅ Added
- Added `callback` param to `MessageComposerViewModel.sendMessage` method. [#5038](https://github.com/GetStream/stream-chat-android/pull/5038)
- Added UI customizations for deleted message. [#5050](https://github.com/GetStream/stream-chat-android/pull/5050)
  * `MessageListItemStyle.textStyleMessageDeletedMine`
  * `MessageListItemStyle.messageDeletedBackgroundMine`
  * `MessageListItemStyle.textStyleMessageDeletedTheirs`
  * `MessageListItemStyle.messageDeletedBackgroundTheirs`

### ⚠️ Changed
- Supported new moderation API. [#5035](https://github.com/GetStream/stream-chat-android/pull/5035)

## stream-chat-android-compose
### ✅ Added
- Added `callback` param to `MessageComposerViewModel.sendMessage` method. [#5038](https://github.com/GetStream/stream-chat-android/pull/5038)

### ⚠️ Changed
- Supported new moderation API. [#5035](https://github.com/GetStream/stream-chat-android/pull/5035)

# October 31th, 2023 - 6.0.6
## stream-chat-android-client
### ✅ Added
- Display translated text in push notifications. [#5028](https://github.com/GetStream/stream-chat-android/pull/5028)

### 🐞 Fixed
- Encode filename before uploading. [#5026](https://github.com/GetStream/stream-chat-android/pull/5026)

### ⚠️ Changed
- Make `ChatClient.addDevice` and `ChatClient.deleteDevice` public. [#5024](https://github.com/GetStream/stream-chat-android/pull/5024)

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed channel update emissions in `MessageListController`. [#5029](https://github.com/GetStream/stream-chat-android/pull/5029)

## stream-chat-android-compose
### ✅ Added
- Added `MessageDateSeparatorTheme` to customize the date separator component. [#5013](https://github.com/GetStream/stream-chat-android/pull/5013)
- Added support for automatic translations [#5028](https://github.com/GetStream/stream-chat-android/pull/5028)
  * Enabled by `ChatTheme.autoTranslationEnabled`

# October 23th, 2023 - 6.0.5
## Common changes for all artifacts
### ✅ Added
- Added `User.language` property. [#5003](https://github.com/GetStream/stream-chat-android/pull/5003)

## stream-chat-android-client
### 🐞 Fixed
- Fix background service used to sync data when a Push Notification is received on Android 14. [#4997](https://github.com/GetStream/stream-chat-android/pull/4997)
- Fix `Message.addOwnReaction()` process. [#5000](https://github.com/GetStream/stream-chat-android/pull/5000)

### ✅ Added
- Supported user's `language` property in `ChatClient.connectUser`. [#5003](https://github.com/GetStream/stream-chat-android/pull/5003)

### ⚠️ Changed
- Disconnect user on `UnrecoverableError`. [#5000](https://github.com/GetStream/stream-chat-android/pull/5004)

## stream-chat-android-state
### ⬆️ Improved
- Improved `SyncManger`, which now does not retry outdated messages/reactions [#4991](https://github.com/GetStream/stream-chat-android/pull/4991)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the message list gap. [#4998](https://github.com/GetStream/stream-chat-android/pull/4998)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the message list gap. [#4998](https://github.com/GetStream/stream-chat-android/pull/4998)

# October 03th, 2023 - 6.0.4
## stream-chat-android-client
### ⬆️ Improved
- Create Throttling mechanism for `MarkRead` Events [#4975](https://github.com/GetStream/stream-chat-android/pull/4975)
- Ignore push messages if WS is connected. [#4979](https://github.com/GetStream/stream-chat-android/pull/4979)
- Added caching mechanism to date parsing. [#4981](https://github.com/GetStream/stream-chat-android/pull/4981)

## stream-chat-android-offline
### ⬆️ Improved
- Improved database caching, which resulted in reduced IO operations. [#4983](https://github.com/GetStream/stream-chat-android/pull/4983) 

## stream-chat-android-state
### ⬆️ Improved
- Expanded/Enhanced event batching to speed up the event processing. [#4982](https://github.com/GetStream/stream-chat-android/pull/4982)

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed thread loading. [#4971](https://github.com/GetStream/stream-chat-android/pull/4971)

# September 18th, 2023 - 6.0.3
## stream-chat-android-client
### 🐞 Fixed
- Fixed Push Notifications for not working with R8 enabled. [#4961](https://github.com/GetStream/stream-chat-android/pull/4961)

### ✅ Added
- Added `ChatClientDebugger.onNonFatalErrorOccurred` to handle non-fatal errors. [#4959](https://github.com/GetStream/stream-chat-android/pull/4959)
- Added `Message.deletedReplyCount` property. [#4950](https://github.com/GetStream/stream-chat-android/pull/4950)

## stream-chat-android-state
### 🐞 Fixed
- Fixed `ChatClient.watchChannelAsState` to be called without connecting a user. [#4962](https://github.com/GetStream/stream-chat-android/pull/4962)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed user avatar click on channel list view. [#4957](https://github.com/GetStream/stream-chat-android/pull/4957)

# September 11th, 2023 - 6.0.2
## stream-chat-android-client
### 🐞 Fixed
- Fixed a crash when replaying the same voice message after scrolling out and in. [4948](https://github.com/GetStream/stream-chat-android/pull/4948)

### ⬆️ Improved
- `ChatClient.markRead` returns ongoing `Call` instance if exists to avoid firing duplicate requests. [#4949](https://github.com/GetStream/stream-chat-android/pull/4949)

## stream-chat-android-ui-common
### 🐞 Fixed
- `MessageListController.markLastMessageRead` does debounce its' calls and checks last seen messageId to avoid duplicate `read` requests. [#4949](https://github.com/GetStream/stream-chat-android/pull/4949)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed UI not being updated after replaying the same voice message after scrolling out and in. [4948](https://github.com/GetStream/stream-chat-android/pull/4948)
- Fixed crash when trying to play a failed voice message. [4951](https://github.com/GetStream/stream-chat-android/pull/4951)

### ⬆️ Improved
- `MessageListScrollHelper.isAtBottom` now triggers `callback.onLastMessageRead()` when it changes from `false` to `true` only. [#4949](https://github.com/GetStream/stream-chat-android/pull/4949)

# September 05th, 2023 - 6.0.1
## stream-chat-android-client
### 🐞 Fixed
- Fixed NPE in `StreamWebSocket.onFailure`. [#4942](https://github.com/GetStream/stream-chat-android/pull/4942)

### ⬆️ Improved
- Extended `PushMessage` to access extra data [#4945](https://github.com/GetStream/stream-chat-android/pull/4945)

## stream-chat-android-ui-components
### ⬆️ Improved
- Extended channel list screen styling. [#4944](https://github.com/GetStream/stream-chat-android/pull/4944)

# August 29th, 2023 - 6.0.0

🚨🚨 **v6.0.0** release brings a lot of different enhancements. Please, make sure to check our [migration guides](https://getstream.io/chat/docs/sdk/android/migration-guides/client/push-notifications/)! 🚨🚨

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

# August 25th, 2023 - 6.0.0-beta5

I hope you're interested in our latest major release - v6.0.0-beta5! We're currently in the process of preparing migration guides and updating all of our
documentation, but in the meantime, you can look into what we plan to release in v6 of Android Chat.

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed `LinkAttachmentView` to open the correct link when the attachment is clicked. [#4930](https://github.com/GetStream/stream-chat-android/pull/4930)

### ✅ Added
- Added `MessageComposerViewModelBinder` for Java parity with Kotlin's `MessageComposerViewModel.bindView` extension function. [#4931](https://github.com/GetStream/stream-chat-android/pull/4931)

### ⚠️ Changed
- Changed `MessageListView.setCustomItemAnimator` signature to allow passing `null` to reset the item animator. [#4933](https://github.com/GetStream/stream-chat-android/pull/4933)

# August 18th, 2023 - 6.0.0-beta4

I hope you're interested in our latest major release - v6.0.0-beta4! We're currently in the process of preparing migration guides and updating all of our
documentation, but in the meantime, you can look into what we plan to release in v6 of Android Chat.

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

## Common changes for all artifacts
### ⚠️ Changed
- Business models are immutable now. [#4893](https://github.com/GetStream/stream-chat-android/pull/4893)

## stream-chat-android-client
### ✅ Added
- Added `R.color.stream_ic_notification` to the list of resources that can be overridden. [#4921](https://github.com/GetStream/stream-chat-android/pull/4921)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed send button being hidden with non-empty input when `audioRecordingButtonPreferred` is enabled. [#4925](https://github.com/GetStream/stream-chat-android/pull/4925)

### ⬆️ Improved
- Improved UI customization for Audio Recording feature. [#4906](https://github.com/GetStream/stream-chat-android/pull/4906)
- Improved trailing buttons customization for `MessageComposerView`. [#4915](https://github.com/GetStream/stream-chat-android/pull/4915)

### ✅ Added
- Added `MessageListView.setCustomItemAnimator` to allow customizing the item animator used by the `MessageListView`. [#4922](https://github.com/GetStream/stream-chat-android/pull/4922)
- Added `ChannelListView.setMoreOptionsIconProvider` and `ChannelListView.setDeleteOptionIconProvider` to allow customizing the options icons used by the `ChannelListView`. [#4927](https://github.com/GetStream/stream-chat-android/pull/4927)

# July 27th, 2023 - 6.0.0-beta3

I hope you're interested in our latest major release - v6.0.0-beta3! We're currently in the process of preparing migration guides and updating all of our
documentation, but in the meantime, you can look into what we plan to release in v6 of Android Chat.

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

## Common changes for all artifacts
### ⬆️ Improved
- Added baseline profiles to compose and clients modules for improving performance [#4610](https://github.com/GetStream/stream-chat-android/pull/4610)

### ✅ Added
- Count for read messages. [#4678](https://github.com/GetStream/stream-chat-android/pull/4678)

## stream-chat-android-client
### 🐞 Fixed
- Fixing unread messages count for channels. [#4499](https://github.com/GetStream/stream-chat-android/pull/4499)
- Fix reconnection socket behavior. [#4821](https://github.com/GetStream/stream-chat-android/pull/4821)
- Fixed `QueryChannelRequest.withWatchers` to make `watchers` accessible in response. [#4848](https://github.com/GetStream/stream-chat-android/pull/4848)

### ⬆️ Improved
- Changes in messages that are quoted now reflect in all messages, not only the original message.[#4646](https://github.com/GetStream/stream-chat-android/pull/4646)

### ✅ Added
- Added the string extension function `createResizedStreamCdnImageUrl()` which resizes images hosted on Stream's CDN by adding the necessary query parameters to the URL. [#4600](https://github.com/GetStream/stream-chat-android/pull/4600)
- Added the string extension function `getStreamCdnHostedImageDimensions()` used to extract original image dimensions from URLs of images hosted on Stream's CDN which contain the original width and height parameters. Added a new class called `StreamCdnOriginalImageDimensions` which stores the original height and width data. [#4600](https://github.com/GetStream/stream-chat-android/pull/4600)
- Added classes `StreamCdnCropImageMode` and `StreamCdnResizeImageMode` used for modifying Stream CDN image resize requests. [#4600](https://github.com/GetStream/stream-chat-android/pull/4600)
- Added `ChatClient.clearPersistence()` to be able to clear local data even if the user is not connected. [#4796](https://github.com/GetStream/stream-chat-android/pull/4796)
- Added new `hideHistory` flag into `ChatClient.addMembers` function. This flag can be used to hide the channel's history from the added member. [#4817](https://github.com/GetStream/stream-chat-android/pull/4817)
- Added new `hideHistory` flag into `ChannelClient.addMembers` function. This flag can be used to hide the channel's history from the added member. [#4817](https://github.com/GetStream/stream-chat-android/pull/4817)
- Added `ChatClient.inviteMembers` to invite members to an existing channel. [#4816](https://github.com/GetStream/stream-chat-android/pull/4816)
- Added `ChannelClient.inviteMembers` to invite members to an existing channel. [#4816](https://github.com/GetStream/stream-chat-android/pull/4816)
- Added `ChannelUserBannedEvent.shadow` property to know if the user is shadow-banned or standard banned. [#4836](https://github.com/GetStream/stream-chat-android/pull/4836)
- Added `AttachmentsVerifier` to verify if the uploaded attachments are valid. [#4852](https://github.com/GetStream/stream-chat-android/pull/4852)
- Added `ChannelClient.fetchCurrentUser` to fetch current user from backend. [#4860](https://github.com/GetStream/stream-chat-android/pull/4860)
- Added `FetchCurrentUserListener` interface used to perform actions as side effects when the `ChatCliet.fetchCurrentUser()` method is used to fetch the current user from the backend. [#4860](https://github.com/GetStream/stream-chat-android/pull/4860)
- Added `AudioPlayer` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)

### ⚠️ Changed
- Changed `newMessageIntent` lambda's signature of `NotificationHandlerFactory.createNotificationHandler()`. It receives the whole `Message`/`Channel` entity to help you  create a more complex navigation intent.
- Moved `ChatClient.dismissChannelNotifications()` into `ChatClient` class. [#4692](https://github.com/GetStream/stream-chat-android/pull/4692)
- Changed `ConnectionState` from interface to Sealed Class. The new `ConnectionState.Connected` class contains the `user` that open the WebSocket connection. [#4808](https://github.com/GetStream/stream-chat-android/pull/4808)
- Renamed `InitializationState.RUNNING` to `InitializationState.INITIALIZING`. [#4827](https://github.com/GetStream/stream-chat-android/pull/4827)
- Changed return type of `ChatClient.disconnectSocket()` to `Call<Unit>`. [#4829](https://github.com/GetStream/stream-chat-android/pull/4829)
- Changed return type of `ChatClient.reconnectSocket()` to `Call<Unit>`. [#4829](https://github.com/GetStream/stream-chat-android/pull/4829)

### ❌ Removed
- Removed `ChatClient.setDevice()` method. [#4692](https://github.com/GetStream/stream-chat-android/pull/4692)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed pagination problems related for quoted messages [#4638](https://github.com/GetStream/stream-chat-android/issues/4638)
- Fixed message gaps in message list view when using offline support. [#4633](https://github.com/GetStream/stream-chat-android/pull/4633)

## stream-chat-android-state
### 🐞 Fixed
- Fixed `SyncManager` not emitting missed events after getting online. [#4862](https://github.com/GetStream/stream-chat-android/pull/4862)

### ⬆️ Improved
- The `ChannelStateLogic` keeps members updated after ban/unban events are received. [#4836](https://github.com/GetStream/stream-chat-android/pull/4836)

## stream-chat-android-ui-common
### ⬆️ Improved
- Send `TypingStopEvent` whenever the message is sent or the messageComposer contains an empty text message. [#4904](https://github.com/GetStream/stream-chat-android/pull/4904)

### ✅ Added
- Added `showDateSeparatorInEmptyThread: Boolean` to `MessageListController`. It is used to regulate whether date separators appear in empty threads. [#4742](https://github.com/GetStream/stream-chat-android/pull/4742)
- Added `StreamMediaRecorder` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added `AudioRecordingController` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed edit messages and reply messages with unsupported attachments. [#4757](https://github.com/GetStream/stream-chat-android/pull/4757)
- Fixed `ChannelViewHolder` to show proper last message value. [#4901](https://github.com/GetStream/stream-chat-android/pull/4901)
- Fixed `CnahnelViewHolder` to handle TypingIndicator visibility properly. [#4904](https://github.com/GetStream/stream-chat-android/pull/4904)

### ⬆️ Improved
- Emails are highlighted and clickable in the message text. [#4833](https://github.com/GetStream/stream-chat-android/pull/4833)
- `ChannelListPayloadDiff` is calculated using the list of members instead of the list of users. [#4840](https://github.com/GetStream/stream-chat-android/pull/4840)

### ✅ Added
- Added `ChatUI.streamCdnImageResizing` which allows resizing images where they appear as previews, such as the message list, attachment gallery overview or user and channel avatars. Only images hosted by Stream's CDN which contain original width and height query parameters can be resized. Image resizing is a paid feature and is disabled by default, you can enable it by overriding the aforementioned `ChatUI.streamCdnImageResizing` property with with an instance that has `StreamCdnImageResizing.imageResizingEnabled` set to true. Pricing can be found [here](https://getstream.io/chat/pricing/). [#4600](https://github.com/GetStream/stream-chat-android/pull/4600)
- Added `showDateSeparatorInEmptyThread: Boolean` to `MessageListViewModelFactory`. It is used to regulate whether date separators appear in empty threads. [#4742](https://github.com/GetStream/stream-chat-android/pull/4742)
- Added the ability to choose `PickerMediaMode` that allows control if the camera recorder and/or take picture feature is allowed or not in `MessageComposerView` via xml attributes. [#4812](https://github.com/GetStream/stream-chat-android/pull/4812)
  * `streamUiMessageComposerAttachmentsPickerMediaMode`
- Added Typing Users list to `ChannelItem`. [#4868](https://github.com/GetStream/stream-chat-android/pull/4868)
- Added typing indicator on `ChannelLitsView`. [#4868](https://github.com/GetStream/stream-chat-android/pull/4868)
- Added options visibility customization for each channel in a list. [#4870](https://github.com/GetStream/stream-chat-android/pull/4870)
- Added `AudioRecordAttachmentPreviewFactory` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added `DefaultMessageComposerOverlappingContent` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added new custom views (`AudioRecordPlayerView`, `AudioWavesSeekBar`, `WaveformView`) as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added `AudioRecordingAttachmentsGroupView` in addition to `MediaAttachmentsGroupView` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)

### ⚠️ Changed
- Replaced the method parameter `replyMessageId: String` with `replyTo: Message` inside `ReplyMessageClickListener.onReplyClick()`. The new parameter now contains the complete message to which the reply was made. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)
- Added the parameter `parentId: String?` to `AttachmentGalleryResultItem`. It is used to indicate when a message is belongs to a thread. Same has been added to the extension function `Attachment.toAttachmentGalleryResultItem()`. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)
- Added the parameter `parentMessageId: String?` to the class `MessageListViewModel.ShowMessage`. If the message you want to scroll to is a thread message, pass in its parent message ID, otherwise you can pass in `null`. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)
- 🚨 Breaking change: Removed `ChatUI.showThreadSeparatorInEmptyThread`. It has been replaced by `MessageListController.showDateSeparatorInEmptyThread`. If you are using our `ViewModel` factory, `MessageListViewModelFactory.showDateSeparatorInEmptyThread` will pass the parameter through to the `MessageListController` contained by `MessageListViewMode`. [#4742](https://github.com/GetStream/stream-chat-android/pull/4742)
- Create new `bind()` method on `BaseChannelListItemViewHolder` that takes as parameter `ChannelItem`. [#4868](https://github.com/GetStream/stream-chat-android/pull/4868)
- Added `MessageComposerView.setCenterOverlapContent` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- `FileAttachmentsView` supports new audio recording view type as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added audio recording style customization into `MessageComposerViewStyle` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
  * `audioRecordingButtonVisible`
  * `audioRecordingHoldToRecordText`
  * `audioRecordingHoldToRecordTextColor`
  * `audioRecordingHoldToRecordBackgroundDrawable`
  * `audioRecordingHoldToRecordBackgroundDrawableTint`
  * `audioRecordingSlideToCancelText`
  * `audioRecordingMicIconDrawable`
  * `audioRecordingMicIconDrawableTint`
  * `audioRecordingLockIconDrawable`
  * `audioRecordingLockIconDrawableTint`
  * `audioRecordingLockedIconDrawable`
  * `audioRecordingLockedIconDrawableTint`
- Added audio recording xml attrs for `MessageComposerView` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
  * `streamUiMessageComposerAudioRecordingButtonVisible`
  * `streamUiMessageComposerAudioRecordingHoldToRecordText`
  * `streamUiMessageComposerAudioRecordingHoldToRecordTextColor`
  * `streamUiMessageComposerAudioRecordingHoldToRecordBackgroundDrawable`
  * `streamUiMessageComposerAudioRecordingHoldToRecordBackgroundDrawableTint`
  * `streamUiMessageComposerAudioRecordingSlideToCancelText`
  * `streamUiMessageComposerAudioRecordingMicIconDrawable`
  * `streamUiMessageComposerAudioRecordingMicIconDrawableTint`
  * `streamUiMessageComposerAudioRecordingLockIconDrawable`
  * `streamUiMessageComposerAudioRecordingLockIconDrawableTint`
  * `streamUiMessageComposerAudioRecordingLockedIconDrawable`
  * `streamUiMessageComposerAudioRecordingLockedIconDrawableTint`

## stream-chat-android-compose
### ⬆️ Improved
- Updated Compose compiler version to `1.4.3`. [#4697](https://github.com/GetStream/stream-chat-android/pull/4697)
- Added `ChatClient.clearPersistence()` to be able to clear local data even if the user is not connected. [#4797](https://github.com/GetStream/stream-chat-android/pull/4797)
- Emails are highlighted and clickable in the message text. [#4833](https://github.com/GetStream/stream-chat-android/pull/4833)

### ✅ Added
- Added `onChannelAvatarClick` handler to `MessageListHeader`. [#4545](https://github.com/GetStream/stream-chat-android/pull/4545)
- Added `ChatTheme.streamCdnImageResizing` which allows resizing images where they appear as previews, such as the message list, attachment gallery overview or user and channel avatars. Only images hosted by Stream's CDN which contain original width and height query parameters can be resized. Image resizing is a paid feature and is disabled by default, you can enable it by overriding the aforementioned `ChatTheme.streamCdnImageResizing` property with an instance that has `StreamCdnImageResizing.imageResizingEnabled` set to true.. Pricing can be found [here](https://getstream.io/chat/pricing/). [#4600](https://github.com/GetStream/stream-chat-android/pull/4600)
- Added the composable content slot `emptyThreadPlaceholderItemContent` to `MessageContainer`. It is used to display placeholders inside empty threads if the feature was enabled by the `MessageListController` of the `MessageListViewModel` instance you have created.  [#4742](https://github.com/GetStream/stream-chat-android/pull/4742)
- Added `showDateSeparatorInEmptyThread: Boolean` to `MessagesViewModelFactory`. It is used to regulate whether date separators appear in empty threads. [#4742](https://github.com/GetStream/stream-chat-android/pull/4742)
- Add `ThreadMessagesStart` that allows to control if the stack of thread messages starts at the bottom or the top. [#4807](https://github.com/GetStream/stream-chat-android/pull/4807)
- Add `PickerMediaMode` that allows control if the camera recorder and/or take picture feature is allowed or not. [#4812](https://github.com/GetStream/stream-chat-android/pull/4812)
- Added `MessageTheme` to customize the message components into `ChatTheme`. [#4856](https://github.com/GetStream/stream-chat-android/pull/4856)
- Added `StatefulStreamMediaRecorder` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added `AudioRecordAttachmentFactory` as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)
- Added new components (`RunningWaveForm`, `AudioWaveVSeekbar`, `AudioRecordAttachmentContent`, `AudioRecordGroupContent`) as a part of `async-voice-messaging` feature. [#4828](https://github.com/GetStream/stream-chat-android/pull/4828)

### ⚠️ Changed
- 🚨 Breaking change: Renamed `onHeaderActionClick` to `onHeaderTitleClick` in `MessagesScreen`. Change made in order to better reflect the handler's behavior. [#4535](https://github.com/GetStream/stream-chat-android/pull/4535)
- 🚨 Breaking change: Renamed `onHeaderActionClick` to `onHeaderTitleClick` in `MessageListHeader`. Change made in order to better reflect the handler's behavior. [#4535](https://github.com/GetStream/stream-chat-android/pull/4535)
- Added `onChannelAvatarClick` handler to `MessageListHeader`. [#4545](https://github.com/GetStream/stream-chat-android/pull/4545)
- Added `parentMessageId` to `MediaGalleryPreviewResult`. It is used when we need to return a result upon which we navigate to a thread message. If the message we need to navigate to is not a thread message the `parentMessageId` value will be null. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)
- Added argument `parentMessageId: String?` to `MessageListViewModel.scrollToMessage()`. If the message you want to scroll to is a thread message, pass in its parent message ID, otherwise you can pass in `null`. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)
- Added parameter `parentMessageId: String?` to `MessageListViewModelFactory`. If you want to scroll to a thread message upon opening the messaging screen, pass in the thread message's parent message ID, otherwise you can pass in `null`. [#4639](https://github.com/GetStream/stream-chat-android/pull/4639)

### ❌ Removed
- Removed `ImagePreviewAction`, `ImagePreviewOption`, `ImagePreviewResult`, `ImagePreviewResultType`, `ImagePreviewViewModel` and `ImagePreviewViewModelFactory`. These were removed in favor of using the newer `MediaGalleryPreviewActivity` along with it's accompanying classes. The removed classes have their media gallery counterparts, for instance `ImagePreviewAction` becomes `MediaGalleryPreviewAction`, and so on. [#4766](https://github.com/GetStream/stream-chat-android/pull/4766)

# December 1st, 2022 - 6.0.0-beta2

I hope you're interested in our latest major release - v6.0.0-beta2! We're currently in the process of preparing migration guides and updating all of our
documentation, but in the meantime, you can look into what we plan to release in v6 of Android Chat.

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

## Common changes for all artifacts
### ✅ Added
- Added `Result::getOrNull`, `Result::getOrThrow` and `Result.chatErrorOrNull` to simplify getting `value` for Java users. [#4415](https://github.com/GetStream/stream-chat-android/pull/4415)

## stream-chat-android-client
### 🐞 Fixed
- Fixing erase of offline messages when entering a channel. [#4457](https://github.com/GetStream/stream-chat-android/pull/4457)

## stream-chat-android-state
### ❌ Removed
- Removed `StateAwarePlugin` interface.[4435](https://github.com/GetStream/stream-chat-android/pull/4435)

## stream-chat-android-ui-common
### ✅ Added
- Added constructor overloads for the `MessageMode.MessageThread` class to simplify usage for Java users. [#4427](https://github.com/GetStream/stream-chat-android/pull/4427)

## stream-chat-android-ui-components
### ✅ Added
- Added the default value for the `message` parameter passed to the `MessageComposerViewModel:sendMessage` function. [#4427](https://github.com/GetStream/stream-chat-android/pull/4427)

## stream-chat-android-pushprovider-firebase
### ❌ Removed
- Artifact removed, use `io.getstream:stream-android-push-firebase:VERSION` instead. [#4512](https://github.com/GetStream/stream-chat-android/pull/4512)

## stream-chat-android-pushprovider-huawei
### ❌ Removed
- Artifact removed, use `io.getstream:stream-android-push-huawei:VERSION` instead. [#4512](https://github.com/GetStream/stream-chat-android/pull/4512)

## stream-chat-android-pushprovider-xiaomi
### ❌ Removed
- Artifact removed, use `io.getstream:stream-android-push-xiaomi:VERSION` instead. [#4512](https://github.com/GetStream/stream-chat-android/pull/4512)

# November 11th, 2022 - 6.0.0-beta1

I hope you're interested in our latest major release - v6.0.0-beta1! We're currently in the process of preparing migration guides and updating all of our
documentation, but in the meantime, you can look into what we plan to release in v6 of Android Chat.

If you want to learn more about these changes and our decisions, check out our [Android Chat v6 Blog Post](https://getstream.io/blog/announcement-android-sdk-6-beta/).

## Common changes for all artifacts
### ⬆️ Improved
- Updated Kotlin version to `1.7.20`. (#4247)[https://github.com/GetStream/stream-chat-android/pull/4247]

### ⚠️ Changed
- Separated `state` and `offline` modules. (#4214)[https://github.com/GetStream/stream-chat-android/pull/4214]
- Moved `ClientState.user` to `GlobalState.user` because the state module is the one that correctly updates the user in the SDK. [#4333](https://github.com/GetStream/stream-chat-android/pull/4333)
- 🚨 Breaking change: Converted `Result` class into sealed class with two implementations: `Result.Success` and `Result.Failure`. [#4356](https://github.com/GetStream/stream-chat-android/pull/4356)
- 🚨 Breaking change: Converted `ChatError` class into sealed class with three implementations: `Error.Generic`, `Error.Throwable` and `Error.Network`. [#4368](https://github.com/GetStream/stream-chat-android/pull/4368)

## stream-chat-android-client
### ⬆️ Improved
- Removing unnecessary configuration fields for `OfflinePluginConfig`. [#4376](https://github.com/GetStream/stream-chat-android/pull/4376)

### ✅ Added
- Added `isFilteringMessages` check on `QueryChannelRequest` request. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)
- Exposed `MessageType`, `AttachmentType` and `ChannelType` classes containing useful constants. [#4285](https://github.com/GetStream/stream-chat-android/pull/4285)

### ⚠️ Changed
- Removed `Channel::cid` from constructor. It's now an immutable property calculated based on `type` and `id`. [#4322](https://github.com/GetStream/stream-chat-android/pull/4322)

### ❌ Removed
- ClientMutableState is now an internal interface, intead of a public interface. [#4374](https://github.com/GetStream/stream-chat-android/pull/4374)
- Remove ClientState.clearState() [#4372](https://github.com/GetStream/stream-chat-android/pull/4372)

## stream-chat-android-offline
### ✅ Added
- Added `loadNewestMessages` method to `ChatClient` that loads newest messages in the channel and clears the rest. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)

### ⚠️ Changed
- Changed the logic how the end of pages is determined inside `ChannelLogic.onQueryChannelResult`. Added loadNewestMessages in `ChannelLogic`. Added check to prevent upserting new messages if newest page isn't loaded to avoid breaking pagination. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)

## stream-chat-android-state
### 🐞 Fixed
- Stop showing a blink of empty state screen when loading channel without loading from database first. [#4261](https://github.com/GetStream/stream-chat-android/pull/4261)
- Fixing hard coded user presence of watchChannel method. [#4375](https://github.com/GetStream/stream-chat-android/pull/4375)

### ✅ Added
- Adding ChannelState.getMessageById to fetch messages from the state of the SDK. [#4292](https://github.com/GetStream/stream-chat-android/pull/4292)
- Added `loadNewestMessages` method to `ChatClient` that loads newest messages in the channel and clears the rest. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)

### ⚠️ Changed
- Separated `QueryChannelListenerState` into state and databased focused classes. [#4188](https://github.com/GetStream/stream-chat-android/pull/4188)
- Separated `ThreadQueryListener` into state and databased focused classes. [#4208](https://github.com/GetStream/stream-chat-android/pull/4208)
- Rename of `QueryChannelsListenerImpl` to `QueryChannelsListenerState` [#4170](https://github.com/GetStream/stream-chat-android/pull/4170)
- Renamed `ChannelData::channelId` to `ChannelData::id`. [#4322](https://github.com/GetStream/stream-chat-android/pull/4322)
- Removed `ChannelData::cid` from constructor. It's now an immutable property calculated based on `type` and `id`. [#4322](https://github.com/GetStream/stream-chat-android/pull/4322)
- Moved the send attachment logic to the LLC (Low Level Client) module [#4244](https://github.com/GetStream/stream-chat-android/pull/4244)

### ❌ Removed
- Removed `EventHandlerImpl` from the codebase. [#4207](https://github.com/GetStream/stream-chat-android/pull/4207)

## stream-chat-android-ui-common
### ⬆️ Improved
- Updated Compose compiler version to `1.3.2`. (#4247)[https://github.com/GetStream/stream-chat-android/pull/4247]

### ✅ Added
- Added `MessageListController` which generalizes message list state and actions, exposing them to each SDK's ViewModel. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added `MessageListState`, `MessageListItemState`, `MessageItemState`, `DateSeparatorItemState`, `ThreadSeparatorItemState`, `SystemMessageItemState`, `TypingItemState`, `MessagePosition`, `NewMessageState`, `SelectedMessageState` and `MessageFocusState` to keep track of the message list states. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added `ClipboardHandler` that handles copy/pasting. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added `DateSeparatorHandler` that handles when date separators should be shown. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added `MessagePositionHandler` that determines the message group position inside the list. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added `GiphyAction` to control giphies. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)

### ⚠️ Changed
- `DateFormatter::formatDate` and `DateFormatter::formatTime` methods now accept Dates instead of classes from ThreeTenABP API. [#4320](https://github.com/GetStream/stream-chat-android/pull/4320)

### ❌ Removed
- Removed ThreeTenABP dependency. [#4320](https://github.com/GetStream/stream-chat-android/pull/4320)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed scroll to bottom. [#3849](https://github.com/GetStream/stream-chat-android/pull/3849)
- Fixed search for messages. [#3861](https://github.com/GetStream/stream-chat-android/pull/3861)
- Fixed thread list initials scroll state. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)

### ⬆️ Improved
- Improved asking for `WRITE_EXTERNAL_STORAGE` permission. The permission won't be requested starting from Android Q unless legacy external storage is requested. [#4219](https://github.com/GetStream/stream-chat-android/pull/4219)
- Improved the stability of cooldown timer in slow mode. [#4251](https://github.com/GetStream/stream-chat-android/pull/4251)
- Improved how system bar colors are handled on the attachment gallery screen. [#4269](https://github.com/GetStream/stream-chat-android/pull/4269)
- The default attachment gallery is now able to handle videos as well as images. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- Improved the way video length information is displayed over video previews inside the attachment picker. [#4299](https://github.com/GetStream/stream-chat-android/pull/4299)
- Renamed `ImageAttachmentPreviewFactory` to `MediaAttachmentPreviewFactory` and gave it the ability to preview video attachments as well as image attachments. [#4386](https://github.com/GetStream/stream-chat-android/pull/4386)

### ✅ Added
- Added `UserAvatarView` and `ChannelAvatarView` to replace `AvatarView` to keep consistency with the Compose UI SDK. [#4165](https://github.com/GetStream/stream-chat-android/pull/4165)
- Added the ability to turn off video previews (thumbnails) via `ChatUI.videoThumbnailsEnabled`. Video previews are a paid feature and as such you can turn them off. They are on by default and the pricing can be found [here](https://getstream.io/chat/pricing/). [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added a new function `MessageListItemViewHolderFactory.createMediaAttachmentsViewHolder()` which returns a `ViewHolder` capable of previewing both images and videos. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added a style class called `MediaAttachmentViewStyle`. The new style controls how previews of both image and video attachments are displayed inside the message list. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added `UnsupportedAttachmentFactory` for unsupported attachments. [#4271](https://github.com/GetStream/stream-chat-android/pull/4271)
- Added attrs to `UnsupportedAttachmentsView` that allow to customize the UI of unsupported attachments in [#4271](https://github.com/GetStream/stream-chat-android/pull/4271):
 * `streamUiUnsupportedAttachmentBackgroundColor`
 * `streamUiUnsupportedAttachmentStrokeColor`
 * `streamUiUnsupportedAttachmentStrokeWidth`
 * `streamUiUnsupportedAttachmentCornerRadius`
 * `streamUiUnsupportedAttachmentTitleTextSize`
 * `streamUiUnsupportedAttachmentTitleTextColor`
 * `streamUiUnsupportedAttachmentTitleTextFont`
 * `streamUiUnsupportedAttachmentTitleFontAssets`
 * `streamUiUnsupportedAttachmentTitleTextStyle`
- Added a page about [Sample Apps](https://getstream.io/chat/docs/sdk/android/resources/sample-apps/) to the docs. [#4282](https://github.com/GetStream/stream-chat-android/pull/4282)
- Added the ability to turn off video previews (thumbnails) via `ChatUI.videoThumbnailsEnabled`. Video previews are a paid feature and as such you can turn them off. They are on by default and the pricing can be found [here](https://getstream.io/chat/pricing/). [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added a new function `MessageListItemViewHolderFactory.createMediaAttachmentsViewHolder()` which returns a `ViewHolder` capable of previewing both images and videos. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added a style class called `MediaAttachmentViewStyle`. The new style controls how previews of both image and video attachments are displayed inside the message list. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- Added `OnScrollToBottomHandler` to `MessageListView`. [#3849](https://github.com/GetStream/stream-chat-android/pull/3849)
- Added the ability to style the play button inside the attachment gallery. The necessary attributes along with their description can be found [here](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs_attachment_gallery_activity.xml). These attributes can be parsed using the newly created `AttachmentGalleryViewMediaStyle` class. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- Added new styled attributes for `MediaAttachmentGridView`. The new attributes along with their description can be found [here](https://github.com/GetStream/stream-chat-android/blob/main/src/main/res/values/attrs_media_attachment_grid_view.xml). These attributes can be parsed using the newly created `MediaAttachmentGridViewStyle` class. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- Added the attribute `streamUiAttachmentsPickerVideoIconDrawableTint` to the `AttachmentsPickerDialog` styleable. You can use this attribute to change to tint of the video drawable displayed in the attachment picker video previews. [#4299](https://github.com/GetStream/stream-chat-android/pull/4299)
- Added the attribute `streamUiAttachmentVideoLogoIconTint` to the `MessageInputView` styleable. You can use this attribute to change to tint of the video drawable displayed in the attachment picker video previews. [#4299](https://github.com/GetStream/stream-chat-android/pull/4299)
- Added the property `videoIconDrawableTint` to `AttachmentSelectionDialogStyle`. You can use this property to change to tint of the video drawable displayed in the attachment picker video previews. [#4299](https://github.com/GetStream/stream-chat-android/pull/4299)
- Added a guide that demonstrates how use events to close the chat screen when the current user has been removed from the channel. [#4078](https://github.com/GetStream/stream-chat-android/issues/4078)
- Added loading more indicator to `MessageListView`. [#4309](https://github.com/GetStream/stream-chat-android/pull/4309)
- Added the `streamUiMessageListLoadingMoreView` attribute to customize the layout of loading more indicator in `MessageListView`. [#4309](https://github.com/GetStream/stream-chat-android/pull/4309)
- Added the ability to preview video attachments as thumbnails inside `MessageComposerView` using `MediaAttachmentPreviewFactory`.  [#4386](https://github.com/GetStream/stream-chat-android/pull/4386)
- Added new attributes used by `MessageComposerView` that customize the way video attachments are rendered [#4386](https://github.com/GetStream/stream-chat-android/pull/4386)
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawable`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawableTint`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconBackgroundColor`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconElevation`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingTop`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingBottom`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingStart`
  * `streamUiMessageComposerMessageInputVideoAttachmentIconDrawablePaddingEnd`

### ⚠️ Changed
- 🚨 Breaking change: The function `MessageListItemViewHolderFactory.createImageAttachmentsViewHolder()` has been removed in favor of the function `MessageListItemViewHolderFactory.createMediaAttachmentsViewHolder()` which returns a `ViewHolder` capable of previewing both images and videos. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- 🚨 Breaking change: `ImageAttachmentViewStyle` has been removed and replaced by `MediaAttachmentViewStyle`. The new style controls how previews of both image and video attachments are displayed inside the message list. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- 🚨 Breaking change: Attribute `streamUiSaveImageEnabled` has been renamed to `streamUiSaveMediaEnabled`. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- 🚨 Breaking change: Attribute `streamUiSaveImageIcon` has been renamed to `streamUiSaveMediaIcon`. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- 🚨 Breaking change: String resource `stream_ui_attachment_gallery_save_image` has been renamed to `stream_ui_attachment_gallery_save_media`. [#4283](https://github.com/GetStream/stream-chat-android/pull/4283)
- Aligned the information displayed in the title and subtitle of `ChannelActionsDialogFragment` with the information in `MessageListHeaderView`. [#4306](https://github.com/GetStream/stream-chat-android/pull/4306)
- 🚨 Breaking change: `MessageListViewModel` now uses `MessageListController` for state and action handling. Updated `MessageListViewModelFactory` with new parameters to be able to build `MessageListController`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: `MessageListViewModel.Event.MessageReaction` no longer takes `enforceUnique` as a parameter, instead it is handled as part of `enforceUniqueReaction` inside `MessageListViewModelFactory` and `MessageListController`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: `ui-common` module `GiphyAction`s are now used instead of removed `GiphyAction` enum inside `ui-components` module. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: `MessageListItem` now uses `MessagePosition` from `ui-common`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Replaced the Dexter library for checking runtime permissions with the PermissionX library. [#4338](https://github.com/GetStream/stream-chat-android/pull/4338)
- Renamed `ImageAttachmentPreviewFactory` to `MediaAttachmentPreviewFactory`. [#4386](https://github.com/GetStream/stream-chat-android/pull/4386)

### ❌ Removed
- Removed `AvatarView` in favor of `UserAvatarView` and `ChannelAvatarView` to keep consistency with the Compose UI SDK. [#4165](https://github.com/GetStream/stream-chat-android/pull/4165)
- The function `MessageListItemViewHolderFactory.createImageAttachmentsViewHolder()` has been removed in favor of the function `MessageListItemViewHolderFactory.createMediaAttachmentsViewHolder()` which returns a `ViewHolder` capable of previewing both images and videos. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- `ImageAttachmentViewStyle` has been removed and replaced by `MediaAttachmentViewStyle`. The new style controls how previews of both image and video attachments are displayed inside the message list. [#4158](https://github.com/GetStream/stream-chat-android/pull/4158)
- 🚨 Breaking change: Removed the old `MessageInputView`. Use `MessageComposerView` instead. [#4289](https://github.com/GetStream/stream-chat-android/pull/4289)
- Removed `GiphyAction` enum. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Removed `MessageListItem.Position` enum. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed pagination when the newest messages aren't loaded and we are paginating newer messages pagination. Fixed scroll to bottom if the newest messages aren't loaded. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)
- Fixed thread list initials scroll state. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- Added a guide showing how to customize image and video previews. You can find it [here](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/#image-and-video) [#4373](https://github.com/GetStream/stream-chat-android/pull/4373)

### ⬆️ Improved
- Improved the way the [ChannelsScreen](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/) is built. [#4183](https://github.com/GetStream/stream-chat-android/pull/4183)
- Improved the way the [MessagesScreen](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/) is built. [#4183](https://github.com/GetStream/stream-chat-android/pull/4183)
- Improved automatic reloading of non-cached images when regaining network connection. The improvements are visible in the messages list and the new media gallery called `MediaGalleryPreviewActivity`. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Improved requesting `WRITE_EXTERNAL_STORAGE` permission when legacy storage is requested. [#4219](https://github.com/GetStream/stream-chat-android/pull/4219)
- Improved the stability of cooldown timer in slow mode. [#4251](https://github.com/GetStream/stream-chat-android/pull/4251)
- Improved how system bar colors are handled on the gallery screen. [#4267](https://github.com/GetStream/stream-chat-android/pull/4267)
- Improved the way video length information is displayed over video previews inside the attachment picker. [#4299](https://github.com/GetStream/stream-chat-android/pull/4299)
- The default factory for previewing video and image attachment now is `MediaAttachmentFactory`. It holds numerous improvements, the biggest of which are the ability to reload the image intelligently if the image wasn't loaded and network connection is re-established and the access to the new and improved media gallery. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)

### ✅ Added
- Added a new gallery called `MediaGalleryPreviewActivity`. This gallery is an upgrade over `ImagePreviewActivity` as it has the capability to reproduce videos as well as images, automatically reloads non-cached images upon regaining network connection and works in offline mode. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Added `MediaAttachmentContent`. The new composable is an improvement over `ImageAttachmentContent` as it has the ability to preview both videos and images and has access to the new and improved media gallery and the ability to tile more than 4 previews by modifying the parameter `maximumNumberOfPreviewedItems`. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Added `MediaAttachmentFactory`. The new factory is an improvement over `ImageAttachmentFactory`. The new factory hs the ability to preview videos and the ability to tile more than 4 previews in a group by changing the value of the parameter `maximumNumberOfPreviewedItems`. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Added parameters `attachmentsContentVideoMaxHeight`, `attachmentsContentMediaGridSpacing`, `attachmentsContentVideoWidth`, `attachmentsContentGroupPreviewWidth` and `attachmentsContentGroupPreviewHeight` to `StreamDimens`. These parameters are meant for more finer grained control over how media previews are displayed in the message list. For the best aesthetic outcome, the width of these should be equal to the value in `StreamDimens.messageItemMaxWidth`. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Added the ability to turn off video previews (thumbnails) via `ChatTheme.videoThumbnailsEnabled`. Video previews are a paid feature and as such you can turn them off. They are on by default and the pricing can be found [here](https://getstream.io/chat/pricing/). [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Added fallback factory for unsupported attachments. [#4270](https://github.com/GetStream/stream-chat-android/pull/4270)
- Added a page about [Sample Apps](https://getstream.io/chat/docs/sdk/android/resources/sample-apps/) to the docs. [#4282](https://github.com/GetStream/stream-chat-android/pull/4282)
- Added end pagination handler to `MessageList` and support for bidirectional pagination. Added scroll to bottom handler to `MessagesList` to load the newest messages before scrolling if they are not loaded already. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)
- Added `MessageLazyListState` to replace the default `LazyListState`. `MessageLazyListState` is used to track the scroll position of the message list as well as the focused message offset. [#3948](https://github.com/GetStream/stream-chat-android/pull/3948)
- Added properties `showMoreOverlay` and `showMoreCountText` to `ChatTheme`. These properties are designed to change the appearance of the show more count that appears when a message contains more media attachments than are able to be displayed in the message list media content preview. [#4293](https://github.com/GetStream/stream-chat-android/pull/4293)
- Added `TypingItemState` as a type of `MessageistItemState`. To show the typing item custom composable needs to be provided.

### ⚠️ Changed
- Changed the way ChannelsScreen and MessagesScreen components are built. Instead of exposing a ton of parameters for customization, we now expose a ViewModelFactory that accepts them. [#4183](https://github.com/GetStream/stream-chat-android/pull/4183)
- Using this new approach you can reuse and connect to ViewModels from the outside, if you want to power custom behavior. Make sure to check out our documentation regarding these components. [#4183](https://github.com/GetStream/stream-chat-android/pull/4183)
- 🚨 Breaking change: `MessageAttachmentsContent` function parameter `onImagePreviewResult: (ImagePreviewResult?) -> Unit` has been replaced with `onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit`. Functionally `ImagePreviewResult` and `MediaGalleryPreviewResult` are the same, the only difference is the activity they are returned from so changes should be minimal.
- 🚨 Breaking change: `QuotedMessageAttachmentContent` function parameter `onImagePreviewResult: (ImagePreviewResult?) -> Unit` has been replaced with `onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit`. Functionally `ImagePreviewResult` and `MediaGalleryPreviewResult` are the same, the only difference is the activity they are returned from so changes should be minimal.
- 🚨 Breaking change: `MessageContent` function parameter `onImagePreviewResult: (ImagePreviewResult?) -> Unit` has been replaced with `onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit`. Functionally `ImagePreviewResult` and `MediaGalleryPreviewResult` are the same, the only difference is the activity they are returned from so changes should be minimal.
- 🚨 Breaking change: `MessageContainer` function parameter `onImagePreviewResult: (ImagePreviewResult?) -> Unit` has been replaced with `onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit`. Functionally `ImagePreviewResult` and `MediaGalleryPreviewResult` are the same, the only difference is the activity they are returned from so changes should be minimal.
- 🚨 Breaking change: Both bound (with `MessageListViewModel` as a parameter) and unbound `MessageList` Composable functions have had parameter `onImagePreviewResult: (ImagePreviewResult?) -> Unit` replaced with `onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit`. Functionally `ImagePreviewResult` and `MediaGalleryPreviewResult` are the same, the only difference is the activity they are returned from so changes should be minimal.
- Video previews are now automatically displayed. These are a paid feature and can be turned off via `ChatTheme.videoThumbnailsEnabled`. If you are interested in the pricing before making a decision, you can find it [here](https://getstream.io/chat/pricing/). [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Started the deprecation process for `ImagePreviewActivity`, please use `MediaGalleryPreviewActivity` as it has all the functionality of the previous gallery while adding additional features such as video playback and offline capabilities. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Started the deprecation process for `ImageAttachmentFactory`, please use `MediaAttachmentFactory` as it has all the functionality of the previous factory while adding additional features such as displaying video previews modifiable number of tiles in a group preview. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Started the deprecation process for `ImageAttachmentContent`, please use `MediaAttachmentContent` as it has all the functionality of the previous component while adding additional features such as displaying video previous and modifiable number of tiles in a group preview. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- Started the deprecation process for `ImageAttachmentQuotedContent`, please use `MediaAttachmentQuotedContent` as it retains all of the previous functionality while adding the ability to preview video attachments. [#4096](https://github.com/GetStream/stream-chat-android/pull/4096)
- 🚨 Breaking change: Compose now uses `MessageListState`, `MessageListItemState`, `MessageItemState`, `DateSeparatorItemState`, `ThreadSeparatorItemState`, `SystemMessageItemState`, `TypingItemState`, `MessagePosition`, `NewMessageState`, `SelectedMessageState` and `MessageFocusState` found in `ui-common` package. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: `MessageListViewModel` now uses `MessageListController` for state and action handling. Updated `MessagesViewModelFactory` with new parameters to be able to build `MessageListController`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)

### ❌ Removed
- 🚨 Breaking change: Removed compose `MessageMode` indicating whether the list is in thread mode or normal mode in favor of ui-common `MessageMode`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: Removed compose models in favor of `ui-common` models: `MessageListState`, `MessageListItemState`, `MessageItemState`, `DateSeparatorState`, `ThreadSeparatorState`, `SystemMessageState`, `MessagePosition`, `NewMessageState`, `SelectedMessageState` and `MessageFocusState`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)
- 🚨 Breaking change: Removed `MessageListViewModel.focusMessage()`. To achieve the same effect use `MessageListViewModel.scrollToMessage(messageId: String)`. [#4157](https://github.com/GetStream/stream-chat-android/pull/4157/files)

# March 17th, 2023 - 5.15.0
## Common changes for all artifacts
### ⚠️ Changed
- Upgrade ThreeTenBP and ThreeTenABP to support new added timezones. [#4734](https://github.com/GetStream/stream-chat-android/pull/4734)

# March 7th, 2023 - 5.14.0
## stream-chat-android-client

### ✅ Added
- Added the following parameters to `Message`. [#4701](https://github.com/GetStream/stream-chat-android/pull/4701)
  * `skipPushNotification`: when set to `true` a newly sent message will not trigger a push notification.
  * `skipEnrichUrl`: when set to `true` the URL contained inside the message will not be enriched as a link

## stream-chat-android-compose

### ✅ Added
- Added the property `skipEnrichUrl` to `ImagePreviewViewModelFactory`, `ImagePreviewViewModel` and `ImagePreviewContract.Input` constructors and the functions `StreamAttachmentFactories.defaultFactories()`, `ImageAttachmentFactory()`, `ImageAttachmentContent()`. When set to false, updating a message by deleting an attachment inside the message will skip the URL enrichment process, meaning the links will not be transformed to link attachments. Any existing link attachments will be preserved. [#4701](https://github.com/GetStream/stream-chat-android/pull/4701)
- Added the following parameters to `MessagesScreen`. [#4701](https://github.com/GetStream/stream-chat-android/pull/4701)
  * `skipPushNotification`: when set to `true` a newly sent message will not trigger a push notification.
  * `skipEnrichUrl`: when set to `true` the URL contained inside the message will not be enriched as a link.

# February 23rd, 2023 - 5.13.0
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed image scaling when width is bigger than height. [#4659](https://github.com/GetStream/stream-chat-android/pull/4659)
- Fixed date separator handlers not being applied when calling `MessageListViewModel.setDateSeparatorHandler()` and  `MessageListViewModel.()`. [#4681](https://github.com/GetStream/stream-chat-android/pull/4681)

### ⬆️ Improved
- When creating message previews, `Attachment.fallback` is now included as a fallback option after `Attachment.title` and `Attachment.name`. [#4667](https://github.com/GetStream/stream-chat-android/pull/4667)

### ✅ Added
- Added a feature flag to `ChatUI` called `showThreadSeparatorInEmptyThread`. You can use this to enable a thread separator if the thread is empty. [#4629](https://github.com/GetStream/stream-chat-android/pull/4629)
- Added the `messageLimit` parameter to MessageListViewModel and MessageListViewModelFactory. [#4634](https://github.com/GetStream/stream-chat-android/pull/4634)
- Added the method `showModeratedMessageDialog()` to `MessageListView`. It is used to display a dialog when long clicking on a message that has failed a moderation check. [#4645](https://github.com/GetStream/stream-chat-android/pull/4645)
- Added the ability to style the way message reply bubbles are displayed in `MessageComposerView` via xml attributes. The styling applies both when replying to messages sent by the currently logged-in user, and those sent by other users. [#4679](https://github.com/GetStream/stream-chat-android/pull/4679)
  * `streamUiMessageComposerMessageReplyBackgroundColor`
  * `streamUiMessageComposerMessageReplyTextSizeMine`
  * `streamUiMessageComposerMessageReplyTextColorMine`
  * `streamUiMessageComposerMessageReplyTextFontMine`
  * `streamUiMessageComposerMessageReplyTextFontAssetsMine`
  * `streamUiMessageComposerMessageReplyTextStyleMine`
  * `streamUiMessageComposerMessageReplyStrokeColorMine`
  * `streamUiMessageComposerMessageReplyStrokeWidthMine`
  * `streamUiMessageComposerMessageReplyTextSizeTheirs`
  * `streamUiMessageComposerMessageReplyTextColorTheirs`
  * `streamUiMessageComposerMessageReplyTextFontTheirs`
  * `streamUiMessageComposerMessageReplyTextFontAssetsTheirs`
  * `streamUiMessageComposerMessageReplyTextStyleTheirs`
  * `streamUiMessageComposerMessageReplyStrokeColorTheirs`
  * `streamUiMessageComposerMessageReplyStrokeWidthTheirs`
- Added new properties to `MessageComposerViewStyle` which allow styling the way message reply bubbles are displayed in `MessageComposerView`. The styling applies both when replying to messages sent by the currently logged-in user, and those sent by other users. [#4679](https://github.com/GetStream/stream-chat-android/pull/4679)
  * `messageReplyBackgroundColor`
  * `messageReplyTextStyleMine`
  * `messageReplyMessageBackgroundStrokeColorMine`
  * `messageReplyMessageBackgroundStrokeWidthMine`
  * `messageReplyTextStyleTheirs`
  * `messageReplyMessageBackgroundStrokeColorTheirs`
  * `messageReplyMessageBackgroundStrokeWidthTheirs`
- Added the ability to style the way message reply bubbles are displayed in `MessageInputView` via xml attributes. The styling applies both when replying to messages sent by the currently logged-in user, and those sent by other users. [#4679](https://github.com/GetStream/stream-chat-android/pull/4679)
  * `streamUiMessageInputMessageReplyBackgroundColor`
  * `streamUiMessageInputMessageReplyTextSizeMine`
  * `streamUiMessageInputMessageReplyTextColorMine`
  * `streamUiMessageInputMessageReplyTextFontMine`
  * `streamUiMessageInputMessageReplyTextFontAssetsMine`
  * `streamUiMessageInputMessageReplyTextStyleMine`
  * `streamUiMessageInputMessageReplyStrokeColorMine`
  * `streamUiMessageInputMessageReplyStrokeWidthMine`
  * `streamUiMessageInputMessageReplyTextSizeTheirs`
  * `streamUiMessageInputMessageReplyTextColorTheirs`
  * `streamUiMessageInputMessageReplyTextFontTheirs`
  * `streamUiMessageInputMessageReplyTextFontAssetsTheirs`
  * `streamUiMessageInputMessageReplyTextStyleTheirs`
  * `streamUiMessageInputMessageReplyStrokeColorTheirs`
  * `streamUiMessageInputMessageReplyStrokeWidthTheirs`
- Added new properties to `MessageComposerViewStyle` which allow styling the way message reply bubbles are displayed in `MessageInputView`. The styling applies both when replying to messages sent by the currently logged-in user, and those sent by other users. [#4679](https://github.com/GetStream/stream-chat-android/pull/4679)
  * `messageReplyBackgroundColor`
  * `messageReplyTextStyleMine`
  * `messageReplyMessageBackgroundStrokeColorMine`
  * `messageReplyMessageBackgroundStrokeWidthMine`
  * `messageReplyTextStyleTheirs`
  * `messageReplyMessageBackgroundStrokeColorTheirs`
  * `messageReplyMessageBackgroundStrokeWidthTheirs`

### ⚠️ Changed
- The styling for the reply message bubbles visible inside `MessageInputView` and `MessageComposerView` when replying to messages has changed slightly and is now the same for both messages sent by the currently logged-in user and those sent by other users. However, you are now able to style the bubbles. For more information check the added section for `stream-chat-android-ui-components`. [#4679](https://github.com/GetStream/stream-chat-android/pull/4679)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed Compose Previews for ChatTheme and other minor components like `MessageText`. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)

### ⬆️ Improved
- When creating message previews, `Attachment.fallback` is now included as a fallback option after `Attachment.title` and `Attachment.name`. [#4667](https://github.com/GetStream/stream-chat-android/pull/4667)

### ✅ Added
- Added the parameter `channelOptions: List<ChannelOptionState>` to `SelectedChannelMenu` allowing users to override the default channel options more easily. The parameter comes with a default argument of `buildDefaultChannelOptionsState()`. [#4671](https://github.com/GetStream/stream-chat-android/pull/4671)

### ⚠️ Changed
- Added the parameter `channelOptions: List<ChannelOptionState>` to `SelectedChannelMenu` allowing users to override the default channel options more easily. The parameter comes with a default argument of `buildDefaultChannelOptionsState()`. [#4671](https://github.com/GetStream/stream-chat-android/pull/4671)
- Added `currentUser` as a parameter to `MessageContent` and `MessageText`. These are non-optional, but nullable, parameters that define the behavior and looks of these components. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)
- Similarly, added `currentUser` as a parameter to `QuotedMessage`, `QuotedMessageContent` and `QuotedMessageText`.

### ❌ Removed
The following items are breaking changes, since it was very important to improve/fix the behavior. The items described were used to expose customizable permission handlers which can be reused. However, this API is experimental and breaking for Previews, so we chose to go down a different path.
- Removed PermissionHandler and its API. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)
- Removed DownloadPermissionHandler. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)
- Removed StreamPermissionHandlers. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)
- Removed `permissionHandlers` parameter from `ChatTheme`, this should make it easier to preview components within Android Studio. [#4672](https://github.com/GetStream/stream-chat-android/pull/4672)

# January 31st, 2023 - 5.12.0
## stream-chat-android-client
### ⬆️ Improved
- Added offline plugin integration to the `ChatCliet.getMessage()` method. If you use the offline plugin, any message fetched using this method will be stored inside the database upon successful completion of the API call. [#4623](https://github.com/GetStream/stream-chat-android/pull/4623)

## stream-chat-android-offline
### ✅ Added
- Added the `GetMessageListener` interface used to perform actions as side effects when the `ChatCliet.getMessage()` method is used to fetch a single message from the backend. [#4623](https://github.com/GetStream/stream-chat-android/pull/4623)

## stream-chat-android-ui-common
### ⬆️ Improved
- `MessageComposerController` will now query the server for a list of channel members if the input contains a mention symbol (@) and no user name matching the expression after the symbol @ was found in the local state containing a list of channel members. [#4647](https://github.com/GetStream/stream-chat-android/pull/4647)

## stream-chat-android-ui-components
### ⬆️ Improved
- The default implementation of `MessageInputView` will now query channel members from the server if a mention lookup fails to find the matching channel member using the data available in the local state. [#4647](https://github.com/GetStream/stream-chat-android/pull/4647)

### ✅ Added
- Added a feature flag to `ChatUI` called `showThreadSeparatorInEmptyThread`. You can use this to enable a thread separator if the thread is empty. [#4629](https://github.com/GetStream/stream-chat-android/pull/4629)
- Added the `messageLimit` parameter to MessageListViewModel and MessageListViewModelFactory. [#4634](https://github.com/GetStream/stream-chat-android/pull/4634)
- Added lambda parameter `queryMembersOnline` to `DefaultUserLookupHandler`. The lambda parameter is used internally by `DefaultUserLookupHandler.handleUserLookup()` when no matches could be found inside the list of users contained by `DefaultUserLookupHandler.users`. It should be used to query members from the server and return the results. [#4647](https://github.com/GetStream/stream-chat-android/pull/4647)
- Added the feature flag boolean `navigateToThreadViaNotification` to `MessageListViewModel` and `MessageListViewModelFactory`. If it is set to true and a thread message has been received via push notification, clicking on the notification will make the SDK automatically navigate to the thread. If set to false, the SDK will always navigate to the channel containing the thread without navigating to the thread itself. [#4612](https://github.com/GetStream/stream-chat-android/pull/4612)

## stream-chat-android-compose
### ✅ Added
- Added the parameter `messageId: String?` to `MessageListViewModel` and `MessageListViewModelFactory`. If `navigateToThreadViaNotification` is set to true (see the changelog entry below), it will enable navigating to threads upon clicking a push notification triggered by a thread message. [#4612](https://github.com/GetStream/stream-chat-android/pull/4612)
- Added the feature flag boolean `navigateToThreadViaNotification: Boolean` to `MessageListViewModel` and `MessageListViewModelFactory`. If it is set to true and a thread message has been received via push notification, clicking on the notification will make the SDK automatically navigate to the thread. If set to false, the SDK will always navigate to the channel containing the thread without navigating to the thread itself. [#4612](https://github.com/GetStream/stream-chat-android/pull/4612)

# December 22nd, 2022 - 5.11.10
## stream-chat-android-offline
### 🐞 Fixed
- Allowed downloading `Attachment`s with missing `title` and `name` properties by ensuring a fallback name is present. Please note that the properties should be populated, this is only used to guard against edge cases. [#4599](https://github.com/GetStream/stream-chat-android/pull/4599)

# December 5th, 2022 - 5.11.9
## stream-chat-android-ui-common
### ✅ Added
- Exposed a way to allow you to include the current user avatar in the Channel avatar [#4561](https://github.com/GetStream/stream-chat-android/pull/4561)

# November 23th, 2022 - 5.11.8
## stream-chat-android-client
### ⬆️ Improved
- Return an error when invoking `ChatClient::disconnect` without a connected user. [#4494](https://github.com/GetStream/stream-chat-android/pull/4494)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed `IllegalArgumentException` when uploading attachments fails. [#4487](https://github.com/GetStream/stream-chat-android/pull/4487)
- Fixed returning `ChannelsStateData.OfflineNoResults` from `QueryChannelsState::channelsStateData` when API call is still in progress. [#4496](https://github.com/GetStream/stream-chat-android/pull/4496)
- Fixed returning an empty map from `QueryChannelsState::channels` when API call is still in progress. [#4496](https://github.com/GetStream/stream-chat-android/pull/4496)

## stream-chat-android-ui-components
### ✅ Added
- Added the `streamUiShowReactionsForUnsentMessages` attribute to `MessageListView` that allows to show/hide the edit reactions bubble for unsent messages on the options overlay. [#4449](https://github.com/GetStream/stream-chat-android/pull/4449)

### 🐞 Fixed
- Fixed empty placeholder blinking on `ChannelListView` when loading channels. [#4496](https://github.com/GetStream/stream-chat-android/pull/4496)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed empty placeholder blinking on `ChannelList` when loading channels. [#4496](https://github.com/GetStream/stream-chat-android/pull/4496)

# November 18th, 2022 - 5.11.7
## stream-chat-android-client
### ⬆️ Improved
- Improved logs for sending message with attachments. [#4448](https://github.com/GetStream/stream-chat-android/pull/4448)

### ⚠️ Changed
- Changed default worker's constraints from `NetworkType.NOT_ROAMING` to `NetworkType.CONNECTED`. [#4448](https://github.com/GetStream/stream-chat-android/pull/4448)

# November 16th, 2022 - 5.11.6
## stream-chat-android-client
### 🐞 Fixed
- Fixed the race condition when connecting the user just after receiving a push notification when the application is killed. [#4429](https://github.com/GetStream/stream-chat-android/pull/4429)

# November 15th, 2022 - 5.11.5
## stream-chat-android-client
### 🐞 Fixed
- Fixing postponing api calls to avoid showing empty screen in the wrong moment. [#4344](https://github.com/GetStream/stream-chat-android/pull/4344)

### ✅ Added
- Added `thumbUrl` field to the return type of the `FileUploader::sendImage` method, so that clients are able to return image thumb URL when implementing their custom `FileUploader`. [#4423](https://github.com/GetStream/stream-chat-android/pull/4423)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed an edge case in which deleting an attachment using the attachment gallery would not delete it given the message was freshly uploaded.  [#4349](https://github.com/GetStream/stream-chat-android/pull/4349)
- When a user uploads image attachments in a message that contains links, the link attachments are no longer displayed inside the attachment gallery along with the image attachments. [#4399](https://github.com/GetStream/stream-chat-android/pull/4399)

## stream-chat-android-compose
### ⬆️ Improved
- When a user uploads image attachments in a message that contains links, the link attachments are no longer displayed inside the attachment gallery along with the image attachments. [#4399](https://github.com/GetStream/stream-chat-android/pull/4399)

# November 3th, 2022 - 5.11.4
## stream-chat-android-compose
### ✅ Added
- Added `ownMessageQuotedBackground`, `otherMessageQuotedBackground`, `ownMessageQuotedText` and `otherMessageQuotedText` options to `StreamColors`, to make it possible to customize the appearance of quoted messages via `ChatTheme`. [#4335](https://github.com/GetStream/stream-chat-android/pull/4335)

## stream-chat-android-pushprovider-firebase
### 🐞 Fixed
- Fix multi-bundle feature when using Firebase as Push Provider. [#4341](https://github.com/GetStream/stream-chat-android/pull/4341)

# October 31th, 2022 - 5.11.3
## stream-chat-android-client
### 🐞 Fixed
- Fixed `OutOfMemoryException` in `HttpLoggingInterceptor` when sending big attachments. [#4314](https://github.com/GetStream/stream-chat-android/pull/4314)

# October 17th, 2022 - 5.11.2
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed displaying messages with failed image attachments. [#4234](https://github.com/GetStream/stream-chat-android/pull/4234)
- Fixes problem when alligning reactions ballon for custom ViewHolders in message options dialog. [#4248](https://github.com/GetStream/stream-chat-android/pull/4248)

### ⬆️ Improved
- Improved asking for `WRITE_EXTERNAL_STORAGE` permission. The permission won't be requested starting from Android Q unless legacy external storage is requested. [#4219](https://github.com/GetStream/stream-chat-android/pull/4219)
- When `ChatClient.disconnect` is called, only remove the device on backend side if `flushPersistence` is `true` [#4280](https://github.com/GetStream/stream-chat-android/pull/4280)

## stream-chat-android-compose
### ⬆️ Improved
- Improved requesting `WRITE_EXTERNAL_STORAGE` permission when legacy storage is requested. [#4219](https://github.com/GetStream/stream-chat-android/pull/4219)

# September 30th, 2022 - 5.11.1
## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed giphy size parsing. [#4222](https://github.com/GetStream/stream-chat-android/pull/4222)

# September 21th, 2022 - 5.11.0
## stream-chat-android-client
### ⬆️ Improved
- Reviewed requests parameters. `connectionId` is now sent only if necessary. [#4138](https://github.com/GetStream/stream-chat-android/pull/4138)

### ❌ Removed
- 🚨 Breaking change: Removed `connectionId` parameter from `FileUploader` methods. [#4138](https://github.com/GetStream/stream-chat-android/pull/4138)

## stream-chat-android-state
### ⚠️ Changed
- Divided QueryChannelLogic into state and database. [#4156](https://github.com/GetStream/stream-chat-android/pull/4156)

## stream-chat-android-compose
### ✅ Added
- Added `ownMessageText` and `otherMessageText` to `StreamColors` to enable message text customization. If you have been using `StreamColors.textHighEmphasis` to customize the color of the message texts, we recommend switching to the new attributes instead. [#4175](https://github.com/GetStream/stream-chat-android/pull/4175)

# September 13th, 2022 - 5.10.0
## stream-chat-android-client
### ⬆️ Improved
- Improving precision in time in the endpoint that syncs information between SDK and Stream's backend to make sure that undesired events are not coming due to a incorrect round down in time or desired events are not being ignored due to a incorrect round up of time when serializing/desirializing time in the SDK. [#4102](https://github.com/GetStream/stream-chat-android/pull/4102)

### ✅ Added
- Added `ChatEvent.rawDate` to access the time of an event as it was sent by the backend. This class includes microseconds precision and can be used when a higher precision than miliseconds is desired. [#4102](https://github.com/GetStream/stream-chat-android/pull/4102)
- Added [Handling User Connection](https://getstream.io/chat/docs/sdk/android/client/guides/handling-user-connection/) guide. [#4131](https://github.com/GetStream/stream-chat-android/pull/4131)
- Supported Android 13 behaviour changes. [#4039](https://github.com/GetStream/stream-chat-android/pull/4039)

## stream-chat-android-state
### 🐞 Fixed
- Fixed incrementing unread count if the message is already in the state. [#4135](https://github.com/GetStream/stream-chat-android/pull/4135)

## stream-chat-android-ui-common
### ⬆️ Improved
- Improved slow mode countdown which is now started only after the message is sent to the server. [#4120](https://github.com/GetStream/stream-chat-android/pull/4120)

## stream-chat-android-ui-components
### ⬆️ Improved
- Clicking or long clicking on the white spaces next to messages will no longer trigger listeners, from now on, only clicking on the actual message containers will. [#4151](https://github.com/GetStream/stream-chat-android/pull/4151)

### ✅ Added
- Added the `MessageListView::showMessageOptionsDialog` method to show message options dialog. [#4127](https://github.com/GetStream/stream-chat-android/pull/4127)
- Added styled attribute `streamUiGiphyMediaAttachmentSizingMode` and it's programmatic counterpart `GiphyMediaAttachmentViewStyle.sizingMode`. This parameters controls the way Giphy containers resize themselves. For a care free experience use adaptive resizing which will intelligently adapt its size while respecting the gif's aspect ratio. We recommend using adaptive resizing, however if you require more control use manual mode. [#4134](https://github.com/GetStream/stream-chat-android/pull/4134)
- Added styled attributes `streamUiGiphyMediaAttachmentWidth`, `streamUiGiphyMediaAttachmentHeight`, `streamUiGiphyMediaAttachmentDimensionRatio` and their programmatic counterparts contained within `GiphyMediaAttachmentViewStyle` that are `width`, `height` and `dimensionRatio`. These values are used to exert more control over Giphys. They are ignored if sizing mode is set to adaptive and respected if it is set to manual. [#4134](https://github.com/GetStream/stream-chat-android/pull/4134)

### ⚠️ Changed
- Exposed `MessageOptionsDialogFragment` so that clients are able to create and show the dialog manually. [#4127](https://github.com/GetStream/stream-chat-android/pull/4127)
- 🚨 Breaking change: `streamUiGiphyMediaAttachmentGiphyType` and its programmatic counterpart `GiphyMediaAttachmentViewStyle.giphyType` no longer change the container size. Container sizing has been decoupled from giphy scale type which now only controls the quality of the gif itself. If you need fixed sizes, set the sizing mode to manual and manipulate the container by setting custom width, height and if needed dimension ratio and scale type. Please check the added section for the full names of the new attributes. [#4134](https://github.com/GetStream/stream-chat-android/pull/4134)

## stream-chat-android-compose
### ⬆️ Improved
- Improved the way Giphy attachments are displayed. Instead of being cropped they can now be set to either respect the Giphy attachment's aspect ratio and adaptively resize themselves automatically or be manually sized in a fixed way by the user. [#4027](https://github.com/GetStream/stream-chat-android/pull/4027)
- Added the ability to control Giphy quality, sizing mode and content scaling by adding the parameters `GiphyInfoType` (controls quality), `GiphySizingMode` (controls sizing) and `contentScale` (controls scaling) to the functions `StreamAttachmentFactories.defaultFactories()`, `GiphyAttachmentFactory()` and `GiphyAttachmentContent()`. [#4027](https://github.com/GetStream/stream-chat-android/pull/4027)

### ✅ Added
- Added `GiphySizingMode`, an enum class used to control the way Giphys are displayed. When `GiphySizingMode.ADAPTIVE` is passed in as an argument it will make Giphy automatically resize themselves to fill available space while respecting their original aspect ratios, while `GiphySizingMode.FIXED_SIZE` will allow you to manually control the dimensions of the Giphy container in a fixed size manner. You can clip the maximum height and width in both cases by modifying `ChatTheme.attachmentsContentGiphyMaxWidth` and `attachmentsContentGiphyMaxHeight`. `GiphySizingMode` can be passed in as an argument to the following functions: `StreamAttachmentFactories.defaultFactories()`, `GiphyAttachmentFactory()` and `GiphyAttachmentContent()`. [#4027](https://github.com/GetStream/stream-chat-android/pull/4027)
- Added parameters `attachmentsContentGiphyMaxWidth` and `attachmentsContentGiphyMaxHeight` to `StreamDimens`. These parameters can be used to control the maximum dimensions of Giphys in either sizing mode. [#4027](https://github.com/GetStream/stream-chat-android/pull/4027)

# August 30th, 2022 - 5.9.0
## stream-chat-android-client
### ⬆️ Improved
- Show rounded avatars on Push Notification when `MessagingStyleNotificationHandler` is used. [#4059](https://github.com/GetStream/stream-chat-android/pull/4059)
- Add an option to use a custom implementation when showing avatars on Push Notifications when `MessagingStyleNotificationHandler` is used. [#4069](https://github.com/GetStream/stream-chat-android/pull/4069)

### ✅ Added
- Method to switch between users `ChatClient.switchUser`. Can be used for switching between users to simplify code for disconnecting and connecting to the SDK. [#4018](https://github.com/GetStream/stream-chat-android/pull/4018)
- Added `UploadedFile` which represents an uploaded file. It contains the url to the file under the property `file`, and a thumbnail of the file under the property `thumbUrl`. Thumbnails are usually returned when uploading a video file. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)

### ⚠️ Changed
- 🚨 Breaking change: `ChatClient.sendFile` now returns `UploadedFile` instead of `String`. `UploadedFile.file` is the equivalent of the previous return value. If you do not need the other parts of `UploadedFile`, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: `ChannelClient.sendFile` now returns `UploadedFile` instead of `String`. `UploadedFile.file` is the equivalent of the previous return value. If you do not need the other parts of `UploadedFile`, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: Overloaded functions `FileUploader.sendFile()` have had their signatures changed. Instead of returning `String`, they are now supposed to return `UploadedFile`. If you have extended this interface and only need the previous return functionality, you can assign a value to `UploadedFile.file` while keeping `UploadedFile.thumbUrl` `null`. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: Overloaded functions `StreamFileUploader.sendFile()` have had their signatures changed. Instead of returning `String`, they now return `UploadedFile`. If you do not need the other parts of `UploadedFile`, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: `ChatClient.sendImage` now returns `UploadedImage` instead of `String`. `UploadedImage.file` is the equivalent of the previous return value, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: `ChannelClient.sendImage` now returns `UploadedImage` instead of `String`. `UploadedImage.file` is the equivalent of the previous return value, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: Overloaded functions `FileUploader.sendImage()` have had their signatures changed. Instead of returning `String`, they are now supposed to return `UploadedImage`. To mitigate these changes, you can wrap your previously returned file URL inside `UploadedImage`. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)
- 🚨 Breaking change: Overloaded functions `StreamFileUploader.sendImage()` have had their signatures changed. Instead of returning `String`, they now return `UploadedImage`, you can use `.map { it.file }` to mitigate the breaking change. [#4058](https://github.com/GetStream/stream-chat-android/pull/4058)

## stream-chat-android-state
### ⚠️ Changed
- `EventHandlerSequential` is now in use by default.

## stream-chat-android-offline
### 🐞 Fixed
- Removed calls to Kotlin Collection's `getOrDefault()` inside `TypingEventPruner`. The function is not available below Android API 24 and was causing exceptions. [#4100](https://github.com/GetStream/stream-chat-android/pull/4100)

## stream-chat-android-ui-common
### ✅ Added
- Added `hasCommands` field to `MessageComposerState` to set commands button visibility. [#4057](https://github.com/GetStream/stream-chat-android/pull/4057)
- Add an implementation of `UserIconBuilder` which uses Coil to load the avatar picture. [#4069](https://github.com/GetStream/stream-chat-android/pull/4069)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed user avatar in navigation drawer of the sample app. [#4050](https://github.com/GetStream/stream-chat-android/pull/4050)
- The commands button in `MessageComposerView` can now be used to hide the command suggestion popup. [#4041](https://github.com/GetStream/stream-chat-android/pull/4041)
- Now "Quotes" toggle in the dashboard controls the "Reply" option in the message list. [#4074](https://github.com/GetStream/stream-chat-android/pull/4074)
- Now "Threads & Replies" toggle in the dashboard controls the "Thread Reply" option in the message list. [#4074](https://github.com/GetStream/stream-chat-android/pull/4074)
- Fixed a bug that made `MessageInputView` not adhere to integration visibility attributes (`streamUiAttachButtonEnabled` and `streamUiLightningButtonEnabled`). [#4107](https://github.com/GetStream/stream-chat-android/pull/4107)
- Fixed scroll state on filter change. [#4105](https://github.com/GetStream/stream-chat-android/pull/4105/files)

### ⬆️ Improved
- Added check to hide command button if no commands are available in `MessageInputView` and `MessageComposerView`. [#4057](https://github.com/GetStream/stream-chat-android/pull/4057)
- Revert workaround for setting `ChatUI::imageHeadersProvider` introduced in [#3237](https://github.com/GetStream/stream-chat-android/pull/3237). [#4065](https://github.com/GetStream/stream-chat-android/pull/4065)
- Integration button visibility inside `MessageComposerView` dictated by attributes `streamUiMessageComposerCommandsButtonVisible` and `streamUiMessageComposerAttachmentsButtonVisible` is now set prior to binding the ViewModel, improving compliance and possible flickering issues. [#4107](https://github.com/GetStream/stream-chat-android/pull/4107)

### ✅ Added
- Added the `stream-chat-android-ui-guides` application that showcases different customizations of the SDK. [#4024](https://github.com/GetStream/stream-chat-android/pull/4024)

### ⚠️ Changed
- 🚨 Breaking change: `ChannelListUpdateListener` is now tasked with scrolling the list to the bottom once the `ChannelListItem.LoadingMoreItem` is inserted after requesting a new page of `Channel`s. If `ChannelListUpdateListener` inside `ChannelListView` is overridden in order to keep the scroll to bottom when loading a new page please copy the default implementation to the custom implementation of the listener. [#4105](https://github.com/GetStream/stream-chat-android/pull/4105/files)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the online member count indicator in the message list header. Previously it did not properly track members going offline. [#4043](https://github.com/GetStream/stream-chat-android/pull/4043)
- Now "Quotes" toggle in the dashboard controls the "Reply" option in the message list. [#4074](https://github.com/GetStream/stream-chat-android/pull/4074)
- Now "Threads & Replies" toggle in the dashboard controls the "Thread Reply" option in the message list. [#4074](https://github.com/GetStream/stream-chat-android/pull/4074)

### ⬆️ Improved
- Added check to hide command button if no commands are available in `MessageComposer`. [#4057](https://github.com/GetStream/stream-chat-android/pull/4057)

### ✅ Added
- Added the `stream-chat-android-ui-guides` application that showcases different customizations of the SDK. [#4024](https://github.com/GetStream/stream-chat-android/pull/4024)

# August 24th, 2022 - 5.8.2
## stream-chat-android-offline
### 🐞 Fixed
- Fixed `useSequentialEventHandler` config parameter that was passed to `StreamOfflinePluginFactory` but had no effect. [#4089](https://github.com/GetStream/stream-chat-android/pull/4089)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed stale online member count in `MessageListHeaderView` when a member leaves the channel. [#4072](https://github.com/GetStream/stream-chat-android/pull/4072)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed stale online member count in `MessageListHeader` when a member leaves the channel. [#4072](https://github.com/GetStream/stream-chat-android/pull/4072)

# August 22nd, 2022 - 5.8.1
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed loading of image attachments with null values of `Attachment.originalWidth` and `Attachment.originalHeight`. A bug was introduced in the previous release that made these image attachments not load as their container height would remain set to 0. [#4067](https://github.com/GetStream/stream-chat-android/pull/4067)

# August 16th, 2022 - 5.8.0
## Common changes for all artifacts
### ⚠️ Changed
- Updated external libraries version. Check the PR to get more details.[#3976](https://github.com/GetStream/stream-chat-android/pull/3976)
- Updated Compose Compiler version to `1.3.0`, Compose UI version to `1.2.1`,  and Kotlin version to `1.7.10`. [#4019](https://github.com/GetStream/stream-chat-android/pull/4019)

## stream-chat-android-client
### 🐞 Fixed
- Rename of field for optional multi bundle push provider. Now projects with multiple push providers will correct correctly. [#4008](https://github.com/GetStream/stream-chat-android/pull/4008)
- Fixed blinking unread count indicator. [#4030](https://github.com/GetStream/stream-chat-android/pull/4030)
- Fixed push notification reply action. [#4046](https://github.com/GetStream/stream-chat-android/pull/4046)

### ✅ Added
- Added properties `originalHeight` and `originalWidth` to `Attachment`. These represent the original dimensions of an image attachment. [#4011](https://github.com/GetStream/stream-chat-android/pull/4011)

## stream-chat-android-offline
### ⬆️ Improved
- Improved updating channels after receiving `NotificationMessageNew` event. [#3991](https://github.com/GetStream/stream-chat-android/pull/3991)
- Improved updating channels after receiving `NewMessageEvent`. The channel will be added to the list if the message is not a system message. [#3999](https://github.com/GetStream/stream-chat-android/pull/3999)
- `ThreadState` is now independent from `ChannelState`. [#3959]

### ✅ Added
- `loading` is added to `ThreadState`. [#3959]

### ⚠️ Changed
- Deprecated `NonMemberChatEventHandler`. Use `BaseChatEventHandler` or `DefaultChatEventHandler` for custom implementation. [#3991](https://github.com/GetStream/stream-chat-android/pull/3991)
- Deprecated multiple event specific `BaseChatEventHandler` methods . Use `handleChatEvent()` or `handleCidEvent()` instead. [#3991](https://github.com/GetStream/stream-chat-android/pull/3991)
- Made `DefaultChatEventHandler` open. You can extend it to change default member-based events handling. [#3991](https://github.com/GetStream/stream-chat-android/pull/3991)
- 🚨 Breaking change: `ChatEventHandlerFactory::chatEventHandler` signature was changed. It now requires `StateFlow<Map<String, Channel>?>` instead of `StateFlow<List<Channel>?>` [#3992](https://github.com/GetStream/stream-chat-android/pull/3992)
- Added additional `chatEventHandlerFactory` parameter to `ChatClient.queryChannelsAsState` which allows you to customize `chatEventHandler` associated with the query. [#3992](https://github.com/GetStream/stream-chat-android/pull/3992)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed a crash when passing content URIs without duration metadata to the `StorageHelper::.getAttachmentsFromUriList` method. [4002](https://github.com/GetStream/stream-chat-android/pull/4002)
- Image attachment containers now posses the correct fixed size prior to loading, avoiding message items around messages containing images from "jumping". This is applicable only to image attachments which contain non-null values`Attachment.originalWidth` and `Attachment.originalHeight` properties. [#4011](https://github.com/GetStream/stream-chat-android/pull/4011)
- Fixed a bug when a reaction left by the current user appears as a reaction left by some other user. [4035#](https://github.com/GetStream/stream-chat-android/pull/4035)

### ✅ Added
- Add `streamUiAttachmentsPickerMediaAttachmentsTabEnabled`, `streamUiAttachmentsPickerFileAttachmentsTabEnabled` and `streamUiAttachmentsPickerCameraAttachmentsTabEnabled` attributes to `MessageComposerView` that allow to show/hide particular tabs in the attachment picker. [#3977](https://github.com/GetStream/stream-chat-android/pull/3977)
- Add `streamUiMediaAttachmentsTabEnabled`, `streamUiFileAttachmentsTabEnabled` and `streamUiCameraAttachmentsTabEnabled` attributes to `MessageInputView` that allow to show/hide particular tabs in the attachment picker.. [#3977](https://github.com/GetStream/stream-chat-android/pull/3977)
- Add the `attachmentsPickerTabFactories` parameter to `AttachmentSelectionDialogFragment` that allows to create a custom tab for the attachment picker. [#3977](https://github.com/GetStream/stream-chat-android/pull/3977)

### ⚠️ Changed
- Link attachment previews now feature a more compact image preview container. [#4011](https://github.com/GetStream/stream-chat-android/pull/4011)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a crash when passing content URIs without duration metadata to the `StorageHelperWrapper::.getAttachmentsFromUris` method. [4002](https://github.com/GetStream/stream-chat-android/pull/4002)
- Fixed a bug when a reaction left by the current user appears as a reaction left by some other user. [4035#](https://github.com/GetStream/stream-chat-android/pull/4035)

### ⬆️ Improved
- `ImageAttachmentContent` is no longer statically sized. It now auto-sizes itself according to the image attachment dimension ratio. If you wish to limit the maximum height of image attachments, please use `StreamDimens.attachmentsContentImageMaxHeight`.  [#4013](https://github.com/GetStream/stream-chat-android/pull/4013)

### ✅ Added
- Added additional `chatEventHandlerFactory` parameter to `ChannelListViewModel` and `ChannelListViewModelFactory` that allows customizing `ChatEventHandler`. [#3997](https://github.com/GetStream/stream-chat-android/pull/3997)
- Added the `tabFactories` parameter to `AttachmentsPicker` that allows to control the list of tabs displayed in the picker. [#3994](https://github.com/GetStream/stream-chat-android/pull/3994)
- Added parameter `attachmentsContentImageMaxHeight` to `StreamDimens`. [#4013](https://github.com/GetStream/stream-chat-android/pull/4013)

### ⚠️ Changed
- `StreamDimens` constructor containing parameter `attachmentsContentImageHeight` has been deprecated. Please use the one without it. This has been done because images displayed by `ImageAttachmentContent` inside the message list now auto-size themselves intelligently according to their aspect ratio. If you wish to limit the maximum vertical height of such images, use `StreamDimens.attachmentsContentImageMaxHeight`.  [#4013](https://github.com/GetStream/stream-chat-android/pull/4013)

# August 02th, 2022 - 5.7.0
## Common changes for all artifacts
### ⚠️ Changed
- Updated compile & target SDK to **32**. [#3965](https://github.com/GetStream/stream-chat-android/pull/3965)
- Updated Kotlin version to **1.7.0**.[#3965](https://github.com/GetStream/stream-chat-android/pull/3965)

## stream-chat-android-client
### 🐞 Fixed
- Fixed the missing disconnected state in `ClientState.connectionState`. [#3943](https://github.com/GetStream/stream-chat-android/pull/3943)

### ⬆️ Improved
- Offline data is clear after the user is disconnect by calling `ChatClient.disconnect(true)`. [#3917](https://github.com/GetStream/stream-chat-android/pull/3917)
- Adding logs to understand more about unrecoverable errors in socket connection. [#3946](https://github.com/GetStream/stream-chat-android/pull/3946)
- Added the ClientState.initializationState. Now you can check when the current state of the initialization progress. [#3962](https://github.com/GetStream/stream-chat-android/pull/3962)

### ✅ Added
- Added a check if `lastSyncedAt` is no later than 30 days when calling `ChatClient::getSyncHistory`. [#3934](https://github.com/GetStream/stream-chat-android/pull/3934)
- Added `ClientState::isNetworkAvailable` which gives you information about device's internet connection status.[#3880](https://github.com/GetStream/stream-chat-android/pull/3880)

### ⚠️ Changed
- Queries that require active socket connection will be postponed until connection is established: [#3952](https://github.com/GetStream/stream-chat-android/pull/3952)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed preview for channels when sending messages offline. [3933](https://github.com/GetStream/stream-chat-android/pull/3933)
- Fixed marking the channel as read when opening it from a push notification. Previously the SDK would fail to make the call. [#3985](https://github.com/GetStream/stream-chat-android/pull/3985)

## stream-chat-android-ui-common
### ✅ Added
- Added more file sources to the file provider used when sending file attachments. [3958](https://github.com/GetStream/stream-chat-android/pull/3958)

### ⚠️ Changed
- Deprecated `MessageAction.MuteUser`. The option to mute users via a message options has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the display of disconnected state in channel list and message list headers. [#3943](https://github.com/GetStream/stream-chat-android/pull/3943)
- Fixed list state race condition while switching filters in channel list. [#3939](https://github.com/GetStream/stream-chat-android/pull/3939/files)

### ✅ Added
- Added `android:inputType` customization option to `MessageComposerView` and `MessageInputView`. [#3942](https://github.com/GetStream/stream-chat-android/pull/3924)
- Added `streamUiOptionsOverlayEditReactionsMargin`, `streamUiOptionsOverlayUserReactionsMargin` and `streamUiOptionsOverlayMessageOptionsMargin` attributes to `MessageInputView` to customize the spacing between the elements on the message options overlay. [#3950](https://github.com/GetStream/stream-chat-android/pull/3950)
- Added `MessageListViewModel.Event.BanUser`. This event is used to ban a user by using calling `MessageListViewModel.onEvent(Event)` and providing it as an argument. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel.Event.UnbanUser`. This event is used to unban a user by using calling `MessageListViewModel.onEvent(Event)` and providing it as an argument. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel.Event.ShadowBanUser`. This event is used to shadow ban a user by using calling `MessageListViewModel.onEvent(Event)` and providing it as an argument. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel.Event.RemoveShadowBanFromUser`. This event is used to remove a shadow ban from a user by using calling `MessageListViewModel.onEvent(Event)` and providing it as an argument. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)

### ⚠️ Changed
- Deprecated `LegacyDateFormatter`, `PorterImageView` and `PorterShapeImageView` classes as they are unused. [3923](https://github.com/GetStream/stream-chat-android/pull/3923)
- Deprecated `DefaultTypingUpdatesBuffer`. Should you wish to create your own implementation of a typing buffer, you can create a custom implementation of `TypingUpdatesBuffer`. [#3968](https://github.com/GetStream/stream-chat-android/pull/3968)
- Deprecated `MessageListViewModel.BlockUser`. Use `MessageListViewModel.ShadowBanUser` if you want to retain the same functionality, or `MessageListViewModel.BanUser` if you want to outright ban the user. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView::setUserMuteHandler`. The option to mute users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView::setUserUnmuteHandler`. The option to unmute users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView::setMuteUserEnabled`. The option to mute users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView.UserMuteHandler`. The option to mute users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView.UserUnmuteHandler`. The option to unmute users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView::setBlockUserEnabled`. The option to block users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated `MessageListView.UserBlockHandler`. The option to block users via message option has been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated the following `MessageListViewAttributes`: `streamUiMuteOptionIcon`, `streamUiUnmuteOptionIcon`, `streamUiMuteUserEnabled`, `streamUiBlockOptionIcon` and `streamUiBlockUserEnabled`. The options to block and mute user using `MessageListView` message options have been deprecated and will be removed. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Deprecated the `MessageListViewStyle` constructor containing params `muteIcon`, `unmuteIcon`, `muteEnabled`, `blockIcon` and `blockEnabled`. Use the constructor which does not contain these parameters. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the display of disconnected state in channel list and message list headers. [#3943](https://github.com/GetStream/stream-chat-android/pull/3943)

### ✅ Added
- Added `KeyboardOptions` customization option to `MessageInput` composable. [#3942](https://github.com/GetStream/stream-chat-android/pull/3924)
- Added `MessageListViewModel::banUser`. You can use it to ban a user belonging to the current channel. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel::unbanUser`. You can use it to unban a user belonging to the current channel. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel::shadowBanUser`. You can use it to shadow ban a user belonging to the current channel. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel::removeShadowBanFromUser`. You can use it to remove a shadow ban from a user belonging to the current channel. For the difference between banning and shadow banning, you can read the documentation [here](https://getstream.io/blog/feature-announcement-shadow-ban/). [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel::muteUser`. You can use it to mute a user belonging to the current channel. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)
- Added `MessageListViewModel::unmuteUser`. You can use it to mute a user belonging to the current channel. [#3953](https://github.com/GetStream/stream-chat-android/pull/3953)

### ⚠️ Changed
- Deprecated `RowScope.DefaultComposerInputContent` to be marked internal. Use `MessageInput` directly instead. [#3942](https://github.com/GetStream/stream-chat-android/pull/3924)
- Updated Compose compiler and UI version to **1.2.0**.[#3965](https://github.com/GetStream/stream-chat-android/pull/3965)

# July 20th, 2022 - 5.6.1
## stream-chat-android-client
### ⚠️ Changed
- Functions inside `ThreadQueryListener` have been turned into `suspend` functions. [#3926](https://github.com/GetStream/stream-chat-android/pull/3926)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed a crash when reacting to a message in a thread. [#3926](https://github.com/GetStream/stream-chat-android/pull/3926)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed thread not scrolling to new message. [#3930](https://github.com/GetStream/stream-chat-android/pull/3930)

# July 20th, 2022 - 5.6.0
## Common changes for all artifacts
### ⚠️ Changed
- 🚨 Breaking change: The class `io.getstream.chat.android.offline.model.connection.ConnectionState` was moved to `io.getstream.chat.android.client.models.ConnectionState`. Please update your imports to be able to compile code using this class. [#3852](https://github.com/GetStream/stream-chat-android/pull/3852).

## stream-chat-android-client
### ✅ Added
- Added a way to convert `Flow` into `LiveData` for Java users.
- Base state of the SDK can be check using `io.getstream.chat.android.client.setup.state.ClientState` interface. Use this interface to receive the state of the SDK as StateFlows. [#3852](https://github.com/GetStream/stream-chat-android/pull/3852)

### ⚠️ Changed
-. `Call` interface provides an `await()` suspend function implemented on every subclass and is not needed to use the extension function anymore. [#3807](https://github.com/GetStream/stream-chat-android/pull/3807)
- `ChatLoggerHandler` has a new function named `logV`. [#3869](https://github.com/GetStream/stream-chat-android/pull/3869)
- `ChatClient.disconnect()` is deprecated and a new `ChatClient.disconnect(Boolean)` method with a boolean argument is created. This method return a `Call` to be invoked for disconnection. [#3817](https://github.com/GetStream/stream-chat-android/pull/3817)

### ❌ Removed
- 🚨 Breaking change: Removed the `Member.role` field. [3851](https://github.com/GetStream/stream-chat-android/pull/3851)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed initializing channels state when DB is empty and API requests fails. [3870](https://github.com/GetStream/stream-chat-android/pull/3870)
- Fixed that causes a crash while reconnecting to the SDK multiple times. [#3888](https://github.com/GetStream/stream-chat-android/pull/3888)

## stream-chat-android-ui-components
### 🐞 Fixed
- The reply option on the gallery screen and moderation options now work with `MessageComposerView`. [#3864](https://github.com/GetStream/stream-chat-android/pull/3864)
- Fixed potential crashes when showing dialogs after process recreation. [#3857](https://github.com/GetStream/stream-chat-android/pull/3857)
- Fixed potential unnecessary channel query on the channel list screen. [#3895](https://github.com/GetStream/stream-chat-android/pull/3895)

### ⬆️ Improved
- `MessageListView` now allows multiple re-bindings of `MessageListViewModel` provided that `MessageListViewModel.deletedMessageVisibility` has not been changed since `MessageListView` was first initialized. [#3843](https://github.com/GetStream/stream-chat-android/pull/3843)

### ✅ Added
- Added the ability to align system messages via a `MessageListView` attribute called `streamUiSystemMessageAlignment`. [#3840](https://github.com/GetStream/stream-chat-android/pull/3840)
- Added the `MessageListViewModel::setMessagePositionHandler` method to customize message position within a group. [#3882](https://github.com/GetStream/stream-chat-android/pull/3882)
- Added documentation for [MessageComposerView](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer). [#3845](https://github.com/GetStream/stream-chat-android/pull/3845)
- Added a new version of the [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-composer/) guide that uses the new `MessageComposerView`. [#3877](https://github.com/GetStream/stream-chat-android/pull/3877)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed potential unnecessary channel query on the channel list screen. [#3895](https://github.com/GetStream/stream-chat-android/pull/3895)

### ⬆️ Improved
- Improved recomposition in `MessagesScreen` by deferring state reads to the latest possible point. [#3667](https://github.com/GetStream/stream-chat-android/pull/3667)

### ⚠️ Changed
- Show snackbar instead of toast when file exceeds the size limit. [#3858](https://github.com/GetStream/stream-chat-android/pull/3858)

# July 05th, 2022 - 5.5.0
## Common changes for all artifacts
### 🐞 Fixed
- Add ordered substitution arguments in `ja` and `ko` translated strings files [#3778](https://github.com/GetStream/stream-chat-android/pull/3778)

## stream-chat-android-client
### 🐞 Fixed
- Fix the channel screen being stuck with an infinite loading [3791](https://github.com/GetStream/stream-chat-android/pull/3791)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug of empty channels while sending messages. [3776](https://github.com/GetStream/stream-chat-android/pull/3776)
- Fixed populating mentions when sending a message with attachments. [3801](https://github.com/GetStream/stream-chat-android/pull/3801)
- Fixed crash at ExtraDataConverter.stringToMap. [3816](https://github.com/GetStream/stream-chat-android/pull/3816)

### ⚠️ Changed
- Deprecated `GlobalState::typingUpdates` in favor of `GlobalState::typingChannels`.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed reply messages inside notification. [#3756](https://github.com/GetStream/stream-chat-android/pull/3756)
- Fixed the display of avatars before system messages. [#3799](https://github.com/GetStream/stream-chat-android/pull/3799)
- Fixed a bug which made the unread count disappear on certain devices when it went over double digits. [#3798](https://github.com/GetStream/stream-chat-android/pull/3798)
- Fixed a bug where typing items in MessageList weren't properly set on all data changes. [#3790](https://github.com/GetStream/stream-chat-android/pull/3790)

### ⬆️ Improved
- `ChannelListView` can now restore the previously saved scroll state. [3804](https://github.com/GetStream/stream-chat-android/pull/3804)

### ✅ Added
- Added `MessagePreviewFormatter` field to the `ChatUI` class, to allow for message preview text format customization across the app. [3788](https://github.com/GetStream/stream-chat-android/pull/3788).
- Added `streamUiDisableScrollWhenShowingDialog` attribute to `MessageListView` that allows to enable/disable message list scroll while a dialog is shown over the message list. [#3809](https://github.com/GetStream/stream-chat-android/pull/3809)
- Added the preview of moderation bounced messages and the ability to take actions upon those messages like edit, delete and send anyway. [#3625](https://github.com/GetStream/stream-chat-android/pull/3625)
- Added experimental implementation of `MessageComposerView` and `MessageComposerViewModel` which are supposed to replace `MessageInputView` in the future. [3019](https://github.com/GetStream/stream-chat-android/pull/3019)
- Added `MessageListView::setMessageOptionItemsFactory` and `MessageListView::setCustomActionHandler` methods to add and handle custom actions in `MessageListView`. [3768](https://github.com/GetStream/stream-chat-android/pull/3768)

### ⚠️ Changed
- The layout width of the unread count view is now set to `wrap_content` instead of being a fixed size dictated by the dimen `stream_ui_scroll_button_unread_badge_size`. [#3798](https://github.com/GetStream/stream-chat-android/pull/3798)

### ❌ Removed
- 🚨 Breaking change: The block action has been removed from message options. [3768](https://github.com/GetStream/stream-chat-android/pull/3768)

## stream-chat-android-compose
### 🐞 Fixed
- Channels will now be marked as read only when the latest message is reached. Previously they were marked read whenever an unread message was read, regardless of its position in the list. [#3772](https://github.com/GetStream/stream-chat-android/pull/3772)

### ⬆️ Improved
- Improved `Messages` recomposition when marking messages as read. It will now avoid going into a recomposition loop in certain situations such as when you have two or more failed messages visible in the list. [#3772](https://github.com/GetStream/stream-chat-android/pull/3772)
- Covered an edge case inside `DefaultTypingUpdatesBuffer`. It will now always call `onTypingStopped()` when you call `DefaultTypingUpdatesBuffer.clear()`. [#3782](https://github.com/GetStream/stream-chat-android/pull/3782)

### ✅ Added
- Added the preview of moderation bounced messages and the ability to take actions upon those messages like edit, delete and send anyway. [#3625](https://github.com/GetStream/stream-chat-android/pull/3625)

# June 27th, 2022 - 5.4.0
## Common changes for all artifacts
### ⬆️ Improved
- Now the SDK can be used if R8 full mode. New rules were added to the library to support the aggressive optimizations [3663](https://github.com/GetStream/stream-chat-android/pull/3663).

### ⚠️ Changed
- Migrated to Coil version 2.1.0 [#3538](https://github.com/GetStream/stream-chat-android/pull/3538)

## stream-chat-android-client
### ⬆️ Improved
-  Avoid multiple calls to `/app` endpoint. [3686](https://github.com/GetStream/stream-chat-android/pull/3686)

### ✅ Added
-. `ChatClient::connectUser` as a new optional argument to configure a timeout to be waiting until the connection is established, in another case an error will be returned [#3605](https://github.com/GetStream/stream-chat-android/pull/3605)
-. `ChatClient::connectAnonymousUser` as a new optional argument to configure a timeout to be waiting until the connection is established, in another case an error will be returned [#3605](https://github.com/GetStream/stream-chat-android/pull/3605)
-. `ChatClient::connectGuestUser` as a new optional argument to configure a timeout to be waiting until the connection is established, in another case an error will be returned [#3605](https://github.com/GetStream/stream-chat-android/pull/3605)
-. `ChatClient::connectUser` doesn't return an error in the case there is a previous connection with the same user. [#3653](https://github.com/GetStream/stream-chat-android/pull/3653)
- Added `ChatClient::countUnreadMentions` extension function which counts messages in which the user is mentioned.

### ⚠️ Changed
- 🚨 Changed `ChatClient::connectUser` - the method shouldn't be called when the user is already set and will automatically disconnect if this happens.

## stream-chat-android-offline
### 🐞 Fixed
- Fix the stale Channel data being stored into database. [3650](https://github.com/GetStream/stream-chat-android/pull/3650)
- Fix race condition problem that allowed multiple threads to increment unread count, which could cause a mistake in the number of unread messages. [3656](https://github.com/GetStream/stream-chat-android/pull/3656)
- A new optional argument `useSequentialEventHandler` has been added to `Config` class of offline plugin to enable a sequential event handling mechanism. [3659](https://github.com/GetStream/stream-chat-android/pull/3659)
- Fix channel mutes being dropped on user updates [3728](https://github.com/GetStream/stream-chat-android/pull/3728)
- Bug fix when deleting reactions without internet connection. [#3753](https://github.com/GetStream/stream-chat-android/pull/3753)

### ⬆️ Improved
- Added logs of all properties available in a class and which one was searched for then QuerySort fails to find a field. [3597](https://github.com/GetStream/stream-chat-android/pull/3597)

### ✅ Added
- Added `EventHandlerSequential` to support a sequential event processing. [3604](https://github.com/GetStream/stream-chat-android/pull/3604)
- Logging when unread count is updated. [3642](https://github.com/GetStream/stream-chat-android/pull/3642)

### ⚠️ Changed
-  Added interface `QuerySorter` and new implementation of query sort `QuerySortByField` so users can choose between implementations that use reflection or not. [3624](https://github.com/GetStream/stream-chat-android/pull/3624)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed potential NPE when disconnecting the user. [#3612](https://github.com/GetStream/stream-chat-android/pull/3612)
- The channel will now be marked as read once the latest message inside `MessagesListView` is reached. Previously scrolling down to it would not trigger this action. [#3620](https://github.com/GetStream/stream-chat-android/pull/3620)
- Now the options button is not displayed on the gallery screen if there are no options available. [#3696](https://github.com/GetStream/stream-chat-android/pull/3696)
- Fixed `app:streamUiMessageInputHintText` not getting applied properly in `MessageInputView`. [#3749](https://github.com/GetStream/stream-chat-android/pull/3749)
- Fixed backwards compatibility of the `ChannelListView` attribute `streamUiIndicatorPendingSyncIcon` and the `MessageListView` attribute `streamUiIconIndicatorPendingSync`. These are now backwards compatible down to API 21 [#3766](https://github.com/GetStream/stream-chat-android/pull/3766)

### ⬆️ Improved
- Improved displaying the upload progress of files being uploaded. Now the upload progress text is less likely to get ellipsized. [#3618](https://github.com/GetStream/stream-chat-android/pull/3618)

### ✅ Added
- Added way to customize quoted attachments through `QuotedAttachmentFactory` and updated custom attachments guide for the new feature. [#3592](https://github.com/GetStream/stream-chat-android/pull/3592)
- Added `ChannelListViewModelFactory.Builder` for Java users. [#3617](https://github.com/GetStream/stream-chat-android/pull/3617)
- Added `MessageListViewModelFactory.Builder` for Java users. [#3617](https://github.com/GetStream/stream-chat-android/pull/3617)
- Added `PinnedMessageListViewModelFactory.Builder` for Java users. [#3617](https://github.com/GetStream/stream-chat-android/pull/3617)
- Added `TypingIndicatorViewModelFactory.Builder` for Java users. [#3617](https://github.com/GetStream/stream-chat-android/pull/3617)
- Added new attributes to `MessageListView` that are designed to customize the scroll to bottom button. They are listed in the linked PR. [3634](https://github.com/GetStream/stream-chat-android/pull/3634)
- Added a way to change runtime filters for Channels in `ChannelListViewModel`, using `setFilters(FilterObject)`. [#3687](https://github.com/GetStream/stream-chat-android/pull/3687) 
- Added support for bottom infinite scrolling when searching for messages or navigating to messages in a non-linear way inside MessageListView. [3654](https://github.com/GetStream/stream-chat-android/pull/3654)
- A new interface `TypingUpdatesBuffer` and its implementation `DefaultTypingUpdatesBuffer` used for buffering typing updates in order to save API calls. [3633](https://github.com/GetStream/stream-chat-android/pull/3633)
- A new method `MessageInputView.setTypingUpdatesBuffer(TypingUpdatesBuffer)` used for setting the typing updates buffer. [3633](https://github.com/GetStream/stream-chat-android/pull/3633)
- Added possibility to customize gallery options style via `TransformStyle.attachmentGalleryOptionsStyleTransformer`. [3696](https://github.com/GetStream/stream-chat-android/pull/3696)

### ⚠️ Changed
- Dimens `stream_ui_spacing_small` no longer has an effect on the internal margins of `ScrollButtonView`, instead use the `MessageListView` attribute `streamUIScrollButtonInternalMargin` to set internal margins. [3634](https://github.com/GetStream/stream-chat-android/pull/3634)
- The default elevation of the unread count badge inside `ScrollButtonView` was changed from `10dp` to `3dp`. [3634](https://github.com/GetStream/stream-chat-android/pull/3634)
- Deprecated `MessageInputView.TypingListener` in favor of `TypingUpdatesBuffer` and `MessageInputView.setTypingListener(TypingListener)` in favor of `MessageInputView.setTypingUpdatesBuffer(TypingUpdatesBuffer)`. [3633](https://github.com/GetStream/stream-chat-android/pull/3633)
- Added `WRITE_EXTERNAL_STORAGE` permission check on the default implementation of the download handler when using `MessageListViewModel.bindView`. [#3719](https://github.com/GetStream/stream-chat-android/pull/3719)
- Removed the default filter from `ChannelListFragment` so that it can rely on the default filter from `ChannelListViewModel`. [3762](https://github.com/GetStream/stream-chat-android/pull/3762)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the display of `ChannelAvatar` for a channel with two members and neither of them is the current user. [3598](https://github.com/GetStream/stream-chat-android/pull/3598)

### ⬆️ Improved
- Improved padding customization options of `InputField`. [#3596](https://github.com/GetStream/stream-chat-android/pull/3596)

### ✅ Added
- Added `Modifier` as an argument to `FileUploadItem` and `FileAttachmentItem`. [#3603](https://github.com/GetStream/stream-chat-android/pull/3603)
- Added option to customise `InitialsAvatar` offset passing it custom offset and through `groupAvatarInitialsXOffset` and `groupAvatarInitialsYOffset` dimens. [#3609](https://github.com/GetStream/stream-chat-android/pull/3609)
- A new interface `TypingUpdatesBuffer` and its implementation `DefaultTypingUpdatesBuffer` used for buffering typing updates in order to save API calls. [3633](https://github.com/GetStream/stream-chat-android/pull/3633)
- A new method `MessageComposerViewModel.setTypingUpdatesBuffer(TypingUpdatesBuffer)` used for setting the typing updates buffer. [3633](https://github.com/GetStream/stream-chat-android/pull/3633)
- Added `PermissionHandler` and `DownloadPermissionHandler` to automatically request storage permission if needed and download the attachments. [#3676](https://github.com/GetStream/stream-chat-android/pull/3676)

### ⚠️ Changed
- Since Coil 2.0, the `LocalImageLoader` has been deprecated. So now we support our own image loader, `StreamImageLoader` for providing composition local. [#3538](https://github.com/GetStream/stream-chat-android/pull/3538)
- Changed how the emoji only message size and how they are laid out depending on emoji count. [#3665](https://github.com/GetStream/stream-chat-android/pull/3665)

### ❌ Removed
- Removed the default gray background from `LoadingIndicator`. [#3599](https://github.com/GetStream/stream-chat-android/pull/3599)

## stream-chat-android-pushprovider-xiaomi
### 🐞 Fixed
- Fix crash when used on Android API 31+ [#3678](https://github.com/GetStream/stream-chat-android/pull/3678)

### ✅ Added
- Upgrade MiPush SDK to version 5.0.6 [#3678](https://github.com/GetStream/stream-chat-android/pull/3678)

# Jun 1st, 2022 - 5.3.1
## stream-chat-android-client
### 🐞 Fixed
- Added getters to members search in `QuerySort` as some compilers may generate getters and setter instead of public properties,
 making our current search for property to fail. [#3608](https://github.com/GetStream/stream-chat-android/pull/3608)

# May 25th, 2022 - 5.3.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed `ChatParser` failing to parse errors because it was trying to fetch the raw response from a converted body. [#3534](https://github.com/GetStream/stream-chat-android/pull/3534)

### ⬆️ Improved
- CurrentUser is not initialized when a PN is received. [#3520](https://github.com/GetStream/stream-chat-android/pull/3520)

### ✅ Added
- 🚨 Breaking change: Added `DistinctChatApi` to prevent multiple query requests being fired. [#3521](https://github.com/GetStream/stream-chat-android/pull/3521)
- 🚨 Breaking change: Added new property `ChatClientConfig.disableDistinctApiCalls` to disable `DistinctChatApi`, which is enabled by default. 

### ⚠️ Changed
- 🚨 Breaking change: `Plugin`, `PluginFactory` and plugin side-effect listeners (`CreatChannelListener`, `SendMessageListener` etc.) are moved out of `experimental` package. [#3583](https://github.com/GetStream/stream-chat-android/pull/3583/)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed process sync offline message when a push is received. [#3518](https://github.com/GetStream/stream-chat-android/pull/3518)
- Fixed syncing the channel after bringing the app from background. [#3548](https://github.com/GetStream/stream-chat-android/pull/3548)
- Fixed initializing `OfflinePlugin` when connecting anonymous user. It fixes the issue when after connecting headers stay in `Disconnected` state. [#3553](https://github.com/GetStream/stream-chat-android/pull/3553)

### ⬆️ Improved
- Change the order of offline message so it matches the order of online messages. Now the reshuffling of messages when switching from offline to online doesn't happen anymore. [3524](https://github.com/GetStream/stream-chat-android/pull/3524)
- 🚨 Breaking change: `QueryChannelsState::channels` can now return a null as an initial value. 
- Adding logs for QuerySort: [3570](https://github.com/GetStream/stream-chat-android/pull/3570)
- Adding logs for plugin usage, state calls usage and ChannelListView. [3572](https://github.com/GetStream/stream-chat-android/pull/3572)

### ✅ Added
- Added `EventHandlingResult.WatchAndAdd` to results returned from `ChatEventHandler`.
- Added handling `ChannelVisibleEvent`. Default `ChatEventHandler` will return `EventHandlingResult.WatchAndAdd`.

## stream-chat-android-ui-common
### ✅ Added
- Added `MessageOptionsUserReactionAlignemnt` used to define the user reaction alignment inside message options. [#3541](https://github.com/GetStream/stream-chat-android/pull/3541)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the way pagination scrolling worked for various non-core components (e.g. search, gallery/media/pinned message lists) [#3507](https://github.com/GetStream/stream-chat-android/pull/3507)
- Added loading more indicator to PinnedMessageListView [#3507](https://github.com/GetStream/stream-chat-android/pull/3507)
- Fix video scaling issue on the media preview screen. [#3560](https://github.com/GetStream/stream-chat-android/pull/3560)
- Fixed refreshing `ChannelListView` after unhiding the channel. [#3569](https://github.com/GetStream/stream-chat-android/pull/3569)

### ✅ Added
- Added the public method `switchToCommandMode(command: Command)` inside `MessageInputView`. This method allows switching the input to command mode using the desired command directly, instead of having to select it from the dialog. An example of its usage is provided inside the patch within the linked PR. [#3515](https://github.com/GetStream/stream-chat-android/pull/3515)
- Added loading indicator to the media preview screen. [#3549](https://github.com/GetStream/stream-chat-android/pull/3549)
- Added `streamUiMediaActivityProgressBarStyle` theme attribute to customize the appearance of loading indicator on the media preview screen. [#3549](https://github.com/GetStream/stream-chat-android/pull/3549)
- Added the ability to customize user reaction alignment and orientation inside message options through `ViewReactionsViewStyle` or `SingleReactionViewStyle`. [#3541](https://github.com/GetStream/stream-chat-android/pull/3541)
- Added `horizontalPadding` customization options to `ViewReactionsViewStyle` and `EditReactionsViewStyle`. [#3541](https://github.com/GetStream/stream-chat-android/pull/3541)

### ⚠️ Changed
- Deprecated `Member.isOwnerOrAdmin` and `List<Member>?.isCurrentUserOwnerOrAdmin()`. Use `Channel::ownCapabilities` instead. [#3576](https://github.com/GetStream/stream-chat-android/pull/3576)
- Changed how padding is applied to `ViewReactionsView`. [#3541](https://github.com/GetStream/stream-chat-android/pull/3541)

## stream-chat-android-compose
### 🐞 Fixed
- Fix video scaling issue on the media preview screen. [#3560](https://github.com/GetStream/stream-chat-android/pull/3560)
- Fixed refreshing `ChannelListView` after unhiding the channel. [#3569](https://github.com/GetStream/stream-chat-android/pull/3569)

### ✅ Added
- Added scroll to quoted message on click. [#3472](https://github.com/GetStream/stream-chat-android/pull/3472)
- Added guides for `QuotedAttachmentFactory`. [You can read about it here](https://getstream.io/chat/docs/sdk/android/compose/guides/adding-custom-attachments/#quoted-messages)
- Added loading indicator to the media preview screen. [#3549](https://github.com/GetStream/stream-chat-android/pull/3549)
- Added the ability to customize user reaction alignment inside message options through `ChatTheme`. [#3541](https://github.com/GetStream/stream-chat-android/pull/3541)

### ⚠️ Changed
- Changed `QuotedMessage` design by adding `QuotedAttachmentFactory`, `ImageAttachmentQuotedContent` and `FileAttachmentQuotedContent`. [#3472](https://github.com/GetStream/stream-chat-android/pull/3472)

# May 11th, 2022 - 5.2.0
## stream-chat-android-client
### ✅ Added
- Added `Channel.membership` property. [#3367](https://github.com/GetStream/stream-chat-android/pull/3367)
- Added `ChannelData.membership` property. [#3367](https://github.com/GetStream/stream-chat-android/pull/3367)
- Added `NotificationAddedToChannelEvent.member` property. [#3367](https://github.com/GetStream/stream-chat-android/pull/3367)
- Add `provideName` property to `Device` entity to support Multi-Bundle [#3396](https://github.com/GetStream/stream-chat-android/pull/3396)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed sorting channels by `Channel::lastMessageAt` when the channel contains not synced messages. [#3470](https://github.com/GetStream/stream-chat-android/pull/3470)
- Fixed bug that made impossible to retry attachments that were not fully sent. [3485](https://github.com/GetStream/stream-chat-android/pull/3485)
- Fixed refreshing channels list when syncing the channel. [#3492](https://github.com/GetStream/stream-chat-android/pull/3492)
- Fixed deleting reactions while offline. [3486](https://github.com/GetStream/stream-chat-android/pull/3486)

## stream-chat-android-ui-common
### ⬆️ Improved
- Updated the attachment upload size limit to 100MB from 20MB. [#3490](https://github.com/GetStream/stream-chat-android/pull/3490)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed Xiaomi crash when long clicking on links inside messages. [#3491](https://github.com/GetStream/stream-chat-android/pull/3491)

## stream-chat-android-compose
### ⬆️ Improved
- Improved the behavior of `DeletedMessageVisibility` and `MessageFooterVisibility` when used in pair. Now the `DeletedMessageVisibility` and its "only visible to you" mode respects the `MessageFooterVisibility` and vice-versa. [#3467](https://github.com/GetStream/stream-chat-android/pull/3467)

## stream-chat-android-pushprovider-firebase
### ✅ Added
- Support Multi-Bundle [#3396](https://github.com/GetStream/stream-chat-android/pull/3396)

### ⚠️ Changed
- Upgrade Firebase Messaging dependency to version `23.0.4`. [#3484](https://github.com/GetStream/stream-chat-android/pull/3484)

## stream-chat-android-pushprovider-huawei
### ✅ Added
- Support Multi-Bundle [#3396](https://github.com/GetStream/stream-chat-android/pull/3396)

## stream-chat-android-pushprovider-xiaomi
### ✅ Added
- Support Multi-Bundle [#3396](https://github.com/GetStream/stream-chat-android/pull/3396)

# May 3rd, 2022 - 5.1.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed ANR happening on a token request. [#3342](https://github.com/GetStream/stream-chat-android/pull/3342)
- Fixed overriding User's `image` and `name` properties with empty values when connecting the user. [#3430](https://github.com/GetStream/stream-chat-android/pull/3430)
- Fixed serialization problem when flagging message. [#3437](https://github.com/GetStream/stream-chat-android/pull/3437)

### ✅ Added
- Added `ChannelRepository.selectChannelByCid` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ChannelRepository.selectChannelsByCids` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ChannelRepository.selectChannelCidsSyncNeeded` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `MessageRepository.selectMessageIdsBySyncState` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ReactionRepository.selectReactionById` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ReactionRepository.selectReactionsByIds` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ReactionRepository.selectReactionIdsBySyncStatus` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `ChatLogger.logV` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)
- Added `TaggedLogger.logV` method. [#3434](https://github.com/GetStream/stream-chat-android/pull/3434)

### ⚠️ Changed
- Changed visibility of the `retry` extension to internal. [#3353](https://github.com/GetStream/stream-chat-android/pull/3353)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed a crash when attachment upload is in progress or about to start and user is disconnected at the same moment. [#3377](https://github.com/GetStream/stream-chat-android/pull/3377)
- Fixed updating `Channel::ownCapabilities` after receiving events. [#3420](https://github.com/GetStream/stream-chat-android/pull/3420)
- Fixed reaction sync issue because `SyncState::lastSyncedAt` was never updated. [#3421](https://github.com/GetStream/stream-chat-android/pull/3421)

### ⬆️ Improved
- Adding the possibility to change the repositories of `OfflinePlugin`. You can change `RepositoryFactory` in `OfflinePlugin` and use custom implementations of repositories.

## stream-chat-android-ui-common
### ⚠️ Changed
- Deprecated `DeletedMessageListItemPredicate` in favor of `DeletedMessageVisibility`. This is a followup on [#3272](https://github.com/GetStream/stream-chat-android/pull/3272/files) which deprecated filtering messages inside `MessageListView` in favor of filtering messages inside `MessageListViewModel`. [#3409](https://github.com/GetStream/stream-chat-android/pull/3409)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed a bug where command suggestion popup was displayed even though all the commands were disabled. [#3334](https://github.com/GetStream/stream-chat-android/pull/3334)
- Fixed a bug on Nougat where the reaction colors were not displayed properly. [#3347](https://github.com/GetStream/stream-chat-android/pull/3347)
- Fixed a bug where custom `MessageListItemViewHolderFactory` was ignore on the message options overlay. [#3343](https://github.com/GetStream/stream-chat-android/pull/3343)
- Fixed `MessageListViewModel` initialization when channel's data is not available immediately, for example when the view model is created after connecting the user. [#3379](https://github.com/GetStream/stream-chat-android/pull/3379)
- Fixed configuration for flag message confirmation dialog. [3411](https://github.com/GetStream/stream-chat-android/pull/3411)
- Fixed a potential crash with conflicting font names. [#3445](https://github.com/GetStream/stream-chat-android/pull/3445)

### ⬆️ Improved
- Added a way to customize reactions behavior to allow multiple reactions. [#3341](https://github.com/GetStream/stream-chat-android/pull/3341)
- Added a way to customize `messageInputField` padding inside `MessageInputFiledView`. [#3392](https://github.com/GetStream/stream-chat-android/pull/3392)
- Added a way to change the `MessageListHeaderView` separator color. [#3395](https://github.com/GetStream/stream-chat-android/pull/3395)
- Added a way to change the `ChannelListHeaderView` separator color. [#3395](https://github.com/GetStream/stream-chat-android/pull/3395)
- Now single membered channels display the name of member instead of "Channel without name" [3423](https://github.com/GetStream/stream-chat-android/pull/3423)
- Channels with only one member now show the member's image in avatar. [3425](https://github.com/GetStream/stream-chat-android/pull/3425)
- Added a way to change the `attachmentsButton` and `commandsButton` ripple color inside `MessageInputView`. [#3412](https://github.com/GetStream/stream-chat-android/pull/3412)

### ✅ Added
- Added support for own capabilities. You can read more about own capabilities [here](https://getstream.io/chat/docs/sdk/android/ui/guides/implementing-own-capabilities/). [#3389](https://github.com/GetStream/stream-chat-android/pull/3389)
- Added the possibility to customize the message footer visibility through `MessageFooterVisibility` inside `MessageListViewModel`. [#3343](https://github.com/GetStream/stream-chat-android/pull/3433)

### ⚠️ Changed
- Deprecated `DeletedMessageListItemPredicate` in favor of `DeletedMessageVisibility`. This is a followup on [#3272](https://github.com/GetStream/stream-chat-android/pull/3272/files) which deprecated filtering messages inside `MessageListView` in favor of filtering messages inside `MessageListViewModel`. [#3409](https://github.com/GetStream/stream-chat-android/pull/3409)
- Added own capabilities. If you are using our UI components separately from our `ViewModel`s, this has the possibility of introducing a change in functionality. You can find the guide on implementing own capabilities [here](https://getstream.io/chat/docs/sdk/android/ui/guides/implementing-own-capabilities/). [#3389](https://github.com/GetStream/stream-chat-android/pull/3389)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the message input handling when typing quickly or holding down the delete (backspace) button. [#3355](https://github.com/GetStream/stream-chat-android/pull/3355)

### ⬆️ Improved
- Now single membered channels display the name of member instead of "Channel without name" [3423](https://github.com/GetStream/stream-chat-android/pull/3423)
- Channels with only one member now show the member's image in avatar. [3425](https://github.com/GetStream/stream-chat-android/pull/3425)
- Improved the way filters work in `ChannelList`, `ChannelsScreen` and `ChannelListViewModel`. Now the filters are nullable and if you want the default behavior, just pass in null. [#3422](https://github.com/GetStream/stream-chat-android/pull/3422)
- You can now completely override the filters by using `ChannelListViewModel.setFilters()` in the runtime, or by passing in custom `FilterObject` to the `ViewModelFactory` or the `ViewModel` constructor. [#3422](https://github.com/GetStream/stream-chat-android/pull/3422)


### ✅ Added
- Added pan to ImagePreviewActivity. [#3335](https://github.com/GetStream/stream-chat-android/pull/3335)
- Added `imageLoader` parameter to `ChatTheme` that allows providing a custom Coil `ImageLoader`. [#3336](https://github.com/GetStream/stream-chat-android/pull/3336)
- Added the "Copy Message" option to link messages [#3354](https://github.com/GetStream/stream-chat-android/pull/3354)
- Added padding customisation option to `ChannelList` and `MessageList` components. [#3350](https://github.com/GetStream/stream-chat-android/pull/3350)
- Added emoji sticker support. [3359](https://github.com/GetStream/stream-chat-android/pull/3359)
- Added support for own capabilities. You can read more about own capabilities [here](https://getstream.io/chat/docs/sdk/android/compose/guides/implementing-own-capabilities/). [#3389](https://github.com/GetStream/stream-chat-android/pull/3389)
- Added better handling for Compose ChannelListHeader and MessageListHeader states. We now cover Connected, Connecting and Disconnected states (added Connecting). [#3428](https://github.com/GetStream/stream-chat-android/pull/3428)
- Added the possibility to customize the message footer visibility through `MessageFooterVisibility` inside `MessageListViewModel`. [#3343](https://github.com/GetStream/stream-chat-android/pull/3433)

### ⚠️ Changed
- `loadMore` calls inside `MessageListViewModel` and `ChannelListViewModel` should no longer load data if there is no network connection. [3362](https://github.com/GetStream/stream-chat-android/pull/3362)
- Added own capabilities. If you are using our components individually this has the possibility of introducing a change in functionality. You can find the guide on implementing own capabilities [here](https://getstream.io/chat/docs/sdk/android/compose/guides/implementing-own-capabilities/). [#3389](https://github.com/GetStream/stream-chat-android/pull/3389)
- Replaced the `imageLoader` parameter in `ChatTheme` with the new `imageLoaderFactory` parameter that can used to provide a custom Coil `ImageLoader` factory.  [#3441](https://github.com/GetStream/stream-chat-android/pull/3441)

# April 12th, 2022 - 5.0.3
## Common changes for all artifacts

### ⚠️ Changed
- Updated Gradle version to [7.4.2](https://docs.gradle.org/7.4.2/release-notes.html). [#3281](https://github.com/GetStream/stream-chat-android/pull/3281)
- Update Coroutines to 1.6.1 and migrate to runTest. [#3327](https://github.com/GetStream/stream-chat-android/pull/3327)

## stream-chat-android-client

### 🐞 Fixed
- Fixed `User` model deserialization error when `User.image` or `User.name` is null. [#3283](https://github.com/GetStream/stream-chat-android/pull/3283)
- Fixed `Channel` model deserialization error when `Channel.image` or `Channel.name` is null. [#3306](https://github.com/GetStream/stream-chat-android/pull/3306)

### ✅ Added
- Added an `ExtraDataValidator` to intercept `ChatApi` calls and validate `CustomObject.extraData` does not contain the reserved keywords. [#3279](https://github.com/GetStream/stream-chat-android/pull/3279)

### ⚠️ Changed
- Migrate androidx-lifecycle version to [2.4.1](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.4.1). [#3282](https://github.com/GetStream/stream-chat-android/pull/3282)

## stream-chat-android-offline

### 🐞 Fixed
- Fixed crash related with logging out while running a request to update channels. [3286](https://github.com/GetStream/stream-chat-android/pull/3286)
- Fixed bug where user was not able to send and edit a message while offline. [3318](https://github.com/GetStream/stream-chat-android/pull/3324)

### ✅ Added
- Added `ChannelState::membersCount` property that can be used to observe total members of the channel. [#3297](https://github.com/GetStream/stream-chat-android/pull/3297)

## stream-chat-android-ui-common

### 🐞 Fixed
- Fixed avatar disappearing from a message group when `MessageListView.deletedMessageListItemPredicate = DeletedMessageListItemPredicate.VisibleToEveryone` or `MessageListView.deletedMessageListItemPredicate = DeletedMessageListItemPredicate.VisibleToAuthorOnly` and the last message in a group of messages posted by someone other than the currently logged in user was deleted. [#3272](https://github.com/GetStream/stream-chat-android/pull/3272)

## stream-chat-android-ui-components

### 🐞 Fixed
- Adding ShowAvatarPredicate for MessageOptions overlay making it possible to hide the avatar picture when in the message options. [#3302](https://github.com/GetStream/stream-chat-android/pull/3302)
- Users now able to open `MessageOptionsDialogFragment` by clicking on a reaction left on a Giphy message. [#3620](https://github.com/GetStream/stream-chat-android/pull/3260)
- inside `MessageOptionsDialogFragment` now properly displays all of the reactions to a message. Previously it erroneously displayed a blank state. [#3620](https://github.com/GetStream/stream-chat-android/pull/3260)
- Fixed the links in UI Components code snippets. [#3261](https://github.com/GetStream/stream-chat-android/pull/3261)
- Messages containing links are now properly aligned with other types of messages. They use `@dimen/stream_ui_spacing_small` for their root layout start and end padding. [#3264](https://github.com/GetStream/stream-chat-android/pull/3264)
- Fixed avatar disappearing from a message group when `MessageListView.deletedMessageListItemPredicate = DeletedMessageListItemPredicate.VisibleToEveryone` or `MessageListView.deletedMessageListItemPredicate = DeletedMessageListItemPredicate.VisibleToAuthorOnly` and the last message in a group of messages posted by someone other than the currently logged in user was deleted. [#3272](https://github.com/GetStream/stream-chat-android/pull/3272)
- Fixed bug in which member counter shown in the `MessageListHeaderViewModel` is incorrect and limited to 30 only. [#3297](https://github.com/GetStream/stream-chat-android/pull/3297)

### ✅ Added
- Added `membersCount` livedata in `MessageListHeaderViewModel` to observe number of all members of channel. [#3297](https://github.com/GetStream/stream-chat-android/pull/3297)

## stream-chat-android-compose

### 🐞 Fixed
- Added thumbnails for video attachments in the attachment picker. [#3300](https://github.com/GetStream/stream-chat-android/pull/3300)
- Fixed a crash occurring when the user would click on a preview of a link that contained no scheme. [#3331](https://github.com/GetStream/stream-chat-android/pull/3331)

### ⬆️ Improved
- Improved the way typing updates work in the MessageComposerController. [#3313](https://github.com/GetStream/stream-chat-android/pull/3313)

### ✅ Added
- Added a way to customize the visibility of deleted messages. [#3298](https://github.com/GetStream/stream-chat-android/pull/3298)
- Added support for file upload configuration that lets you specify what types of files and images you want to allow or block from being uploaded. [3288](https://github.com/GetStream/stream-chat-android/pull/3288)
- Added Compose SDK Guidelines for internal and external contributors. [#3315](https://github.com/GetStream/stream-chat-android/pull/3315)

### ⚠️ Changed
- Switched from vertical to horizontal scrolling for files in the preview section of the message composer. [#3289](https://github.com/GetStream/stream-chat-android/pull/3289)

# March 30th, 2022 - 5.0.2
## stream-chat-android-client
### ✅ Added
- Added a `systemMessage: Message` parameter to  `ChatClient::addMembers`, `ChatClient::removeMembers`, `ChannelClient::addMembers` and `ChannelClient::removeMembers` to send a system message to that channel. [#3254](https://github.com/GetStream/stream-chat-android/pull/3254)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed a bug which occurs when we reinitialize `OfflinePlugin` because it uses old instance of `StateRegistry` and `LogicRegistry`. [#3267](https://github.com/GetStream/stream-chat-android/pull/3267)

## stream-chat-android-ui-components
### 🐞 Fixed
- Users now able to open `MessageOptionsDialogFragment` by clicking on a reaction left on a Giphy message. [#3620](https://github.com/GetStream/stream-chat-android/pull/3260)
- inside `MessageOptionsDialogFragment` now properly displays all of the reactions to a message. Previously it erroneously displayed a blank state. [#3620](https://github.com/GetStream/stream-chat-android/pull/3260)
- Fixed the links in UI Components code snippets. [#3261](https://github.com/GetStream/stream-chat-android/pull/3261)
- Messages containing links are now properly aligned with other types of messages. They use `@dimen/stream_ui_spacing_small` for their root layout start and end padding. [#3264](https://github.com/GetStream/stream-chat-android/pull/3264)
- Made it impossible to send blank or empty messages. [#3269](https://github.com/GetStream/stream-chat-android/pull/3269)

## stream-chat-android-compose
### 🐞 Fixed
- Made it impossible to send blank or empty messages. [#3269](https://github.com/GetStream/stream-chat-android/pull/3269)

### ✅ Added
- Added support for failed messages and an option to resend them. [#3263](https://github.com/GetStream/stream-chat-android/pull/3263)

# March 24th, 2022 - 5.0.1
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed a bug where the missing implementation of the `MessageTextTransformer` caused message text not to show up. [#3248](https://github.com/GetStream/stream-chat-android/pull/3248)

# March 24th, 2022 - 5.0.0
**5.0.0** is a major release! You can read more about the motivation behind the effort and featured changes in the [announcement blog post](https://getstream.io/blog/android-v5-sdk-release/).
## Common changes for all artifacts
### 🐞 Fixed
- Fixed memory leaks related to image loading. [#2979](https://github.com/GetStream/stream-chat-android/pull/2979)

### ⬆️ Improved
- Replaced KAPT with KSP. [#3113](https://github.com/GetStream/stream-chat-android/pull/3113)

### ⚠️ Changed
- Updated AGP version to 7.1.2 and Gradle version to 7.4. [#3159](https://github.com/GetStream/stream-chat-android/pull/3159)

## stream-chat-android-client
### ✅ Added
- Added possibility to configure `RetryPolicy` using `ChaClient.Builder()`. [#3069](https://github.com/GetStream/stream-chat-android/pull/3069)

### ⚠️ Changed
- Add `Channel::image`, `Channel:name`, `User::image`, `User::name` properties. [#3139](https://github.com/GetStream/stream-chat-android/pull/3139)
- Deprecated `Member:role` in favor of `Member:channelRole` [#3189](https://github.com/GetStream/stream-chat-android/pull/3189)

## stream-chat-android-offline
🚨🚨 **v5.0.0** release brings a big change to the offline support library - it replaces `ChatDomain` with the `OfflinePlugin`. Make sure to check our [migration guide](https://getstream.io/chat/docs/sdk/android/client/guides/chatdomain-migration/)! 🚨🚨

### 🐞 Fixed
- Unread count for muted channels no longer increments when the channel is muted and new messages are received. [#3112](https://github.com/GetStream/stream-chat-android/pull/3112)
- Fixed marking the channel as read if it was opened offline previously. [#3162](https://github.com/GetStream/stream-chat-android/pull/3162)

### ❌ Removed
- Moved `RetryPolicy` related logic to `ChatClient`. [#3069](https://github.com/GetStream/stream-chat-android/pull/3069)

## stream-chat-android-ui-common
### ❌ Removed
- Removed ChatMarkdown in favor of ChatMessageTextTransformer [#3189](https://github.com/GetStream/stream-chat-android/pull/3189)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed an issue with message flickering when sending a message with file attachments. [#3209](https://github.com/GetStream/stream-chat-android/pull/3209)
- Fixed a crash when overriding `ChatUI::imageHeadersProvider` caused by compiler [issue](https://youtrack.jetbrains.com/issue/KT-49793). [#3237](https://github.com/GetStream/stream-chat-android/pull/3237)

### ✅ Added
- Added a separate `LinkAttachmentsViewHolder` for handling messages containing link attachments and no other types of attachments. [#3070](https://github.com/GetStream/stream-chat-android/pull/3070)
- Added a separate `FileAttachmentsViewHolder` for handling messages containing file attachments of different types or file attachments not handled by one of the other `ViewHolder`s. [#3091](https://github.com/GetStream/stream-chat-android/pull/3091)
- Introduced `InnerAttachmentViewHolder` as an inner ViewHolder for custom attachments. [#3183](https://github.com/GetStream/stream-chat-android/pull/3183)
- Introduced `AttachmentFactory` as a factory for custom attachment ViewHolders. [#3116](https://github.com/GetStream/stream-chat-android/pull/3116)
- Introduced `AttachmentFactoryManager` as a manager for the list of registered attachment factories. The class is exposed via `ChatUI`. [#3116](https://github.com/GetStream/stream-chat-android/pull/3116)
- Added an attribute to customize the color state list of the AttachmentsDialog buttons called `streamUiAttachmentTabButtonColorStateList`. [#3242](https://github.com/GetStream/stream-chat-android/pull/3242)

### ⚠️ Changed
- Separated the Giphy attachments and content to a GiphyAttachmentViewHolder. [#2932](https://github.com/GetStream/stream-chat-android/pull/2932)
- Created a GiphyMediaAttachmentView and its respective style to customize giphies. [#2932](https://github.com/GetStream/stream-chat-android/pull/2932)
- You can now use `original` sized giphies that apply resizing based on the GIF size. [#2932](https://github.com/GetStream/stream-chat-android/pull/2932)
- Use `fixedHeight` or `fixedHeightDownsampled` giphies to use a fixed height that keeps the aspect ratio and takes up less memory. [#2932](https://github.com/GetStream/stream-chat-android/pull/2932)
- Make sure to check out our giphy attachment styles (GiphyMediaAttachmentView) for customization.
- Created an ImageAttachmentViewHolder that represents images in the message list. [#3067](https://github.com/GetStream/stream-chat-android/pull/3067)
- Renamed MediaAttachmentViewStyle and its attributes to ImageAttachmentViewStyle. [#3067](https://github.com/GetStream/stream-chat-android/pull/3067)
- Messages containing link attachments and no other types of attachments are no longer handled by `TextAndAttachmentsViewHolder`, instead they are handled by `LinkAttachmentsViewHolder`. [#3070](https://github.com/GetStream/stream-chat-android/pull/3070)
- Messages containing file attachments of different file types or types not handled by one of the other `ViewHolders` are no longer handled by `TextAndAttachmentsViewHolder`, instead they are handled by `FileAttachmentsViewHolder`. [#3091](https://github.com/GetStream/stream-chat-android/pull/3091)
- Updated the structure of UI components documentation. [UI Components documentation](https://getstream.io/chat/docs/sdk/android/ui/overview/). [#3186](https://github.com/GetStream/stream-chat-android/pull/3186)
- Updated the code snippets from the UI Components documentation in the `stream-chat-android-docs` module. [3205](https://github.com/GetStream/stream-chat-android/pull/3205)

### ❌ Removed
- All usage of `ChatDomain`. [#3190](https://github.com/GetStream/stream-chat-android/pull/3190)
- Removed "Pin message", "Reply", "Thread reply" message actions for messages that are not synced. [#3226](https://github.com/GetStream/stream-chat-android/pull/3226)

## stream-chat-android-compose
### 🐞 Fixed
- Mitigated the effects of `ClickableText` consuming all pointer events when messages contain links by passing long press handlers to `MessageText`. [#3137](https://github.com/GetStream/stream-chat-android/pull/3137)
- Fixed an issue with message flickering when sending a message with file attachments. [#3209](https://github.com/GetStream/stream-chat-android/pull/3209)
- Fixed ripple color in dark mode. [#3211](https://github.com/GetStream/stream-chat-android/pull/3211)
- Long user names no longer break layout in the message list. [#3219](https://github.com/GetStream/stream-chat-android/pull/3219)
- Fixed the click handler on the last item in the image attachments content. [#3221](https://github.com/GetStream/stream-chat-android/pull/3221)

### ⬆️ Improved
- Allowed passing long press handlers to `MessageText`. [#3137](https://github.com/GetStream/stream-chat-android/pull/3137)

### ✅ Added
- Added code snippets from the Compose documentation to the `stream-chat-android-docs` module. [3197](https://github.com/GetStream/stream-chat-android/pull/3197)
- Added support for delivery indicator in the message list. [#3218](https://github.com/GetStream/stream-chat-android/pull/3218)

### ⚠️ Changed
- Replaced the `reactionTypes` field in `ChatTheme` with the new `reactionIconFactory` field that allows customizing reaction icons. [#3046](https://github.com/GetStream/stream-chat-android/pull/3046)
- `MessageText` now requires the parameter `onLongItemClick: (Message) -> Unit`. This was done in order to mitigate `ClickableText` consuming all pointer events. [#3137](https://github.com/GetStream/stream-chat-android/pull/3137)
- Renamed the `state.channel` package to `state.channels` for consistency. [#3143](https://github.com/GetStream/stream-chat-android/pull/3143)
- Renamed the `viewmodel.channel` package to `viewmodel.channels` for consistency. [#3143](https://github.com/GetStream/stream-chat-android/pull/3143)
- Moved the contents of the `ui.imagepreview` and `ui.mediapreview` packages to `ui.attachments.preview`. [#3143](https://github.com/GetStream/stream-chat-android/pull/3143)
- Moved the preview handlers from the `ui.filepreview` package to `ui.attachments.preview.handler` [#3143](https://github.com/GetStream/stream-chat-android/pull/3143)

### ❌ Removed
- Removed "Pin message", "Reply", "Thread reply" message actions for messages that are not synced. [#3226](https://github.com/GetStream/stream-chat-android/pull/3226)

# March 9th, 2022 - 4.30.1
## stream-chat-android-client
### ✅ Added
- Added `notificationChannel` lambda parameter to `NotificationHandlerFactory::createNotificationHandler` which is being used to create a `NotificationChannel`.
  You can use it to customize notifications priority, channel name, etc. [#3167](https://github.com/GetStream/stream-chat-android/pull/3167)

### ⚠️ Changed
- `LoadNotificationDataWorker` is now using a separate `NotificationChannel` with `NotificationCompat.PRIORITY_LOW`.
  You can customize its name by overriding `stream_chat_other_notifications_channel_name` string. [#3167](https://github.com/GetStream/stream-chat-android/pull/3167)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed updating typing users. [#3154](https://github.com/GetStream/stream-chat-android/pull/3154)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed displaying long usernames in message's footnote within `MessageListView`. [#3149](https://github.com/GetStream/stream-chat-android/pull/3149)
- A bug that made `ScrollButtonView` in `MessageListView` permanently visible. [#3170](https://github.com/GetStream/stream-chat-android/pull/3170)
- Fixed display of read status indicators [#3181](https://github.com/GetStream/stream-chat-android/pull/3181)

### ✅ Added
- Added a way to check if the adapters and message/channel lists have been initialized or not. [#3182](https://github.com/GetStream/stream-chat-android/pull/3182)
- Added `streamUiRetryMessageEnabled` attribute to `MessageListView` that allows to show/hide retry action in message's overlay. [#3185](https://github.com/GetStream/stream-chat-android/pull/3185)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed display of read status indicators [#3181](https://github.com/GetStream/stream-chat-android/pull/3181)

# March 2nd, 2022 - 4.30.0
## Common changes for all artifacts
### ⬆️ Improved
- We upgraded our Kotlin version to 1.6, Moshi to 1.13 and Compose to 1.1.1. [#3104](https://github.com/GetStream/stream-chat-android/pull/3104)[#3123](https://github.com/GetStream/stream-chat-android/pull/3123)
- Updated Google's Accompanist version. [#3104](https://github.com/GetStream/stream-chat-android/pull/3104)

### ⚠️ Changed
- These version updates mean our SDK now expects the minimum of AGP 7.x.x. We recommend using 7.1+. [#3104](https://github.com/GetStream/stream-chat-android/pull/3104)

## stream-chat-android-compose
### ⚠️ Changed
- Since we're using Compose 1.1.1 for our SDK, we recommend upgrading to avoid conflicts. [#3104](https://github.com/GetStream/stream-chat-android/pull/3104)

# February 24th, 2022 - 4.29.0
## stream-chat-android-offline
### 🐞 Fixed
- Fixed updating `ChatDomain::totalUnreadCount` and `ChatDomain::channelUnreadCount` after restoring app from the background and
  when sending a message to a channel without read enabled. [#3121](https://github.com/GetStream/stream-chat-android/pull/3121)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed setting custom empty and loading views for `MessageListView`. [#3082](https://github.com/GetStream/stream-chat-android/pull/3082)

### ⬆️ Improved
- Disabled command popups when attachments are present. [#3051](https://github.com/GetStream/stream-chat-android/pull/3051)
- Disabled the attachments button when popups are present. [#3051](https://github.com/GetStream/stream-chat-android/pull/3051)

### ✅ Added
- Added `ChatUI.channelNameFormatter` to allow customizing the channel's name format. [#3068](https://github.com/GetStream/stream-chat-android/pull/3068)
- Added a customizable height attribute to SearchInputView [#3081](https://github.com/GetStream/stream-chat-android/pull/3081)
- Added `ChatUI.dateFormatter` to allow customizing the way the dates are formatted. [#3085](https://github.com/GetStream/stream-chat-android/pull/3085)
- Added ways to show/hide the delivery status indicators for channels and messages. [#3102](https://github.com/GetStream/stream-chat-android/pull/3102)

### ⚠️ Changed
- Disabled editing on Giphy messages given that it's breaking the UX and can override the GIF that was previously put in. [#3071](https://github.com/GetStream/stream-chat-android/pull/3071)

### ❌ Removed
- Removed ExoMedia dependency in favor of standard Android `VideoView`. [#3098](https://github.com/GetStream/stream-chat-android/pull/3098)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed back press handling. [#3120](https://github.com/GetStream/stream-chat-android/pull/3120)

### ✅ Added
- Exposed a way to clear the message composer externally, e.g. when using custom sendMessage handlers. [#3100](https://github.com/GetStream/stream-chat-android/pull/3100)
- Exposed `loadingMoreContent` for the `ChannelList` and `Channels` components that allows you to override the default loading more content. [#3103](https://github.com/GetStream/stream-chat-android/pull/3103)
- Exposed `loadingMoreContent` for the `MessageList` and `Messages` components that allows you to override the default loading more content. [#3103](https://github.com/GetStream/stream-chat-android/pull/3103)
- Added the `attachmentsContentImageGridSpacing` option to `StreamDimens`, to make it possible to customize the spacing between image attachment tiles via `ChatTheme`. [#3105](https://github.com/GetStream/stream-chat-android/pull/3105)

### ⚠️ Changed
- Replaced the `reactionTypes` field in `ChatTheme` with the new `reactionIconFactory` field that allows customizing reaction icons. [#3046](https://github.com/GetStream/stream-chat-android/pull/3046)
- Disabled editing on Giphy messages given that it's breaking the UX and can override the GIF that was previously put in. [#3071](https://github.com/GetStream/stream-chat-android/pull/3071)

### ❌ Removed
- Removed ExoMedia dependency in favor of standard Android `VideoView`. [#3092](https://github.com/GetStream/stream-chat-android/pull/3092)
- Removed `SystemBackPressHandler` in favor of `BackHandler` from the Compose framework. [#3120](https://github.com/GetStream/stream-chat-android/pull/3120)

# February 17th, 2022 - 4.28.4
## stream-chat-android-client
### ✅ Added
- Added the `member` field to the `MemberRemovedEvent`. [#3090](https://github.com/GetStream/stream-chat-android/pull/3090)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed how member removal is handled in `DefaultChatEventHandler`. [#3090](https://github.com/GetStream/stream-chat-android/pull/3090)

# February 16th, 2022 - 4.28.3
## stream-chat-android-ui-components
### ⬆️ Improved
- Improved the logic around mentions and users that can be mentioned within the input. [#3088](https://github.com/GetStream/stream-chat-android/pull/3088)

## stream-chat-android-compose
### ⬆️ Improved
- Improved the logic around mentions and users that can be mentioned within the input. [#3088](https://github.com/GetStream/stream-chat-android/pull/3088)

# February 9th, 2022 - 4.28.2
## Common changes for all artifacts
- Fix crash with offline support. [#3063](https://github.com/GetStream/stream-chat-android/pull/3063)

# February 9th, 2022 - 4.28.1
## Common changes for all artifacts
- Fix crash when events were received. [#3058](https://github.com/GetStream/stream-chat-android/pull/3058)

# February 8th, 2022 - 4.28.0
## 🚨 Old UI Module removed
`stream-chat-android` is deprecated and won't be maintained anymore. The module will continue working, but we won't be releasing new versions.
The source code has been moved to this [archived repository](https://github.com/GetStream/stream-chat-android-old-ui)
Consider migrating to `stream-chat-android-ui-components` or `stream-chat-android-compose`. Here you can find a set of useful resources for migration:
- [UI Components Documentation](https://getstream.io/chat/docs/sdk/android/ui/overview/)
- [Android Chat Messaging Tutorial](https://getstream.io/tutorials/android-chat/)
- [Compose UI Components Documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/)
- [Compose Chat Messaging Tutorial](https://getstream.io/chat/compose/tutorial/)
- [Old Sample App Migration PR](https://github.com/GetStream/stream-chat-android/pull/2467)

## Common changes for all artifacts
### ✅ Added
- Create new artifact to integrate Xiaomi Mi Push with Stream. You will need to add  `stream-chat-android-pushprovider-xiaomi` artifact to your App. Check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi) for further details. [#2444](https://github.com/GetStream/stream-chat-android/pull/2444)

### ⚠️ Changed
- Update Android Gradle Plugin version to 7.1.0 and Gradle version to 7.3.3. [#2989](https://github.com/GetStream/stream-chat-android/pull/2989)

## stream-chat-android-client
### ⬆️ Improved
- Internal implementation only asks to the provided `TokenProvider` a new token when it is really needed. [#2995](https://github.com/GetStream/stream-chat-android/pull/2995)

### ⚠️ Changed
- UnknownHostException is no longer considered a permanent network error. [#3054](https://github.com/GetStream/stream-chat-android/pull/3054)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed memory leak related to database initialization.[#2974](https://github.com/GetStream/stream-chat-android/pull/2974)

### ✅ Added
- Added new extension function `ChatClient::deleteChannel`. [#3007](https://github.com/GetStream/stream-chat-android/pull/3007)

### ⚠️ Changed
- Deprecated `ChatDomain::deleteChannel` in favour of `ChatClient::deleteChannel`. [#3007](https://github.com/GetStream/stream-chat-android/pull/3007)

## stream-chat-android-ui-common
### ✅ Added
- Added new extension function `ChatClient::loadMessageById`. [#2929](https://github.com/GetStream/stream-chat-android/pull/2929)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the logic for fetching encoding for URLs when opening PDFs and similar documents in the MessageList. [#3017](https://github.com/GetStream/stream-chat-android/pull/3017)

### ⬆️ Improved
- Replaced Lottie typing indicator with a custom view. [#3004](https://github.com/GetStream/stream-chat-android/pull/3004)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the way our attachments work and are represented in Compose to support more attachment types. [#2955](https://github.com/GetStream/stream-chat-android/pull/2955)
- Fixed the logic for fetching encoding for URLs when opening PDFs and similar documents in the MessageList. [#3017](https://github.com/GetStream/stream-chat-android/pull/3017)

### ⬆️ Improved
- Improved RTL support in Compose [#2987](https://github.com/GetStream/stream-chat-android/pull/2987)
- Made the SDK smaller by removing Materials Icons dependency [#2987](https://github.com/GetStream/stream-chat-android/pull/2987)
- Removed unnecessary experimental flags, opted in into APIs we're using from Compose. [#2983](https://github.com/GetStream/stream-chat-android/pull/2983)

### ✅ Added
- Added [`Custom Attachments guide`](https://getstream.io/chat/docs/sdk/android/composee/guides/adding-custom-attachments/). [#2967](https://github.com/GetStream/stream-chat-android/pull/2967)
- Added `onHeaderAvatarClick` parameter to the `ChannelsScreen` component. [#3016](https://github.com/GetStream/stream-chat-android/pull/3016)
- Exposed `lazyListState` for the `ChannelList` and `Channels` components that allows you to control the scrolling behavior and state. [#3049](https://github.com/GetStream/stream-chat-android/pull/3049)
- Exposed `helperContent` for the `ChannelList` and `Channels` components that allows you to implement a helper UI such as scroll to top button for the channel list. [#3049](https://github.com/GetStream/stream-chat-android/pull/3049)
- Exposed `lazyListState` for the `MessageList` and `Messages` components that allows you to control the scrolling behavior and state. [#3044](https://github.com/GetStream/stream-chat-android/pull/3044)
- Exposed `helperContent` for the `MessageList` and `Messages` components that allows you to override the default scrolling behavior UI.  [#3044](https://github.com/GetStream/stream-chat-android/pull/3044)

### ⚠️ Changed
- Renamed `onHeaderClickAction` parameter to `onHeaderActionClick` for the `ChannelsScreen` component. [#3016](https://github.com/GetStream/stream-chat-android/pull/3016)
- `MessageList` and `Messages` now have two new parameters that have default values. Please make sure that you check out the changes and that everything still works for you. [#3044](https://github.com/GetStream/stream-chat-android/pull/3044)

## stream-chat-android-pushprovider-xiaomi
### ✅ Added
- Added a `XiaomiMessagingDelegate` class to simplify custom implementations of `PushMessageReceiver` that forward messages to the SDK. See [Using a Custom PushMessageReceiver](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi#using-a-custom-pushmessagereceiver) for more details. [#2444](https://github.com/GetStream/stream-chat-android/pull/2444)

# February 2nd, 2022 - 4.27.2
## stream-chat-android-offline
### 🐞 Fixed
- Fixed refreshing cached channels after setting the user. [#3010](https://github.com/GetStream/stream-chat-android/pull/3010)

# January 31th, 2022 - 4.27.1
## stream-chat-android-offline
### 🐞 Fixed
- Fixed clearing cache after receiving channel truncated event. [#3001](https://github.com/GetStream/stream-chat-android/pull/3001)

# January 25th, 2022 - 4.27.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed bug related to the wrong unread messages count when a socket connection is not available. [#2927](https://github.com/GetStream/stream-chat-android/pull/2927)
- Fixed deserialization issue when parsing the `Message` object while searching for a message from a channel with 0 members. [#2947](https://github.com/GetStream/stream-chat-android/pull/2947)

### ✅ Added
- Added the `systemMessage` parameter to `ChatClient::truncateChannel` and `ChannelClient:truncate` methods that represents a system message that will be displayed after the channel was truncated. [#2949](https://github.com/GetStream/stream-chat-android/pull/2949)
- Added the `message` parameter to the `ChannelTruncatedEvent` that represents a system message that will be displayed after the channel was truncated. [#2949](https://github.com/GetStream/stream-chat-android/pull/2949)
- Added method to consult the settings of the app. Use `ChatClient.instance().appSettings()` to request the settings of your app. [#2960](https://github.com/GetStream/stream-chat-android/pull/2960)
- Added `ChatClient.shuffleGiphy` extension function and removing ShuffleGiphy use case. [#2962](https://github.com/GetStream/stream-chat-android/pull/2962)
- Added `ChatClient.sendGiphy` extension function and removing SendGiphy use case. [#2963](https://github.com/GetStream/stream-chat-android/pull/2963)
- Added `Channel::ownCapabilities` and `ChannelCapabilities` object.
  Channel capabilities provide you information on which features are available for the current user. [#2971](https://github.com/GetStream/stream-chat-android/pull/2971)

### ⚠️ Changed
- Deprecated `ChatDomain.leaveChannel`. Use ChatClient.removeMembers instead. [#2926](https://github.com/GetStream/stream-chat-android/pull/2926)

## stream-chat-android-offline
### ⬆️ Improved
- Utilized the `message` parameter of the `ChannelTruncatedEvent` to show a system message instantly after the channel was truncated. [#2949](https://github.com/GetStream/stream-chat-android/pull/2949)

### ✅ Added
- Added new extension function `ChatClient::cancelMessage`. [#2928](https://github.com/GetStream/stream-chat-android/pull/2928)
- Added `ChatClient::needsMarkRead` extension function to check if a channel can be marked as read. [#2920](https://github.com/GetStream/stream-chat-android/pull/2920)

### ⚠️ Changed
- Deprecated `ChatDomain::cancelMessage` in favour of `ChatClient::cancelMessage`. [#2928](https://github.com/GetStream/stream-chat-android/pull/2928)

## stream-chat-android-ui-components
### 🐞 Fixed
- Handling video attachments that's don't have mime-type, but have type. [2919](https://github.com/GetStream/stream-chat-android/pull/2919)
- Intercepted and blocked attachment preview for attachments which are not fully uploaded. [#2950](https://github.com/GetStream/stream-chat-android/pull/2950)
- Fixed a bug when changes to the mentioned users in a message were not propagated to the UI. [2951](https://github.com/GetStream/stream-chat-android/pull/2951)

### ⬆️ Improved
- Improve Korean 🇰🇷 translations. [#2953](https://github.com/GetStream/stream-chat-android/pull/2953)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed crashes caused by deleting channels [#2942](https://github.com/GetStream/stream-chat-android/pull/2942)

### ⬆️ Improved
- `ReactionOptions` now displays the option to show more reactions if there are more than 5 available [#2918](https://github.com/GetStream/stream-chat-android/pull/2918)
- Improve Korean 🇰🇷 translations. [#2953](https://github.com/GetStream/stream-chat-android/pull/2953)
- Improved `MessageComposer` UX by disabling commands when attachments or text are present. [#2961](https://github.com/GetStream/stream-chat-android/pull/2961)
- Improved `MessageComposer` UX by disabling attachment integration button when popups with suggestions are present. [#2961](https://github.com/GetStream/stream-chat-android/pull/2961)

### ✅ Added
- Added `ExtendedReactionsOptions` and `ReactionsPicker` in order to improve reaction picking UX [#2918](https://github.com/GetStream/stream-chat-android/pull/2918)
- Added documentation for [`ReactionsPicker`](https://getstream.io/chat/docs/sdk/android/compose/message-components/reactions-picker/) [#2918](https://github.com/GetStream/stream-chat-android/pull/2918)
- Added ways to customize the channel, message and member query limit when building a ChannelListViewModel [#2948](https://github.com/GetStream/stream-chat-android/pull/2948)

# January 12th, 2022 - 4.26.0
## Common changes for all artifacts
### ⬆️ Improved
- 🚨 Breaking change: Markdown support is moved into a standalone module `stream-chat-android-markdown-transformer` which is not included by default. You can use it with `ChatUI.messageTextTransformer` to add Markdown support to your app. You can find more information [here](https://getstream.io/chat/docs/sdk/android/ui/chatui/#markdown). [#2786](https://github.com/GetStream/stream-chat-android/pull/2786)

## stream-chat-android-client
### ✅ Added
- Added `Member::banned` property that represents, if the channel member is banned. [#2915](https://github.com/GetStream/stream-chat-android/pull/2915)
- Added `Member::channelRole` property that represents the user's channel-level role. [#2915](https://github.com/GetStream/stream-chat-android/pull/2915)

## stream-chat-android-offline
### 🐞 Fixed
- Fixed populating mentions after editing the message. `Message::mentionedUsers` shouldn't be empty if edited message contains mentioned users. [#2852](https://github.com/GetStream/stream-chat-android/pull/2852)

### ✅ Added
- Added `memberLimit` to `ChatDomain::queryChannels` and `ChatDomain::queryChannelsLoadMore` that allows modifying the number of members to fetch per channel. [#2826](https://github.com/GetStream/stream-chat-android/pull/2826)

### ❌ Removed
- Removed `QueryChannelsLoadMore` usecase. [#2790](https://github.com/GetStream/stream-chat-android/pull/2790)
- `QueryChannelsController::loadMore` is removed and logic is moved into `ChatDomain`. [#2790](https://github.com/GetStream/stream-chat-android/pull/2790)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed displaying mentions popup when text contains multiple lines. [#2851](https://github.com/GetStream/stream-chat-android/pull/2851)
- Fixed the loading/playback speed of GIFs. [#2914](https://github.com/GetStream/stream-chat-android/pull/2914)
- Fixed scroll persisting after long tapping on an item in the message list. [#2916](https://github.com/GetStream/stream-chat-android/pull/2916)
- Fixed footnote of messages showing "Only Visible to You". This message was visible even when deleted messages were visible to everyone. [#2923](https://github.com/GetStream/stream-chat-android/pull/2923)

### ⬆️ Improved
- Improved the way thread pagination works. [#2845](https://github.com/GetStream/stream-chat-android/pull/2845)

### ✅ Added
- Added `memberLimit` parameter to `ChannelListViewModel` and `ChannelListViewModelFactory` that allows modifying the number of members to fetch per channel. [#2826](https://github.com/GetStream/stream-chat-android/pull/2826)
- Added `ChatMessageTextTransformer` to transform messages and set them to `TextView`. [#2786](https://github.com/GetStream/stream-chat-android/pull/2786)
- Added `AutoLinkableTextTransformer` which is an implementation of `ChatMessageTextTransformer`. After applying the transformer, it also makes links clickable in TextView. [#2786](https://github.com/GetStream/stream-chat-android/pull/2786)

### ⚠️ Changed
- `ChatUI.markdown` is deprecated in favour of `ChatUI.messageTextTransformer`. [#2786](https://github.com/GetStream/stream-chat-android/pull/2786)
- In the sample app the new behaviour for new messages is to count unread messages, instead of always scroll to bottom [#2865](https://github.com/GetStream/stream-chat-android/pull/)

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a small issue with user avatars flickering [#2822](https://github.com/GetStream/stream-chat-android/pull/2822)
- Fixed faulty scrolling behavior in `Messages` by adding an autoscroll. [#2857](https://github.com/GetStream/stream-chat-android/pull/2857)
- Fixed the font size of avatar initials in the message list. [2862](https://github.com/GetStream/stream-chat-android/pull/2862)
- Fixed faulty scrolling behavior in `Channels` by adding an autoscroll. [#2887](  https://github.com/GetStream/stream-chat-android/pull/2887)
- Fixed the loading/playback speed of GIFs. [#2914](https://github.com/GetStream/stream-chat-android/pull/2914)

### ⬆️ Improved
- Added an animation to the `SelectedChannelMenu` component.
- Added an animation to the `ChannelInfo` component.
- Avatars now show fallback initials in case there was an error while loading images from the network. [#2830](https://github.com/GetStream/stream-chat-android/pull/2830)
- Added more parameters to the stateless version of the MessageComposer for consistency [#2809](https://github.com/GetStream/stream-chat-android/pull/2809)
- Updated primary accent colors in order to achieve a better contrast ratio for accessibility [#2857](https://github.com/GetStream/stream-chat-android/pull/2857)
- Removed default background color from `MessageItem` [#2857](https://github.com/GetStream/stream-chat-android/pull/2857)
- Added multiline mentions support [#2859](https://github.com/GetStream/stream-chat-android/pull/2859)
- Improved the way thread pagination works. [#2845](https://github.com/GetStream/stream-chat-android/pull/2845)

### ✅ Added
- Added the `headerContent` and `centerContent` Slot APIs for the `SelectedChannelMenu` component. [#2823](https://github.com/GetStream/stream-chat-android/pull/2823)
- Added the `headerContent` and `centerContent` Slot APIs for the `ChannelInfo` component. [#2823](https://github.com/GetStream/stream-chat-android/pull/2823)
- You can now define a `placeholderPainter` for the `Avatar` that is shown while the image is loading. [#2830](https://github.com/GetStream/stream-chat-android/pull/2830)
- Added more Slot APIs to the`MessageComposer` and `MessageInput` components [#2809](https://github.com/GetStream/stream-chat-android/pull/2809)
- Added [SelectedReactionsMenu documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/selected-reactions-menu/). [#2868](https://github.com/GetStream/stream-chat-android/pull/2868)

### ⚠️ Changed
- Updated [ChatTheme documentation](https://getstream.io/chat/docs/sdk/android/compose/general-customization/chat-theme/). [#2833](https://github.com/GetStream/stream-chat-android/pull/2833)
- Updated [ChannelsScreen documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channels-screen/). [#2839](https://github.com/GetStream/stream-chat-android/pull/2839)
- Updated [ChannelItem documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-item/). [#2832](https://github.com/GetStream/stream-chat-android/pull/2832)
- Updated [ChannelListHeader documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list-header/). [#2828](https://github.com/GetStream/stream-chat-android/pull/2828)
- Updated [Component Architecture documentation](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/). [#2834](https://github.com/GetStream/stream-chat-android/pull/2834)
- Updated [SelectedChannelMenu documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/selected-channel-menu/). [#2838](https://github.com/GetStream/stream-chat-android/pull/2838)
- Updated [ChannelList documentation](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list/). [#2847](https://github.com/GetStream/stream-chat-android/pull/2847)
- Updated [AttachmentsPicker documentation](https://getstream.io/chat/docs/sdk/android/compose/message-components/attachments-picker/) [#2860](https://github.com/GetStream/stream-chat-android/pull/2860)
- Renamed the `ChannelInfo` component to `SelectedChannelMenu`. [#2838](https://github.com/GetStream/stream-chat-android/pull/2838)
- Updated [Overview documentation](https://getstream.io/chat/docs/sdk/android/compose/overview/). [#2836](https://github.com/GetStream/stream-chat-android/pull/2836)
- Updated [Custom Attachments documentation](https://getstream.io/chat/docs/sdk/android/compose/general-customization/attachment-factory/) with minor sentence formatting changes [#2878](https://github.com/GetStream/stream-chat-android/pull/2878)
- Updated [MessagesScreen documentation](https://getstream.io/chat/docs/sdk/android/compose/message-components/messages-screen/) [#2866](https://github.com/GetStream/stream-chat-android/pull/2866)
- Updated [MessageList documentation](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list/). [#2869](https://github.com/GetStream/stream-chat-android/pull/2869)

# December 30th, 2021 - 4.25.1
## stream-chat-android-client
### ✅ Added
- Added support to paginate messages pinned in a channel. [#2848](https://github.com/GetStream/stream-chat-android/pull/2848).


# December 23th, 2021 - 4.25.0
## Common changes for all artifacts
### ⬆️ Improved
- Updated dependency versions
  - Kotlin 1.5.31
  - Compose framework 1.0.5
  - AndroidX
  - Lottie 4.2.2
  - OkHttp 4.9.3
  - Room 2.4.0
  - and other, see [#2771](https://github.com/GetStream/stream-chat-android/pull/2771) for more details

## stream-chat-android-offline
### 🐞 Fixed
- Fixed a bug when hard deleted messages still remain in the UI.
- Stabilized behavior of users' updates propagation across values of the channels and the messages. [#2803](https://github.com/GetStream/stream-chat-android/pull/2803)

### ⚠️ Changed
- 🚨 Breaking change: Added `cachedChannel` parameter to `ChatEventHandler::handleChatEvent` [#2807](https://github.com/GetStream/stream-chat-android/pull/2807)

## stream-chat-android-ui-components
### 🐞 Fixed
- Users' updates done in runtime are now propagated to the `MessageListView` component. [#2769](https://github.com/GetStream/stream-chat-android/pull/2769)
- Fixed the display of image attachments on the pinned message list screen. [#2792](https://github.com/GetStream/stream-chat-android/pull/2792)
-  Button for commands is now disabled in edit mode. [#2812](https://github.com/GetStream/stream-chat-android/pull/2812)
- Small bug fix for borders of attachments

### ⬆️ Improved
- Improved Korean 🇰🇷 and Japanese 🇯🇵 translation.
- Improved KDocs of UI components such as `ChannelListHeaderView` and `AvatarView`.

### ✅ Added
- Added header with back button and attachment's title to `AttachmentMediaActivity` which displays playable attachments.
  You can customize its appearance using `streamUiMediaActivityHeader`, `streamUiMediaActivityHeaderLeftActionButtonStyle` and `streamUiMediaActivityHeaderTitleStyle` attributes.
- Added `hard` flag to `MessageListViewModel.Event.DeleteMessage`.
  You can use `MessageListView::setMessageDeleteHandler` and pass `MessageListViewModel.Event.DeleteMessage(MESSAGE, hard = true)` to hard delete messages using `MessageListViewModel`.
  Check [MessageListViewModelBinding](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/kotlin/io/getstream/chat/android/ui/message/list/viewmodel/MessageListViewModelBinding.kt#L37) for further details. [#2772](https://github.com/GetStream/stream-chat-android/pull/2772)
- Rtl support was added. If the app has `android:supportsRtl="true"` and the locale of the device needs Rtl support, the SDK will draw the components from the right-to-left instead the default way (left-to-right) [#2799](https://github.com/GetStream/stream-chat-android/pull/2799)

### ⚠️ Changed
- Constructor of `ChannelListViewModel` and `ChannelListViewModelFactory` changed. Now they ask for `ChatEventHandlerFactory` instead `ChatEventHandler`, so users can use `StateFlow<List<Channel>>` in their implementations of `ChatEventHandler`, which can make implementation smarter with resources (don't try to add a channel that is already there, for example) [#2747](https://github.com/GetStream/stream-chat-android/pull/2747)

### ❌ Removed

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the message grouping logic to now include date separators when splitting message groups [#2770](https://github.com/GetStream/stream-chat-android/pull/2770)

### ⬆️ Improved
- Improved the UI for message footers to be more respective of thread replies [#2765](https://github.com/GetStream/stream-chat-android/pull/2765)
- Fixed the orientation and UI of ThreadParticipants [#2765](https://github.com/GetStream/stream-chat-android/pull/2765)
- Improved the API structure more, made the components package more clear [#2795](https://github.com/GetStream/stream-chat-android/pull/2795)
- Improved the way to customize the message item types and containers [#2791](https://github.com/GetStream/stream-chat-android/pull/2791)
- Added more parameters to the stateless version of the MessageComposer for consistency [#2809](https://github.com/GetStream/stream-chat-android/pull/2809)
- Added color and shape parameters to `MessageListHeader` and `ChannelListHeader` components [#2855](https://github.com/GetStream/stream-chat-android/pull/2855)

### ✅ Added
- Added site name labels to link attachments for websites using the Open Graph protocol [#2785](https://github.com/GetStream/stream-chat-android/pull/2785)
- Added preview screens for file attachments [#2764](https://github.com/GetStream/stream-chat-android/pull/2764)
- Added a way to disable date separator and system message items in the message list [#2770](https://github.com/GetStream/stream-chat-android/pull/2770)
- Added an option to the message options menu to unmute a user that sent the message. [#2787](https://github.com/GetStream/stream-chat-android/pull/2787)
- Added a `DefaultMessageContainer` component that encapsulates all default message types [#2791](https://github.com/GetStream/stream-chat-android/pull/2791)
- Added the `SelectedReactionsMenu` component that represents a list of user reactions left for a particular message [#2782](https://github.com/GetStream/stream-chat-android/pull/2782)

### ⚠️ Changed
- Removed SelectedMessageOverlay and replaced it with SelectedMessageMenu - [#2768](https://github.com/GetStream/stream-chat-android/pull/2768)
- Big changes to the structure of the project, making it easier to find all the components and building blocks - [#2752](https://github.com/GetStream/stream-chat-android/pull/2752)
- Renamed the `common` package to `components` and added a logical structure to the components there
- Decoupled many smaller components to the `components` package and their individual files, for ease of use
- Improved the API of several smaller components
- Added a few missing previews
- Changed various component names, removed unused/redundant component blocks and moved to Default components [#2795](https://github.com/GetStream/stream-chat-android/pull/2795)
- Changed some of the component types regarding the message item [#2791](https://github.com/GetStream/stream-chat-android/pull/2791)
- Moved message item components to `components.messages` [#2791](https://github.com/GetStream/stream-chat-android/pull/2791)
- When querying for more channels, `ChannelListViewModel` now uses `OfflinePlugin` based approach if it is enabled. [#2790](https://github.com/GetStream/stream-chat-android/pull/2790)
- Updated [MessageListHeader Documentation](https://getstream.io/chat/docs/sdk/android/compose/message-components/message-list-header/) [#2855](https://github.com/GetStream/stream-chat-android/pull/2855)

### ❌ Removed
- Removed some redundant components from separate files and the `components` package [#2795](https://github.com/GetStream/stream-chat-android/pull/2795)

# December 9th, 2021 - 4.24.0
## stream-chat-android-offline
### 🐞 Fixed
- Fix the issue when users' data can be outdated until restart SDK.

### ✅ Added
- Added new extension function `ChatClient::keystroke`.
- Added new extension function `ChatClient::stopTyping`.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed `MessageInputFieldView#mode` not being reset after custom attachments were cleared

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed crash related with creation of MessageOptionsDialogFragment
- Fixed behaviour related to search messages, when message was not already loaded from database MessageListView could not scroll to searched message.
- Removed cut from text when text end with Italic
- Fixed `GiphyViewHolderStyle#cardBackgroundColor` not getting applied
- Fixed bug related of not removing channels when filter selects channels where the the current user is not a member

### ⬆️ Improved
- Replied messages now have a limit for size. The text will get cut if there's too many characters or too many line breaks.
- Improved Korean 🇰🇷 translations.

### ✅ Added
- Added scroll to original message when clicking in a reply message. Use `ReplyMessageClickListener` to change the behaviour of click in reply messages.

## stream-chat-android-compose
### 🐞 Fixed
- Removed preemptive attachment loading that was resulting in crashes on certain Android API versions
- Fixed incorrect message shape for theirs messages in threads.

### ⬆️ Improved
- Minor UI improvements to the message overlay
- Enabled scrolling behavior in SelectedMessageOverlay

### ✅ Added
- Added the mention suggestion popup to the `MessageComposer` component, that allows to autocomplete a mention from a list of users.
- Added support for slowdown mode. Users are no longer able to send messages during the cooldown interval.
- Added support for system messages.
- Added support for Giphy command.
- Added message pinning to the list of message options
- Added pinned message UI
- Added a checkbox to the `MessageComposer` component, that allows to display a thread message in the parent channel.
- Added an option to flag a message to the message options overlay.

### ⚠️ Changed
- Changed the way focus state works for focused messages.
- Added the Pin type to the MessageAction sealed class
- Renamed a bunch of state classes for Compose component, to have the `State` prefix, general renaming, imports and other quality of life improvements
- Renamed `ReactionOption` state wrapper to `ReactionOptionItemState`
- Renamed `MessageListItem` state wrapper to `MessageListItemState` and its children now have a `State` suffix
- Renamed `AttachmentItem` state wrapper to `AttachmentPickerItemState`
- Renamed `MessageInputState` to `MessageComposerState`
- Renamed `MessageOption` to `MessageOptionState`
- Renamed `defaultMessageOptions()` to `defaultMessageOptionsState()`


# November 25th, 2021 - 4.23.0
## Common changes for all artifacts
### ⬆️ Improved
- Improved logs for errors in the SDK.

## stream-chat-android-offline
### 🐞 Fixed
- Deprecated `QueryChannelsController::mutedChannelsIds`. Use `ChatDomain.mutedChannels` instead
- Fix issue when sent attachments from Android SDK don't show title in iOS.

### ✅ Added
- Added new extension function `ChatClient::replayEventsForActiveChannels`.
- Added new extension function `ChatClient::setMessageForReply`.
- Added new extension function `ChatClient::downloadAttachment` to download attachments without `ChatDomain`.

## stream-chat-android-ui-common
### ✅ Added
- Made `ThreeTenInitializer` public to allow manual invocations of it. See the new [documentation](https://getstream.io/chat/docs/sdk/android/ui/guides/app-startup-initializers/) for more details.

## stream-chat-android-ui-components
### 🐞 Fixed
- Removed ripple effect for attachments in message options.
### ⬆️ Improved
- More customization for AvatarView. Now it is possible to choose between Square and Circle. Use new fields in AvatarStyle to customize AvatarView the way you prefer. 
### ✅ Added
- Added setter `MessageListView.setMessageBackgroundFactory` to set a factory to provide a background for messages. 
- Added `MessageInputViewModel::sendMessageWithCustomAttachments` function allowing to send message with custom attachments list.
- Added `MessageInputView::submitCustomAttachments` function allowing setting custom attachments in `MessageInputView`.
- Added `SelectedCustomAttachmentViewHolderFactory` interface and `BaseSelectedCustomAttachmentViewHolder`class allowing defining how previews of custom attachments in `MessageInputView` should be rendered.

### ⚠️ Changed
- Added `MessageSendHandler::sendMessageWithCustomAttachments` and `MessageSendHandler::sendToThreadWithCustomAttachments` allowing to intercept sending custom attachments actions.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed the information about channel members shown in the `MessageListHeader` subtitle.
- Fixed the bug where the channel icon did not appear because of a lengthy title.

### ⬆️ Improved
- Updated a lot of documentation around the Messages features
- Improved the subtitle text in the `MessageListHeader` component.
- Now, the `MessageComposer` component supports sending `typing.start` and `typing.stop` events when a user starts or stops typing.
- Made the `ChannelNameFormatter`, `ClipboardHandler` and `MessagePreviewFormatter` interfaces functional for ease of use.
- Now, an error Toast is shown when the input in the `MessageComposer` does not pass validation.

### ✅ Added
- Added the "mute" option to the `ChannelInfo` action dialog.
- Added a wrapper for the message input state in the form of `MessageInputState`
- Added `attachmentsContentImageWidth`, `attachmentsContentImageHeight`, `attachmentsContentGiphyWidth`, `attachmentsContentGiphyHeight`, `attachmentsContentLinkWidth`, `attachmentsContentFileWidth` and `attachmentsContentFileUploadWidth` options to `StreamDimens`, to make it possible to customize the dimensions of attachments content via `ChatTheme`.
- Added a thread separator between a parent message and thread replies.
- Added the `threadSeparatorGradientStart` and `threadSeparatorGradientEnd` options to `StreamColors`, to make it possible to customize the thread separator background gradient colors via `ChatTheme`.
- Added the `threadSeparatorVerticalPadding` and `threadSeparatorTextVerticalPadding` options to `StreamDimens`, to make it possible to customize the dimensions of thread separator via `ChatTheme`.
- Added a typing indicator to the `MessageListHeader` component. 
- Added the `messageOverlayActionItemHeight` option to `StreamDimens`, to make it possible to customize the height of an action item on the selected message overlay via `ChatTheme`.
- Added the `messageAlignmentProvider` field to the `ChatTheme` that allows to customize message horizontal alignment. 
- Added the `maxAttachmentCount` and `maxAttachmentSize` parameters to the `MessagesViewModelFactory`, to make it possible to customize the allowed number and size of attachments that can be sent via the `MessageComposer` component.
- Added the `textStyle` and `textColor` parameters to the `NetworkLoadingView` component, to make it possible to customize the text appearance of the inner text.

### ⚠️ Changed
- Made the MessageMode subtypes to the parent class, to make it easier to understand when importing
- Renamed the MessageMode.Thread to MessageMode.MessageThread for clarity
- Changed the signature of the MessageComposer to accommodate for the `MessageInputState`
- Moved common state to the `io.getstream.chat.android.common` package
- Made the `AttachmentFactory.previewContent` field nullable.
- Exposed `MessageReactions` as a public component so users can use it to display a message reactions bubble in their custom UI.
- Changed the type of the inner channel items in the `ChannelsState` class from `Channel` to `ChannelItem`.


# November 11th, 2021 - 4.22.0
## Common changes for all artifacts
### ⬆️ Improved
- Bumped the SDKs target API to 31
- Updated WorkManager to version 2.7.0, which fixes compatibility issues with SDK 31

### ✅ Added
- Added Indonesian :indonesia: translations.
- Added `onErrorSuspend` extension for `Result` to allow executing suspending lambda function for handing error response.

## stream-chat-android
### ✅ Added
- Added `ChannelListItemAdapter::getChannels()` for getting a list of channels

## stream-chat-android-client
### ✅ Added
- Added `NotificationConfig::shouldShowNotificationOnPush` that allows enabling/disabling showing notification after receiving a push message

### ⚠️ Changed
- `NotificationConfig::pushNotificationsEnabled` is now disabled by default if you don't provide custom `NotificationConfig` - our SDK won't create a `NotificationChannel` if push notifications are not configured

## stream-chat-android-offline
### 🐞 Fixed
- Fixed inserting messages with empty `Message::cid`

### ✅ Added
- Added new extension function `ChatCliet::requestMembers` to query members without `ChatDomain`.
- Added new extension function `ChatCliet::searchUsersByName`.

### ⚠️ Changed
- 🚨 Breaking change: `RetryPolicy` in `ChatDomain` is now immutable and can only be set with Builder before creating an instance of it.
- 🚨 Breaking change: `ChannelEventsHandler` is renamed to `ChatEventHandler`, it's function is renamed from `onChannelEvent` to `handleChatEvent`, EventHandlingResult is sealed class now. To get more details read [our docs](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#chateventhandler)

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed bug when showing messages with pending attachments that cause loading state to be not shown in some cases.
- Fixed clearing `MessageInputView` after dismissing message to edit
- Fixed support for videos from other SDKs
- Fixed downloading attachments with some special characters in their names

### ⬆️ Improved
- Improved Korean 🇰🇷 translation related to the flagging.
- 🚨 Breaking change: Now the button for sending message in MessageInputView sizes itself accordingly with the drawable used, instead of having a predefined size (32dp)
- Improved KDocs for `MessageListFragment`.

### ✅ Added
- You can now use MessageListView.backgroundDrawable to have more flexibility to customize your message items background. Be aware that setting backgroundDrawable will override the background configurations of xml.
- Added `streamUiEditInputModeIcon` and `streamUiReplyInputModeIcon` attributes to `MessageInputView`.
  Use them to customize icon in the `MessageInputView's` top left corner displayed when user edits or replies to the message.
- Added `setMessageInputModeListener`, `setSendMessageButtonEnabledDrawable` and `setSendMessageButtonDisabledDrawable` method to `MessageInputView`.
  They can be used together for changing send button icon based on current input mode. See [docs](https://getstream.io/chat/docs/sdk/android/ui/components/message-input#changing-send-message-button) for more details.
- Added static methods `createIntent` and `newInstance` those doesn't have default parameters on `MessageListActivity` and `MessageListFragment` for supporting Java side.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed channel options that are displayed in the `ChannelInfo` component.

### ⬆️ Improved
- Improved the icon set and polished the UI for various Messages features
- Improved the set of customization options for the `DefaultChannelItem`
- Updated documentation for Channels set of features
- Now it is possible to search for distinct channels by member names using `ChannelListViewModel`.
- Improved the design of `ChannelInfo` bottom sheet dialog.

### ✅ Added
- Added a new parameter to the `AttachmentFactory` called `previewContent` that represents attachments within the MessageInput
- Added the `leadingContent`, `detailsContent`, `trailingContent` and `divider` Slot APIs for the `DefaultChannelItem`
- Added `StreamDimens` option to the `ChatTheme`, to allow for dimension customization across the app.
- Added localization support for the components related the channel list.
- Added the `emptySearchContent` parameter to `ChannelList` component that allows to customize the empty placeholder, when there are no channels matching the search query.
- Added support for the muted channel indicator in the message list.
- Added `ChannelNameFormatter` option to the `ChatTheme`, to allow for channel name format customization across the app.
- Added the `textFormatter` field to `AttachmentFactory`, to allow for attachment text format customization.
- Added `MessagePreviewFormatter` option to the `ChatTheme`, to allow for message preview text format customization across the app.
- Added the `leadingContent`, `headerContent`, `footerContent`, `trailingContent` and `content` Slot APIs for the `DefaultMessageItem`
- Added `channelInfoUserItemWidth`, `channelInfoUserItemHorizontalPadding` and `channelInfoUserItemAvatarSize` options to `StreamDimens`, to make it possible to customize the dimensions inside the `ChannelInfo` component via `ChatTheme`.
- Added `ownMessagesBackground`, `otherMessagesBackground` and `deletedMessagesBackgroundColor` options to `StreamColors`, to make it possible to customize the message bubble color via `ChatTheme`.

### ⚠️ Changed
- The `AttachmentFactory` now requires an additional parameter - `previewContent` that's used to preview the attachment within the MessageInput, so please be aware of this!
- Renamed `ChannelOption.icon` property to `ChannelOption.iconPainter` and changed the property type from `ImageVector` to `Painter`.
- Changed the type of the `ChannelListViewModel.selectedChannel` field to `MutableState<Channel?>`.

# October 27th, 2021 - 4.21.0
## Common changes for all artifacts
### ⬆️ Improved
- Improved Korean 🇰🇷 translations.

### ✅ Added
- Added `ChatDomain.connectionState` that exposes 3 states: `CONNECTED`, `CONNECTING` and `OFFLINE`.
  `ChannelListHeaderView` and `MessageListHeaderView` show different title based on newly introduced connection state.
  `ChatDomain.online` is now deprecated - use `ChatDomain.connectionState` instead.

## stream-chat-android-client
### ⬆️ Improved
- Added KDocs for `Result` properties and methods.

### ✅ Added
- The `UserCredentialStorage` interface was added to `ChatClient`. You can set your own implementation via `ChatClient.Builder::credentialStorage`

### ⚠️ Changed
- 🚨 Breaking change: Config property `isRepliesEnabled` is renamed to `isThreadEnabled` to avoid misleading. Now it toggles only thread feature.

### ❌ Removed
- `androidx-security-crypto` dependency was removed. Now, the user's token storage uses private shared preferences by default.

## stream-chat-android-offline
### 🐞 Fixed
- Fix bug when ChannelEventsHandler was not used even if it was set in QueryChannelsController

### ⬆️ Improved
- Channel gets removed from `QueryChannelsController` when receive `ChannelHiddenEvent`

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed position of reactions. Now the reactions adapts its starting position to fit entirely in the screen. 
- 🚨 Breaking change: Fixing positions of reactions in edit reactions dialog. Using a GridLayoutManager instead of LinearLayoutManager, so now there's box with all reactions instead of a scrollable list. The way to customize the box is a bit different, then a breaking change was inserted in this feature. 
- Made it impossible to send a message during the cooldown interval in slow mode.

### ⬆️ Improved
- Better position for icon of failed message
- Small improvement for information update in messages. The ViewHolders only update the information that had a change.

### ✅ Added
- Added `streamUiMaxAttachmentsCount` attribute to `MessageInputView` to allow customizing the maximum number of attachments in the single message.
The maximum attachments count cannot be greater than 10. Default value: 10.
- Added `streamUiMessageMaxWidthFactorMine` and `streamUiMessageMaxWidthFactorTheirs` `MessageListView` attributes. You can adjust messages width by passing values in [75% - 100%] range.
- Added `MessageInputView::setAttachmentButtonClickListener` that helps you to override click listener for the attachment button.
- Added `MessageInputView::submitAttachments` method to set attachments in `MessageInputView` to be sent with a message.

### ⚠️ Changed
- Feature of replied messages can be enabled/disabled only locally via SDK. `Thread` dashboard flag toggles only thread feature.

## stream-chat-android-compose
### ⬆️ Improved
- Added a way to customize the app font family, by passing in a parameter to `StreamTypography.defaultTypography()`
- Improved permission handling for the `AttachmentsPicker` to handle only the required permissions
- `ThreadParticipants` is now public and can be used for your custom UI.

### ✅ Added
- `ThreadParticipants` component now has a `text: String` parameter allowing customizing the thread label.
- Added unread message count indicators to ChannelItems to show users more info about their channels

### ⚠️ Changed
- `CAMERA` permission is no longer required to be declared in the App Manifest, because we don't use it

### ❌ Removed
- Removed `CAMERA` permission requirement, because we don't use internal camera preview, we request a 3rd party app
- Removed `CAMERA` permission checks if the user doesn't require the permission in their app


# October 18th, 2021 - 4.20.0
## Common changes for all artifacts
### ⬆️ Improved
- Upgraded Kotlin version to 1.5.30
- Make our SDK compile-friendly with TargetSDK 31
- Upgraded Coil version to [1.4.0](https://github.com/coil-kt/coil/releases/tag/1.4.0)

### ⚠️ Changed
- 🚨 Breaking change: `ProgressCallback` is not invoked on main thread anymore. So make sure to handle it if you were previously using this callback to update the UI directly.
- Attachment#uploadState is now updated in real-time during uploads.

### ❌ Removed
- Removed `ProgressTrackerFactory` and `ProgressTracker` in favour of new progress tracking implementation.

## stream-chat-android
### ✅ Added
- Push Notification uses `MessagingStyle` on devices with API Version 23+
- Push Notification configuration has been simplified, check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#customizing-push-notifications) to see how it works
- `NotificationHandler` interface allows you to implement your own Push Notification logic show/remove notifications. It is the new interface you need to use if you were using `ChatNotificationHandler` previously
- `NotificationHandlerFactory` help you to use our default `NotificationHandler` implementations

### ⚠️ Changed
- Some properties of `NotificationConfig` has been deprecated, check our [DEPRECATIONS](https://github.com/GetStream/stream-chat-android/blob/main/DEPRECATIONS.md) section
- `ChatNotificationhandler` class has been deprecated, you need to use `NotificationHandler` now. Check our [DEPRECATIONS](https://github.com/GetStream/stream-chat-android/blob/main/DEPRECATIONS.md) section.

## stream-chat-android-client
### 🐞 Fixed
- Fixed issues with Proguard stripping response classes incorrectly

### ⬆️ Improved
- Added KDocs for `ChatClient.Builder` methods.
- `ChatClient` now defaults to using the `https://chat.stream-io-api.com` base URL, using [Stream's Edge API Infrastructure](https://getstream.io/blog/chat-edge-infrastructure/) instead of connecting to a region-specific API. If you're not on a dedicated chat infrastructure, remove any region-specific base URL settings from the `ChatClient.Builder` to use Edge instead.

### ✅ Added
- 🚨 Breaking change: A new `Idle` state is added to `Attachment.UploadState`.
- Added a new callback function `onProgress(bytesUploaded: Long, totalLength: Long)` in `ProgressCallback`.
- Added the possibility to add your own instance of OkHttpClient with `ChatClient.okHttpClient`.

### ⚠️ Changed
- 🚨 Breaking change: `Attachment.UploadState.InProgress` now is data class having two fields, `bytesUploaded: Long` and `totalBytes: Long` instead of object.
- Deprecated the `ChatClient.Builder#cdnUrl` method. To customize file uploads, set a custom `FileUploader` implementation instead. More info in the documentation: [Using Your Own CDN](https://getstream.io/chat/docs/android/file_uploads/?language=kotlin#using-your-own-cdn).

## stream-chat-android-offline
### 🐞 Fixed
- Fixed infinite loading of message if any of its attachments uploading was failed

### ✅ Added
- `ChannelEventsHandler` is added to `QueryChannelsController` to handle updating channel list logic after receiving events. You can provide custom `ChannelEventsHandler` through `ChannelListViewModel` or using `QueryChannelsController` directly.

### ⚠️ Changed
- `QueryChannelsController::newChannelEventFilter` and `QueryChannelsController#checkFilterOnChannelUpdatedEvent` are now deprecated. See the deprecation log for more details.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed PDF attachments previews

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed bug related to scroll of messages.
- Updating attachments view holder only when attachments have changed. This fixes a problem with reloading gifs when reactions are added or removed.
- Fixing ViewReactionsView being cropped if more than 7 reactions are added
- Fix bug using custom attributes into views inflated into our SDK Views

### ⬆️ Improved
- Now it is possible to set a custom `LinearLayoutManager` to `MessageListView`, this can be used to change stack of messages or revert the layout.
- Removed full screen loading view when loading more message items on the `SearchResultListView`.

### ✅ Added
- Added `MessageListView::getRecyclerView` method which exposes the inner `RecyclerView` with message list items.
- Added `MessageListView::setUserReactionClickListener` method to set a listener used when a reaction left by a user is clicked on the message options overlay.
- Added attr `streamUiScrollButtonElevation` to set the elevation of scroll button ot `MessageListView` 
### ⚠️ Changed
- `ChatUI.uiMode` has been deprecated. If you want to force Dark/Light theme, you need to use `AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO|AppCompatDelegate.MODE_NIGHT_YES)`

### ❌ Removed
- `android.permission.CAMERA` from our Manifest. This permission is not required anymore.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where attachments weren't properly stored when editing a message

### ⬆️ Improved
- Updated the Compose framework version (1.0.3)
- Updated the Accompanist libraries version (0.19.0)
- Improved overlays in all components, to match the same design and opacity
- Added smaller animations to the AttachmentPicker in the MessagesScreen
- General improvements in the Attachments API and the way we build different attachments
- Allowed for better long clicks on attachments
- Improved the experience of creating the MessagesViewModelFactory with default arguments
- Updated and cleaned up Channel screen design
- Improved logic for updating the `lastSeenMessage` for fewer calculations

### ✅ Added
- Added DateSeparator items to Messages to group up messages by their creation date
- Added an `overlayDark` color for date separators and similar UI components

### ⚠️ Changed
- Removed AttachmentPicker option when editing messages
- Removed Attachment previews when editing messages with attachments
- Improved the ease of use of the AttachmentState API by keeping it state & actions only
- Moved the `modifier` parameter outside of the AttachmentState to the AttachmentFactory
- Updated Attachments to hold `Message` items instead of `MessageItem`s
- Changed the type of the `onLastVisibleMessageChanged` parameter to `Message` for ease of use
- Changed the parameter type of `itemContent` in `MessageList` and `Messages` to `MessageListItem`
- Renamed `onScrollToBottom` to `onScrolledToBottom` in `MessageList` and `Messages`
- Made the ChannelListHeader Slot APIs non-nullable so they're always provided, also made them an extension of the RowScope for ease of use

# September 15th, 2021 - 4.19.0
## Common changes for all artifacts
### ✅ Added
- Create new artifact to integrate Huawei Push Kit with Stream. You will need to add  `stream-chat-android-pushprovider-huawei` artifact to your App. Check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei) for further details.

## stream-chat-android
### ✅ Added
- Added a method to dismiss all notifications from a channel. It is handled internally from the SDK but you are able to dismiss channel notification at whatever time calling `ChatClient::dismissChannelNotifications`
- Notifications are dismissed after the user logout the SDK

## stream-chat-android-client
### 🐞 Fixed
- Fixed sending messages using `ChatClient::sendMessage` without explicitly specifying the sender user id.
- Fixed sending custom attachments without files to upload
- Fixed deserialization issues when parsing `ChannelTruncatedEvent` and `MessageDeletedEvent` events with an absent user.

### ⬆️ Improved
- Custom attachment types are now preserved after file uploads

### ✅ Added
- Added `hardDelete` field to `MessageDeletedEvent`.

### ⚠️ Changed
- Now it is possible to hard delete messages. Insert a flag `hard = true` in the `ChatClient.deleteMessage` and it will be deleted in the backend. **This action can't be undone!**

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed bug with light mode.
- Removed `streamUiValidTheme`, as we don't support extending our base theme any longer. Please don't extend our base theme and set the `streamUiTheme` in your application theme instead.

## stream-chat-android-ui-components
### ✅ Added
- Notifications are dismissed after the user go into the channel conversation when you are using `MessageListView`
- Added `bubbleBorderColorMine`, `bubbleBorderColorTheirs`, `bubbleBorderWidthMine`, `bubbleBorderWidthTheirs` to `ViewReactionsViewStyle` for customizing reactions` border

## stream-chat-android-compose
### ⬆️ Improved
- Updated the Compose framework version (1.0.2)
- Updated the Accompanist library version (0.18.0)

### ✅ Added
- Added an uploading indicator to files and images
- Images being uploaded are now preloaded from the system
- Upload indicators show the upload progress and how much data is left to send
- Added more image options to the ImagePreviewActivity such as download, delete, reply to message...
- Added an Image Gallery feature to the ImagePreviewActivity where users can browse all the images
- Notifications are dismissed after the user go into the channel conversation when you are using `MessageList`

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
- Changed the `AttachmentState` `message` property name to `messageItem`
- Added an `isFocused` property to `MessageItem`
- Added an `onImagePreviewResult` callback/parameter to various Messages screen components

### ❌ Removed

## stream-chat-android-pushprovider-firebase
### ✅ Added
- Added a `FirebaseMessagingDelegate` class to simplify custom implementations of `FirebaseMessagingService` that forward messages to the SDK. See [Using a Custom Firebase Messaging Service](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service) for more details.

## stream-chat-android-pushprovider-huawei
### ✅ Added
- Added a `HuaweiMessagingDelegate` class to simplify custom implementations of `HmsMessageService` that forward messages to the SDK. See [Using a Custom Huawei Messaging Service](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei#using-a-custom-huawei-messaging-service) for more details.

# September 15th, 2021 - 4.18.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed setting notification's `contentTitle` when a Channel doesn't have the name. It will now show members names instead

### ✅ Added
- Added a new way to paginate through search message results using limit and next/previous values.

### ⚠️ Changed
- Deprecated `Channel#name`, `Channel#image`, `User#name`, `Ues#image` extension properties. Use class members instead.

### ❌ Removed
- Completely removed the old serialization implementation. You can no longer opt-out of using the new serialization implementation.
- Removed the `UpdateUsersRequest` class.

## stream-chat-android-offline
### ⬆️ Improved
- Improving logs for Message deletion error.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed theme for `AttachmentDocumentActivity`. Now it is applied: `Theme.AppCompat.DayNight.NoActionBar`

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the bug when MessageInputView let send a message with large attachments. Such message is never sent.
- Fixed bug related to `ScrollHelper` when `MessageListView` is initialised more than once.

### ⬆️ Improved
- The search for mentions now includes transliteration, diacritics removal, and ignore typos. To use transliteration, pass the id of the desired alphabets to `DefaultStreamTransliterator`, add it to DefaultUserLookupHandler and set it using `MessageInputView.setUserLookupHandler`. Transliteration works only for android API 29. If you like to add your own transliteration use https://unicode-org.github.io/icu/userguide/icu4j/.
- Improved scroll of message when many gif images are present in `MessageListView`

### ✅ Added
- Added scroll behaviour to `MessageListViewStyle`.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where the Message list flickered when sending new messages
- Fixed a few bugs where some attachments had upload state and weren't file/image uploads

### ⬆️ Improved
- Improved the Message list scrolling behavior and scroll to bottom actions
- Added an unread count on the Message list's scroll to bottom CTA
- Improved the way we build items in the Message list
- Added line limit to link attachment descriptions
- Added a way to customize the default line limit for link descriptions
- Improved the `MessageListHeader` with more customization options

### ✅ Added
- Added an uploading indicator to files and images
- Images being uploaded are now preloaded from the system
- Upload indicators show the upload progress and how much data is left to send
- Added UploadAttachmentFactory that handles attachment uploads

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
- Changed the `AttachmentState` `message` property name to `messageItem`
- Added a `Channel` parameter to the `MessagesScreen`'s `onHeaderActionClick` lambda
- Changed the way the `MessageListHeader` is structured by adding slot components

# August 30th, 2021 - 4.17.2
## stream-chat-android-ui-client
### 🐞 Fixed
- Fixed bug which can lead to crash when immediate logout after login

# August 30th, 2021 - 4.17.2
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixes a bug related to incorrect theme of AttachmentActivity.

# August 30th, 2021 - 4.17.1
## Common changes for all artifacts
### ⬆️ Improved
- Now we provide SNAPSHOT versions of our SDK for every commit arrives to the `develop` branch.
  They shouldn't be used for a production release because they could contains some known bugs or breaking changes that will be fixed before a normal version is released, but you can use them to fetch last changes from our SDK
  To use them you need add a new maven repository to your `build.gradle` file and use the SNAPSHOT.
```
 maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
```
Giving that our last SDK version is `X.Y.Z`, the SNAPSHOT version would be `X.Y.(Z+1)-SNAPSHOT`

## stream-chat-android-client
### 🐞 Fixed
- `TooManyRequestsException` caused to be subscribed multiple times to the `ConnectivityManager`

### ⬆️ Improved
- Reconnection process

## stream-chat-android-offline
### ✅ Added
- Added `ChatDomain#Builder#uploadAttachmentsWorkerNetworkType` for customizing `UploadAttachmentsWorker` network type constraint

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed a bug in state handling for anonymous users.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fix for position of deleted messages for other users
- Fix glitch in selectors of file

### ✅ Added
- Added style attributes for `AttachmentGalleryActivity` to control menu options like enabling/disabling reply button etc.
- Now it is possible to customize when the avatar appears in the conversation. It is possible to use an avatar in messages from other users and for messages of the current user. You can check it here:  https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#configure-when-avatar-appears
- Added support for slow mode. Users are no longer able to send messages during cooldown interval.
- Added possibility to customize the appearance of cooldown timer in the `MessageInputView` using the following attributes:
  - `streamUiCooldownTimerTextSize`, `streamUiCooldownTimerTextColor`, `streamUiCooldownTimerFontAssets`, `streamUiCooldownTimerFont`, `streamUiCooldownTimerTextStyle` attributes to customize cooldown timer text
  - `cooldownTimerBackgroundDrawable`- the background drawable for cooldown timer

# August 24th, 2021 - 4.17.0
## Common changes for all artifacts
### ⬆️ Improved
- Updated Target API Level to 30
- Updated dependency versions
  - Coil 1.3.2
  - AndroidX Activity 1.3.1
  - AndroidX Startup 1.1.0
  - AndroidX ConstraintLayout 2.1.0
  - Lottie 4.0.0

## stream-chat-android-client
### 🐞 Fixed
- Fixed a serialization error when editing messages that are replies

### ✅ Added
- Added the `expiration` parameter to `ChatClient::muteChannel`, `ChannelClient:mute` methods
- Added the `timeout` parameter to `ChatClient::muteUser`, `ChannelClient:mute::muteUser` methods

### ⚠️ Changed
- Allow specifying multiple attachment's type when getting messages with attachments:
  - Deprecated `ChatClient::getMessagesWithAttachments` with `type` parameter. Use `ChatClient::getMessagesWithAttachments` function with types list instead
  - Deprecated `ChannelClient::getMessagesWithAttachments` with `type` parameter. Use `ChannelClient::getMessagesWithAttachments` function with types list instead

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed a bug in state handling for anonymous users.

## stream-chat-android-ui-components
### ✅ Added
- Added self-contained higher-level UI components:
  - `ChannelListFragment` - channel list screen which internally contains `ChannelListHeaderView`, `ChannelListView`, `SearchInputView`, `SearchResultListView`.
  - `ChannelListActivity` - thin wrapper around `ChannelListFragment`
  - `MessageListFragment` - message list screen which internally contains `MessageListHeaderView`, `MessageListView`, `MessageInputView`.
  - `MessageListActivity` - thin wrapper around `MessageListFragment`
    Check [ChannelListScreen](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list-screen/) and [MessageListScreen](https://getstream.io/chat/docs/sdk/android/ui/components/message-list-screen/) docs for further details.

## stream-chat-android-compose
### 🐞 Fixed
- Added missing `emptyContent` and `loadingContent` parameters to `MessageList` inner components.
- Fixed a bug where selected File attachment icons were clipped.
- Fixed a bug where image file attachments weren't shown as thumbnails.
- Added an overlay to the `ChannelInfo` that blocks outside clicks.
- Updated the `ChannelInfoUserItem` to use the `UserAvatar`.

### ⬆️ Improved
- Added default date and time formatting to Channel and Message items.
- Improved attachments API by providing cleaner examples of attachment factories.
- Updated documentation & examples.
- Decoupled attachment content to specific attachment files.
- Decoupled message attachment content to a `MessageAttachmentsContent` component.
- Re-structured SDK module to accommodate a new `attachment` package.

### ✅ Added
- Added `DateFormatter` option to the `ChatTheme`, to allow for date format customization across the app.
- Added a `Timestamp` component that encapsulates date formatting.
- Added a way to customize and override if messages use unique reactions.
- Added a `GiphyAttachmentFactory` for GIF specific attachments.
- Added support for loading GIFs using a custom `ImageLoader` for Coil.


# August 12th, 2021 - 4.16.0
## Common changes for all artifacts
### ✅ Added
- Added support for several languages:
  - French
  - Hindi
  - Italian
  - Japanese
  - Korean
  - Spanish
    You can disable them by explicitly setting `resConfigs` inside `build.gradle` file. Check our [docs](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-translations/) for further details.
### ⚠️ Changed
- 🚨 Breaking change: Firebase dependencies have been extracted from our SDK. If you want to continue working with Firebase Push Notification you need to add `stream-chat-android-pushprovider-firebase` artifact to your App
  Check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/) for further details.
- Updated the Kotlin version to latest supported - `1.5.21`.

## stream-chat-android
### 🐞 Fixed
- Fixed markdown links rendering using custom linkify implementation.

## stream-chat-android-client
### ✅ Added
- `PushMessage` class created to store Push Notification data
- `PushDeviceGenerator` interface to obtain the Push Token and create the `Device`

### ⚠️ Changed
- `Device` class has an extra attribute with the `PushProvider` used on this device
- Breaking change: `ChatClient.setDevice()` and `ChatClient.addDevice()` now receive a `device` instance, instead of only receive the push provider token
- `RemoteMessage` from Firebase is not used anymore inside of our SDK, now it needs to be used with `PushMessage` class
- `NotificationConfig` has a new list of `PushDeviceGenerator` instance to be used for generating the Push Notification Token. If you were using `Firebase` as your Push Notification Provider, you need to add `FirebasePushDeviceGenerator` to your `NotificationConfig` object to continue working as before. `FirebasePushDeviceGenerator` receive by constructor the default `FirebaseMessaging` instance to be used, if you would like to use your own instance and no the default one, you can inject it by constructor. Unneeded Firebase properties have been removed from this class.

### ❌ Removed
- 🚨 Breaking change: Remove `ChatClient.isValidRemoteMessage()` method. It needs to be handled outside
- 🚨 Breaking change: Remove `ChatClient.handleRemoteMessage(RemoteMessage)`. Now it needs to be used `ChatClient.handlePushMessage(PushMessage)`

## stream-chat-android-offline
### 🐞 Fixed
- Fixed the event sync process when connection is recovered

## stream-chat-android-ui-common
### ❌ Removed
- Removed unnecessary "draft" filter from the default channel list filter as it is only relevant to the sample app

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attachments of camera. Now multiple videos and pictures can be taken from the camera.
- Added the possibility to force light and dark theme. Set it in inside ChatUI to make all views, fragments and activity of the SDK light.
- Fixed applying style to `SuggestionListView` when using it as a standalone component. You can modify the style using `suggestionListViewTheme` or `TransformStyle::suggestionListStyleTransformer`
- Fixed markdown links rendering using custom linkify implementation.

### ✅ Added
- Added `MessageListView::setDeletedMessageListItemPredicate` function. It's responsible for adjusting visibility of the deleted `MessageListItem.MessageItem` elements.
- Added `streamUiAttachmentSelectionBackgroundColor` for configuring attachment's icon background in `AttachmentSelectionDialogFragment`
- Added `streamUiAttachmentSelectionAttachIcon` for configuring attach icon in `AttachmentSelectionDialogFragment`
- Added support for pinned messages:
  - Added a button to pin/unpin a message to the message options overlay
  - Added `MessageListView::setMessagePinHandler` and `MessageListView::setMessageUnpinHandler` methods to provide custom handlers for aforementioned button
  - Added `PinnedMessageListView` to display a list of pinned messages. The view is supposed to be used with `PinnedMessageListViewModel` and `PinnedMessageListViewModelFactory`
- Possibility to transform MessageItems before the are displayed in the screen.
  Use the `MessageListView.setMessageItemTransformer` for make the necessary transformation. This example makes groups of messages if they were created less than one hour apart:
```
binding.messageListView.setMessageItemTransformer { list ->
  list.mapIndexed { i, messageItem ->
        var newMessageItem = messageItem

        if (i < list.lastIndex) {
            val nextMessageItem = list[i + 1]

            if (messageItem is MessageListItem.MessageItem &&
                nextMessageItem is MessageListItem.MessageItem
            ) {
                val thisInstant = messageItem.message.createdAt?.time?.let(Instant::ofEpochMilli)
                val nextInstant = nextMessageItem.message.createdAt?.time?.let(Instant::ofEpochMilli)

                if (nextInstant?.isAfter(thisInstant?.plus(1, ChronoUnit.HOURS)) == true) {
                    newMessageItem = messageItem.copy(positions = listOf(MessageListItem.Position.BOTTOM))
                } else {
                    newMessageItem =
                        messageItem.copy(positions = messageItem.positions - MessageListItem.Position.BOTTOM)
                }
            }
        }

        newMessageItem
    }
}
```
- Added possibility to customize the appearance of pinned message in the`MessageListView` using the following attributes:
  - `streamUiPinMessageEnabled` - attribute to enable/disable "pin message" feature
  - `streamUiPinOptionIcon` - icon for pin message option
  - `streamUiUnpinOptionIcon` - icon for unpin message option
  - `streamUiPinnedMessageIndicatorTextSize`, `streamUiPinnedMessageIndicatorTextColor`, `streamUiPinnedMessageIndicatorTextFontAssets`, `streamUiPinnedMessageIndicatorTextFont`, `streamUiPinnedMessageIndicatorTextStyle` attributes to customize "pinned by" text
  - `streamUiPinnedMessageIndicatorIcon` - icon in the message list indicating that a message was pinned
  - `streamUiPinnedMessageBackgroundColor` - the background color of a pinned message in the message list
- Added possibility to customize `PinnedMessageListView` style using `streamUiPinnedMessageListStyle` theme attribute or `TransformStyle.pinnedMessageListViewStyleTransformer`. The list of available style attributes can be found in `attrs_pinned_message_list_view.xml`. The default style for `PinnedMessageListView` is `StreamUi.PinnedMessageList`.

### ⚠️ Changed
- 🚨 Breaking change: the deleted `MessageListItem.MessageItem` elements are now displayed by default to all the users. This default behavior can be customized using `MessageListView::setDeletedMessageListItemPredicate` function. This function takes an instance of `MessageListItemPredicate`. You can pass one of the following objects:
  * `DeletedMessageListItemPredicate.VisibleToEveryone`
  * `DeletedMessageListItemPredicate.NotVisibleToAnyone`
  * or `DeletedMessageListItemPredicate.VisibleToAuthorOnly`
    Alternatively you can pass your custom implementation by implementing the `MessageListItemPredicate` interface if you need to customize it more deeply.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where we didn't use the `Channel.getDisplayName()` logic for the `MessageListHeader`.
- Fixed a bug where lazy loading for `Channel`s wasn't working consistently

### ⬆️ Improved
- Updated Jetpack Compose to `1.0.1`
- Updated Accompanist libraries to `0.16.1`
- Updated KTX Activity to `1.3.1`
- Exposed functionality for getting the `displayName` of `Channel`s.
- Added updated logic to Link preview attachments, which chooses either the `titleLink` or the `ogUrl` when loading the data, depending on which exists .

### ✅ Added
- Added the `emptyContent` and `loadingContent` parameters to `ChannelList` and `MessageList` components. Now you can customize the UI of those two states.
- Added lots of improvements to Avatars - added a `UserAvatar`, `ChannelAvatar` and an `InitialsAvatar` to load different types of data.
- We now show a matrix of user images in case we're in a group DM.
- We also show initials in case the user doesn't have an image.
- Added a way to customize the leading content in the `ChannelListHeader`.

### ⚠️ Changed
- `ViewModel`s now initialize automatically, so you no longer have to call `start()` on them. This is aimed to improve the consistency between our SDKs.
- Added a `Shape` parameter to `Avatar` to customize the shape.
- The `User` parameter in the `ChannelListHeader` is nullable and used to display the default leading content.

## stream-chat-android-pushprovider-firebase
### ✅ Added
- Create this new artifact. To use Firebase Push Notification you need do the following steps:
  1. Add the artifact to your `build.gradle` file -> `implementation "io.getstream:stream-chat-android-pushprovider-firebase:$streamVersion"`
  2. Add `FirebaseDeviceGenerator` to your `NotificationConfig`
        ```
            val notificationConfig = NotificationConfig(
                [...]
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
                )
        ```


# August 5th, 2021 - 4.15.1
## stream-chat-android-client
### ⬆️ Improved
- Improved `ChatClient::pinMessage` and `ChatClient::unpinMessage`. Now the methods use partial message updates and the data in other `Message` fields is not lost.

### ✅ Added
- Added `Channel::isMutedFor` extension function which might be used to check if the Channel is muted for User
- Added `ChatClient::partialUpdateMessage` method to update specific `Message` fields retaining the other fields

## stream-chat-android-offline
### 🐞 Fixed
- Fixed updating `ChannelController::muted` value

### ⬆️ Improved
- The following `Message` fields are now persisted to the database: `pinned`, `pinnedAt`, `pinExpires`, `pinnedBy`, `channelInfo`, `replyMessageId`.

## stream-chat-android-ui-components
### 🐞 Fixed
- Added a fix for default view for empty state of ChannelListView.
- Fixed memory leaks for FileAttachmentsView.

### ✅ Added
- Added `MessageListItem.ThreadPlaceholderItem` and corresponding `THREAD_PLACEHOLDER` view type which can be used to implement an empty thread placeholder.
- Added `authorLink` to `Attachment` - the link to the website

### ❌ Removed
- Removed `UrlSigner` class

## stream-chat-android-compose
### ⬆️ Improved
- Exposed `DefaultMessageContainer` as a public component so users can use it as a fallback
- Exposed an `isMine` property on `MessageItem`s, for ease of use.
- Allowed for customization of `MessageList` (specifically `Messages`) component background, through a `modifier.background()` parameter.
- Allowed for better message customization before sending the message.

### ⚠️ Changed
- Moved permissions and queries from the compose sample app `AndroidManifest.xml` to the SDK `AndroidManifest.xml` so users don't have to add permissions themselves.
- Changed the exposed type of the `MessageComposer`'s `onSendMessage` handler. This way people can customize messages before we send them to the API.

### ❌ Removed
- Removed `currentUser` parameter from `DefaultMessageContainer` and some other components that relied on ID comparison to know which message is ours/theirs.
- Removed default background color on `Messages` component, so that users can customize it by passing in a `modifier`.


# July 29th, 2021 - 4.15.0
## New Jetpack Compose UI Components 🎉

Starting from this release, we have a new `stream-chat-android-compose` artifact that contains a UI implementation for Chat built in Jetpack Compose.

The new artifact is available as a beta for now (note the postfix in the version number):

```groovy
implementation "io.getstream:stream-chat-android-compose:4.15.0-beta"
```

Learn more in the [announcement blog post](https://getstream.io/blog/jetpack-compose-sdk/), check out the [documentation of the Compose UI Components](https://getstream.io/chat/docs/sdk/android/compose/overview/), and try them today with the [Compose Chat tutorial](https://getstream.io/chat/compose/tutorial/)!

## Common changes for all artifacts

### 🐞 Fixed
- Fixed adding `MessageListItem.TypingItem` to message list

### ⬆️ Improved
- ⚠ Downgraded Kotlin version to 1.5.10 to support Jetpack Compose
- Removed AndroidX Media dependency
- Updated dependency versions
  - Coil 1.3.0
  - AndroidX Activity 1.3.0
  - AndroidX AppCompat 1.3.1
  - Android Ktx 1.6.0
  - AndroidX RecyclerView 1.2.1
  - Kotlin Coroutines 1.5.1
  - Dexter 6.2.3
  - Lottie 3.7.2

## stream-chat-android-client
### ⬆️ Improved
- Improved the names of properties in the `Config` class

## stream-chat-android-ui-common
### ✅ Added
Now it is possible to style the AttachmentActivity. Just replace the activity's theme
in your Manifest file:

```
<activity
    android:name="io.getstream.chat.android.ui.gallery.AttachmentActivity"
    android:theme="@style/yourTheme"
    tools:replace="android:theme"
    />
```

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed "operator $ne is not supported for custom fields" error when querying channels

### ✅ Added
- Now you can configure the style of `MessageListItem`. Added:
  - streamUiMessageTextColorThreadSeparator
  - streamUiMessageTextFontThreadSeparator
  - streamUiMessageTextFontAssetsThreadSeparator
  - streamUiMessageTextStyleThreadSeparator
  - streamUiMessageTextSizeLinkLabel
  - streamUiMessageTextColorLinkLabel
  - streamUiMessageTextFontLinkLabel
  - streamUiMessageTextFontAssetsLinkLabel
  - streamUiMessageTextStyleLinkLabel
  - streamUiMessageListLoadingView
  - streamUiEmptyStateTextSize
  - streamUiEmptyStateTextColor
  - streamUiEmptyStateTextFont
  - streamUiEmptyStateTextFontAssets
  - streamUiEmptyStateTextStyle

- Now you can configure the style of `AttachmentMediaActivity`
- Added `streamUiLoadingView`, `streamUiEmptyStateView` and `streamUiLoadingMoreView` attributes to `ChannelListView` and `ChannelListViewStyle`
- Added possibility to customize `ChannelListView` using `streamUiChannelListViewStyle`. Check `StreamUi.ChannelListView` style
- Added `edgeEffectColor` attribute to `ChannelListView` and `ChannelListViewStyle` to allow configuring edge effect color
- Added possibility to customize `MentionListView` style via `TransformStyle.mentionListViewStyleTransformer`
- Added `streamUiSearchResultListViewStyle` attribute to application to customize `SearchResultListView`. The attribute references a style with the following attributes:
  - `streamUiSearchResultListSearchInfoBarBackground` - background for search info bar
  - `streamUiSearchResultListSearchInfoBarTextSize`, `streamUiSearchResultListSearchInfoBarTextColor`, `streamUiSearchResultListSearchInfoBarTextFont`, `streamUiSearchResultListSearchInfoBarTextFontAssets`, `streamUiSearchResultListSearchInfoBarTextStyle` attributes to customize text displayed in search info bar
  - `streamUiSearchResultListEmptyStateIcon` - icon for empty state view
  - `streamUiSearchResultListEmptyStateTextSize`, `streamUiSearchResultListEmptyStateTextColor`, `streamUiSearchResultListEmptyStateTextFont`, `streamUiSearchResultListEmptyStateTextFontAssets`, `streamUiSearchResultListEmptyStateTextStyle` attributes to customize empty state text
  - `streamUiSearchResultListProgressBarIcon` - animated progress drawable
  - `streamUiSearchResultListSenderNameTextSize`, `streamUiSearchResultListSenderNameTextColor`, `streamUiSearchResultListSenderNameTextFont`, `streamUiSearchResultListSenderNameTextFontAssets`, `streamUiSearchResultListSenderNameTextStyle` attributes to customize message sender text
  - `streamUiSearchResultListMessageTextSize`, `streamUiSearchResultListMessageTextColor`, `streamUiSearchResultListMessageTextFont`, `streamUiSearchResultListMessageTextFontAssets`, `streamUiSearchResultListMessageTextStyle` attributes to customize message text
  - `streamUiSearchResultListMessageTimeTextSize`, `streamUiSearchResultListMessageTimeTextColor`, `streamUiSearchResultListMessageTimeTextFont`, `streamUiSearchResultListMessageTimeTextFontAssets`, `streamUiSearchResultListMessageTimeTextStyle` attributes to customize message time text
- Added possibility to customize `SearchResultListView` style via `TransformStyle.searchResultListViewStyleTransformer`
- Added `streamUiTypingIndicatorViewStyle` attribute to application to customize `TypingIndicatorView`. The attribute references a style with the following attributes:
  - `streamUiTypingIndicatorAnimationView` - typing view
  - `streamUiTypingIndicatorUsersTextSize`, `streamUiTypingIndicatorUsersTextColor`, `streamUiTypingIndicatorUsersTextFont`, `streamUiTypingIndicatorUsersTextFontAssets`, `streamUiTypingIndicatorUsersTextStyle` attributes to customize typing users text
- Added possibility to customize `TypingIndicatorView` style via `TransformStyle.typingIndicatorViewStyleTransformer`
- Added new properties allowing customizing `MessageInputView` using `MessageInputViewStyle` and `AttachmentSelectionDialogStyle`:
  - `MessageInputViewStyle.fileNameTextStyle`
  - `MessageInputViewStyle.fileSizeTextStyle`
  - `MessageInputViewStyle.fileCheckboxSelectorDrawable`
  - `MessageInputViewStyle.fileCheckboxTextColor`
  - `MessageInputViewStyle.fileAttachmentEmptyStateTextStyle`
  - `MessageInputViewStyle.mediaAttachmentEmptyStateTextStyle`
  - `MessageInputViewStyle.fileAttachmentEmptyStateText`
  - `MessageInputViewStyle.mediaAttachmentEmptyStateText`
  - `MessageInputViewStyle.dismissIconDrawable`
  - `AttachmentSelectionDialogStyle.allowAccessToGalleryText`
  - `AttachmentSelectionDialogStyle.allowAccessToFilesText`
  - `AttachmentSelectionDialogStyle.allowAccessToCameraText`
  - `AttachmentSelectionDialogStyle.allowAccessToGalleryIcon`
  - `AttachmentSelectionDialogStyle.allowAccessToFilesIcon`
  - `AttachmentSelectionDialogStyle.allowAccessToCameraIcon`
  - `AttachmentSelectionDialogStyle.grantPermissionsTextStyle`
  - `AttachmentSelectionDialogStyle.recentFilesTextStyle`
  - `AttachmentSelectionDialogStyle.recentFilesText`
  - `AttachmentSelectionDialogStyle.fileManagerIcon`
  - `AttachmentSelectionDialogStyle.videoDurationTextStyle`
  - `AttachmentSelectionDialogStyle.videoIconDrawable`
  - `AttachmentSelectionDialogStyle.videoIconVisible`
  - `AttachmentSelectionDialogStyle.videoLengthLabelVisible`
- Added `StreamUi.MessageInputView` theme allowing to customize all of the `MessageInputViewStyle` properties:
  - streamUiAttachButtonEnabled
  - streamUiAttachButtonIcon
  - streamUiLightningButtonEnabled
  - streamUiLightningButtonIcon
  - streamUiMessageInputTextSize
  - streamUiMessageInputTextColor
  - streamUiMessageInputHintTextColor
  - streamUiMessageInputScrollbarEnabled
  - streamUiMessageInputScrollbarFadingEnabled
  - streamUiSendButtonEnabled
  - streamUiSendButtonEnabledIcon
  - streamUiSendButtonDisabledIcon
  - streamUiShowSendAlsoToChannelCheckbox
  - streamUiSendAlsoToChannelCheckboxGroupChatText
  - streamUiSendAlsoToChannelCheckboxDirectChatText
  - streamUiSendAlsoToChannelCheckboxTextSize
  - streamUiSendAlsoToChannelCheckboxTextColor
  - streamUiSendAlsoToChannelCheckboxTextStyle
  - streamUiMentionsEnabled
  - streamUiMessageInputTextStyle
  - streamUiMessageInputHintText
  - streamUiCommandsEnabled
  - streamUiMessageInputEditTextBackgroundDrawable
  - streamUiMessageInputDividerBackgroundDrawable
  - streamUiPictureAttachmentIcon
  - streamUiFileAttachmentIcon
  - streamUiCameraAttachmentIcon
  - streamUiAllowAccessToCameraIcon
  - streamUiAllowAccessToFilesIcon
  - streamUiAllowAccessToGalleryIcon
  - streamUiAllowAccessToGalleryText
  - streamUiAllowAccessToFilesText
  - streamUiAllowAccessToCameraText
  - streamUiGrantPermissionsTextSize
  - streamUiGrantPermissionsTextColor
  - streamUiGrantPermissionsTextStyle
  - streamUiAttachmentsRecentFilesTextSize
  - streamUiAttachmentsRecentFilesTextColor
  - streamUiAttachmentsRecentFilesTextStyle
  - streamUiAttachmentsRecentFilesText
  - streamUiAttachmentsFileManagerIcon
  - streamUiAttachmentVideoLogoIcon
  - streamUiAttachmentVideoLengthVisible
  - streamUiAttachmentVideoIconVisible
  - streamUiCommandInputCancelIcon
  - streamUiCommandInputBadgeBackgroundDrawable
  - streamUiCommandInputBadgeIcon
  - streamUiCommandInputBadgeTextSize
  - streamUiCommandInputBadgeTextColor
  - streamUiCommandInputBadgeStyle
  - streamUiAttachmentsFileNameTextSize
  - streamUiAttachmentsFileNameTextColor
  - streamUiAttachmentsFileNameTextStyle
  - streamUiAttachmentsFileSizeTextSize
  - streamUiAttachmentsFileSizeTextColor
  - streamUiAttachmentsFileSizeTextStyle
  - streamUiFileCheckBoxSelectorTextColor
  - streamUiFileCheckBoxSelectorDrawable
  - streamUiAttachmentsFilesEmptyStateTextSize
  - streamUiAttachmentsFilesEmptyStateTextColor
  - streamUiAttachmentsFilesEmptyStateStyle
  - streamUiAttachmentsMediaEmptyStateTextSize
  - streamUiAttachmentsMediaEmptyStateTextColor
  - streamUiAttachmentsMediaEmptyStateStyle
  - streamUiAttachmentsFilesEmptyStateText
  - streamUiAttachmentsMediaEmptyStateText
  - streamUiMessageInputCloseButtonIconDrawable
- Added `streamUiMessageListFileAttachmentStyle` theme attribute to customize the appearance of file attachments within messages.

### ⚠️ Changed
- Made `Channel::getLastMessage` function public
- `AttachmentSelectionDialogFragment::newInstance` requires instance of `MessageInputViewStyle` as a parameter. You can obtain a default implementation of `MessageInputViewStyle` with `MessageInputViewStyle::createDefault` method.
- Renamed `FileAttachmentsViewStyle` class to `FileAttachmentViewStyle`

### ❌ Removed
- 🚨 Breaking change: `MessageListItemStyle::reactionsEnabled` was deleted as doubling of the same flag from `MessageListViewStyle`


# July 19th, 2021 - 4.14.2
## stream-chat-android-client
### ❌ Removed
- Removed `Channel::isMuted` extension. Use `User::channelMutes` or subscribe for `NotificationChannelMutesUpdatedEvent` to get information about muted channels.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed crash caused by missing `streamUiReplyAvatarStyle` and `streamUiMessageOptionsAvatarStyle`

### ⬆️ Improved
- "Copy Message" option is now hidden when the message contains no text to copy.

### ✅ Added
- Now you can configure the style of `AttachmentMediaActivity`.

# July 14th, 2021 - 4.14.1
## stream-chat-android-ui-components
### ✅ Added
- Added `MessageListView::requireStyle` which expose `MessageListViewStyle`. Be sure to invoke it when view is initialized already.

# July 13th, 2021 - 4.14.0
## Common changes for all artifacts
### 🐞 Fixed
- Fix scroll bug in the `MessageListView` that produces an exception related to index out of bounds.

## stream-chat-android-client
### ⬆️ Improved
- Improved `ChatClient::enableSlowMode`, `ChatClient::disableSlowMode`, `ChannelClient::enableSlowMode`, `ChannelClient::disableSlowMode` methods. Now the methods do partial channel updates so that other channel fields are not affected.

### ✅ Added
- Added `ChatClient::partialUpdateUser` method for user partial updates.

## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug related to editing message in offline mode. The bug was causing message to reset to the previous one after connection was recovered.
- Fixed violation of comparison contract for nullable fields in `QuerySort::comparator`

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the alignment of the titles in `MessageListHeaderView` when the avatar is hidden.

### ✅ Added
- Added `streamUiMessagesStart` that allows to control if the stack of messages starts at the bottom or the top.
- Added `streamUiThreadMessagesStart` that allows to control if the stack of thread messages starts at the bottom or the top.
- Added `streamUiSuggestionListViewStyle` that allows to customize `SuggestionListView` with a theme
- Added `streamUiChannelListHeaderStyle` that allows to customize ChannelListHeaderView.
- `MentionListView` can be customisable with XML parameters and with a theme.
- Added possibility to customize all avatar using themes. Create
  ```
  <style name="StreamTheme" parent="@style/StreamUiTheme">
  ```
  and customize all the avatars that you would like. All options are available here:
  https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs.xml
- Now you can use the style `streamUiChannelListHeaderStyle` to customize ChannelListHeaderView.

### ⚠️ Changed
- 🚨 Breaking change: removed `MessageListItemStyle.threadsEnabled` property. You should use only the `MessageListViewStyle.threadsEnabled` instead. E.g. The following code will disable both _Thread reply_ message option and _Thread reply_ footnote view visible below the message list item:
```kotlin
        TransformStyle.messageListStyleTransformer = StyleTransformer {
  it.copy(threadsEnabled = false)
}
```

# July 1st, 2021 - 4.13.0
## Common changes for all artifacts
### ⬆️ Improved
- Updated to Kotlin 1.5.20

## stream-chat-android
### ✅ Added
- Added `ChatUi.Builder#withImageHeadersProvider` to allow adding custom headers to image requests

## stream-chat-android-client
### ⚠️ Changed
- Using the `useNewSerialization` option on the `ChatClient.Builder` to opt out from using the new serialization implementation is now an error. Please start using the new serialization implementation, or report any issues keeping you from doing so. The old implementation will be removed soon.

## stream-chat-android-offline
### 🐞 Fixed
- By default we use backend request to define is new message event related to our query channels specs or not. Now filtering by BE only fields works for channels

## stream-chat-android-ui-components
### ✅ Added
- Added new attributes to `MessageInputView` allowing to customize the style of input field during command input:
  - `streamUiCommandInputBadgeTextSize`, `streamUiCommandInputBadgeTextColor`, `streamUiCommandInputBadgeFontAssets`, `streamUiCommandInputBadgeFont`, `streamUiCommandInputBadgeStyle` attributes to customize the text appearance of command name inside command badge
  - `streamUiCommandInputCancelIcon` attribute to customize the icon for cancel button
  - `streamUiCommandInputBadgeIcon` attribute to customize the icon inside command badge
  - `streamUiCommandInputBadgeBackgroundDrawable` attribute to customize the background shape of command badge
- Added possibility to customize `MessageListHeaderView` style via `streamUiMessageListHeaderStyle` theme attribute and via `TransformStyle.messageListHeaderStyleTransformer`.
- Added new attributes to `MessageInputView`:
  - `streamUiCommandIcon` attribute to customize the command icon displayed for each command item in the suggestion list popup
  - `streamUiLightningIcon` attribute to customize the lightning icon displayed in the top left corner of the suggestion list popup
- Added support for customizing `SearchInputView`
  - Added `SearchInputViewStyle` class allowing customization using `TransformStyle` API
  - Added XML attrs for `SearchInputView`:
    - `streamUiSearchInputViewHintText`
    - `streamUiSearchInputViewSearchIcon`
    - `streamUiSearchInputViewClearInputIcon`
    - `streamUiSearchInputViewBackground`
    - `streamUiSearchInputViewTextColor`
    - `streamUiSearchInputViewHintColor`
    - `streamUiSearchInputViewTextSize`
- Added `ChatUi#imageHeadersProvider` to allow adding custom headers to image requests

### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.
- Made `SuggestionListController` and `SuggestionListUi` public. Note that both of these are _experimental_, which means that the API might change at any time in the future (even without a deprecation cycle).
- Made `AttachmentSelectionDialogFragment` _experimental_ which means that the API might change at any time in the future (even without a deprecation cycle).


# June 23th, 2021 - 4.12.1
## stream-chat-android-client
### ✅ Added
- Added `ChannelClient::sendEvent` method which allows to send custom events.
- Added nullable `User` field to `UnknownEvent`.

### ❌ Removed
- Removed the `Message::attachmentsSyncStatus` field


## stream-chat-android-offline
### 🐞 Fixed
- Fixed `in` and `nin` filters when filtering by extra data field that is an array.
- Fixed crash when adding a reaction to a thread message.

### ⬆️ Improved
- Now attachments can be sent while being in offline


## stream-chat-android-ui-common
### ✅ Added
- Made `AttachmentSelectionDialogFragment` public. Use `newInstance` to create instances of this Fragment.


## stream-chat-android-ui-components
### ⬆️ Improved
- Hide suggestion list popup when keyboard is hidden.

### ✅ Added
- Added the `MessageInputView::hideSuggestionList` method to hide the suggestion list popup.


# June 15th, 2021 - 4.12.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed thrown exception type while checking if `ChatClient` is initialized

## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug where reactions of other users were sometimes displayed as reactions of the current user.
- Fixed bug where deleted user reactions were sometimes displayed on the message options overlay.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed bug where files without extension in their name lost the mime type.
- Using offline.ChatDomain instead of livedata.ChatDomain in ChannelListViewModel.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixing the save of pictures from AttachmentGalleryActivity. When external storage
  permission is not granted, now it asks for it.
### ⬆️ Improved
- Added default implementation of "Leave channel" click listener to `ChannelListViewModelBinding`

### ✅ Added
- Added `streamUiChannelActionsDialogStyle` attribute to application theme and `ChannelListView` to customize channel actions dialog appearance. The attribute references a style with the following attributes:
  - `streamUiChannelActionsMemberNamesTextSize`, `streamUiChannelActionsMemberNamesTextColor`, `streamUiChannelActionsMemberNamesTextFont`, `streamUiChannelActionsMemberNamesTextFontAssets`, `streamUiChannelActionsMemberNamesTextStyle` attributes to customize dialog title with member names
  - `streamUiChannelActionsMemberInfoTextSize`, `streamUiChannelActionsMemberInfoTextColor`, `streamUiChannelActionsMemberInfoTextFont`, `streamUiChannelActionsMemberInfoTextFontAssets`, `streamUiChannelActionsMemberInfoTextStyle` attributes to customize dialog subtitle with member info
  - `streamUiChannelActionsItemTextSize`, `streamUiChannelActionsItemTextColor`, `streamUiChannelActionsItemTextFont`, `streamUiChannelActionsItemTextFontAssets`, `streamUiChannelActionsItemTextStyle` attributes to customize action item text style
  - `streamUiChannelActionsWarningItemTextSize`, `streamUiChannelActionsWarningItemTextColor`, `streamUiChannelActionsWarningItemTextFont`, `streamUiChannelActionsWarningItemTextFontAssets`, `streamUiChannelActionsWarningItemTextStyle` attributes to customize warning action item text style
  - `streamUiChannelActionsViewInfoIcon` attribute to customize "View Info" action icon
  - `streamUiChannelActionsViewInfoEnabled` attribute to hide/show "View Info" action item
  - `streamUiChannelActionsLeaveGroupIcon` attribute to customize "Leave Group" action icon
  - `streamUiChannelActionsLeaveGroupEnabled` attribute to hide/show "Leave Group" action item
  - `streamUiChannelActionsDeleteConversationIcon` attribute to customize "Delete Conversation" action icon
  - `streamUiChannelActionsDeleteConversationEnabled` attribute to hide/show "Delete Conversation" action item
  - `streamUiChannelActionsCancelIcon` attribute to customize "Cancel" action icon
  - `streamUiChannelActionsCancelEnabled` attribute to hide/show "Cancel" action item
  - `streamUiChannelActionsBackground` attribute for dialog's background
- Added `streamUiIconOnlyVisibleToYou` attribute to `MessageListView` to allow customizing "Only visible to you" icon placed in messages footer
- Added `GiphyViewHolderStyle` to `MessageListViewStyle` to allow customizing `GiphyViewHolder`. The new style comes together with following `MessageListView` attributes:
  - `streamUiGiphyCardBackgroundColor` attribute to customize card's background color
  - `streamUiGiphyCardElevation` attribute to customize card's elevation
  - `streamUiGiphyCardButtonDividerColor` attribute to customize dividers' colors
  - `streamUiGiphyIcon` attribute to customize Giphy icon
  - `streamUiGiphyLabelTextSize`, `streamUiGiphyLabelTextColor`, `streamUiGiphyLabelTextFont`, `streamUiGiphyLabelTextFontAssets`, `streamUiGiphyLabelTextStyle` attributes to customize label
  - `streamUiGiphyQueryTextSize`, `streamUiGiphyQueryTextColor`, `streamUiGiphyQueryTextFont`, `streamUiGiphyQueryTextFontAssets`, `streamUiGiphyQueryTextStyle` attributes to customize query text
  - `streamUiGiphyCancelButtonTextSize`, `streamUiGiphyCancelButtonTextColor`, `streamUiGiphyCancelButtonTextFont`, `streamUiGiphyCancelButtonTextFontAssets`, `streamUiGiphyCancelButtonTextStyle` attributes to customize cancel button text
  - `streamUiGiphyShuffleButtonTextSize`, `streamUiGiphyShuffleButtonTextColor`, `streamUiGiphyShuffleButtonTextFont`, `streamUiGiphyShuffleButtonTextFontAssets`, `streamUiGiphyShuffleButtonTextStyle` attributes to customize shuffle button text
  - `streamUiGiphySendButtonTextSize`, `streamUiGiphySendButtonTextColor`, `streamUiGiphySendButtonTextFont`, `streamUiGiphySendButtonTextFontAssets`, `streamUiGiphySendButtonTextStyle` attributes to customize send button text
- Adding extra XML attrs allowing to customize "Send also to channel" CheckBox at `MessageInputView` component:
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDrawable`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDirectChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxGroupChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextStyle`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextColor`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextSize`
- Added `streamUiWarningMessageOptionsTextSize`, `streamUiWarningMessageOptionsTextColor`, `streamUiWarningMessageOptionsTextFont`, `streamUiWarningMessageOptionsFontAssets`, `streamUiWarningMessageOptionsTextStyle` attributes to `MessageListView` for customizing warning actions text appearance
- Deprecated multiple views' tint properties and attributes. Use custom drawables instead.
- Added `MediaAttachmentViewStyle` to allow customizing the appearance of media attachments in the message list. The new style comes together with following `MediaAttachmentView` attributes:
  - `progressIcon` - attribute to customize animated progress drawable when image is loading
  - `giphyIcon` - attribute to customize Giphy icon
  - `imageBackgroundColor` - attribute to customize image background color
  - `moreCountOverlayColor` - attribute to customize the color of "more count" semi-transparent overlay
  - `moreCountTextStyle` - attribute to customize text appearance of more count text
- Added `MessageReplyStyle` class allowing to customize MessageReply item view on MessageListView.
  Customization can be done using `TransformStyle` API or XML attributes of `MessageListView`:
  - `streamUiMessageReplyBackgroundColorMine`
  - `streamUiMessageReplyBackgroundColorTheirs`
  - `streamUiMessageReplyTextSizeMine`
  - `streamUiMessageReplyTextColorMine`
  - `streamUiMessageReplyTextFontMine`
  - `streamUiMessageReplyTextFontAssetsMine`
  - `streamUiMessageReplyTextStyleMine`
  - `streamUiMessageReplyTextSizeTheirs`
  - `streamUiMessageReplyTextColorTheirs`
  - `streamUiMessageReplyTextFontTheirs`
  - `streamUiMessageReplyTextFontAssetsTheirs`
  - `streamUiMessageReplyTextStyleTheirs`
  - `streamUiMessageReplyLinkColorMine`
  - `streamUiMessageReplyLinkColorTheirs`
  - `streamUiMessageReplyLinkBackgroundColorMine`
  - `streamUiMessageReplyLinkBackgroundColorTheirs`
  - `streamUiMessageReplyStrokeColorMine`
  - `streamUiMessageReplyStrokeWidthMine`
  - `streamUiMessageReplyStrokeColorTheirs`
  - `streamUiMessageReplyStrokeWidthTheirs`
- Added `FileAttachmentsViewStyle` class allowing to customize FileAttachmentsView item view on MessageListView.
- Added `MessageInputView::setSuggestionListViewHolderFactory` method which allows to provide custom views from suggestion list popup.

### ⚠️ Changed
- Changed the naming of string resources. The updated names can be reviewed in:
  - `strings_common.xml`
  - `strings_attachment_gallery.xml`
  - `strings_channel_list.xml`
  - `strings_channel_list_header.xml`
  - `strings_mention_list.xml`
  - `strings_message_input.xml`
  - `strings_message_list.xml`
  - `strings_message_list_header.xml`
  - `strings_search.xml`

# May 2nd, 2021 - 4.11.0
## Common changes for all artifacts
### 🐞 Fixed
- Fixed channel list sorting
### ⬆️ Improved
- Updated to Kotlin 1.5.10, coroutines 1.5.0
- Updated to Android Gradle Plugin 4.2.1
- Updated Room version to 2.3.0
- Updated Firebase, AndroidX, and other dependency versions to latest, [see here](https://github.com/GetStream/stream-chat-android/pull/1895) for more details
- Marked many library interfaces that should not be implemented by clients as [sealed](https://kotlinlang.org/docs/sealed-classes.html)
- Removed Fresco, PhotoDraweeView, and FrescoImageViewer dependencies (replaced by StfalconImageViewer)

## stream-chat-android
### 🐞 Fixed
- Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelsView`.
- Fixed link preview UI issues in old-ui package
- Fixed crashes when opening the image gallery.

## stream-chat-android-client
### 🐞 Fixed
- Fixed querying banned users using new serialization.
- Fixed the bug when wrong credentials lead to inability to login
- Fixed issues with Proguard stripping response classes in new serialization implementation incorrectly

### ⬆️ Improved
- Improved handling push notifications:
  - Added `ChatClient.handleRemoteMessage` for remote message handling
  - Added `ChatClient.setFirebaseToken` for setting Firebase token
  - Added `NotificationConfig::pushNotificationsEnabled` for disabling push notifications
  - Deprecated `ChatClient.onMessageReceived`
  - Deprecated `ChatClient.onNewTokenReceived`
  - Changed `ChatNotificationHandler::buildNotification` signature - it now receives `Channel` and `Message` and returns `NotificationCompat.Builder` for better customization
  - Deprecated `ChatNotificationHandler.getSmallIcon`
  - Deprecated `ChatNotificationHandler.getFirebaseMessageIdKey`
  - Deprecated `ChatNotificationHandler.getFirebaseChannelIdKey`
  - Deprecated `ChatNotificationHandler.getFirebaseChannelTypeKey`
  - Changed `ChatNotificationHandler::onChatEvent` - it now doesn't handle events by default and receives `NewMessageEvent` instead of generic `ChatEvent`
- Improved error description provided by `ChatClient::sendImage`, `ChatClient::sendFile`, `ChannelClient::sendImage` and `ChannelClient::sendFile` methods if upload fails.

### ✅ Added
- Added `ChatClient::truncateChannel` and `ChannelClient::truncate` methods to remove messages from a channel.
- Added `DisconnectCause` to `DisconnectedEvent`
- Added method `SocketListener::onDisconnected(cause: DisconnectCause)`
- Added possibility to group notifications:
  - Notifications grouping is disabled by default and can be enabled using `NotificationConfig::shouldGroupNotifications`
  - If enabled, by default notifications are grouped by Channel's cid
  - Notifications grouping can be configured using `ChatNotificationHandler` and `NotificationConfig`
- Added `ChatNotificationHandler::getFirebaseMessaging()` method in place of `ChatNotificationHandler::getFirebaseInstanceId()`.
  It should be used now to fetch Firebase token in the following way: `handler.getFirebaseMessaging()?.token?.addOnCompleteListener {...}`.
- Added `Message.attachmentsSyncStatus: SyncStatus` property.

### ⚠️ Changed
- Changed the return type of `FileUploader` methods from nullable string to `Result<String>`.
- Updated `firebase-messaging` library to the version `22.0.0`. Removed deprecated `FirebaseInstanceId` invocations from the project.

### ❌ Removed
- `ChatNotificationHandler::getFirebaseInstanceId()` due to `FirebaseInstanceId` being deprecated. It's replaced now with `ChatNotificationHandler::getFirebaseMessaging()`.

## stream-chat-android-ui-components
### 🐞 Fixed
Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelListView`.
Fixed bug when for some video attachments activity with media player wasn't shown.

### ✅ Added
- Added `topLeft`, `topRight`, `bottomLeft`, `bottomRight` options to the `streamUiAvatarOnlineIndicatorPosition` attribute of `AvatarView` and corresponding constants to `AvatarView.OnlineIndicatorPosition` enum.

### ⚠️ Changed
- Swipe options of `ChannelListView` component:
  - "Channel more" option is now not shown by default because we are not able to provide generic, default implementation for it.
    If you want to make this option visible, you need to set `app:streamUiChannelOptionsEnabled="true"` explicitly to `io.getstream.chat.android.ui.channel.list.ChannelListView` component.
  - "Channel delete" option has now default implementation. Clicking on the "delete" icon shows AlertDialog asking to confirm Channel deletion operation.

# May 11th, 2021 - 4.10.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed the usage of `ProgressCallback` in `ChannelClient::sendFile` and `ChannelClient::sendImage` methods.

### ✅ Added
- Added `ChannelClient::deleteFile` and `ChannelClient::deleteImage` methods.
- Added `NotificationInviteRejectedEvent`
- Added `member` field to the `NotificationRemovedFromChannel` event
- Added `totalUnreadCount` and `unreadChannels` fields to the following events:
- `notification.channel_truncated`
- `notification.added_to_channel`
- `notification.channel_deleted`
- Added `channel` field to the `NotificationInviteAcceptedEvent` event
- Added `channel` field to the `NotificationInviteRejectedEvent` event

### ⚠️ Changed
- **The client now uses a new serialization implementation by default**, which was [previously](https://github.com/GetStream/stream-chat-android/releases/tag/4.8.0) available as an opt-in API.
  - This new implementation is more performant and greatly improves type safety in the networking code of the SDK.
  - If you experience any issues after upgrading to this version of the SDK, you can call `useNewSerialization(false)` when building your `ChatClient` to revert to using the old implementation. Note however that we'll be removing the old implementation soon, so please report any issues found.
  - To check if the new implementation is causing any failures in your app, enable error logging on `ChatClient` with the `logLevel` method, and look for the `NEW_SERIALIZATION_ERROR` tag in your logs while using the SDK.
- Made the `user` field in `channel.hidden` and `notification.invite_accepter` events non nullable.
- Updated channels state after `NotificationInviteRejectedEvent` or `NotificationInviteAcceptedEvent` is received

### ❌ Removed
- Removed redundant events which can only be received by using webhooks:
  - `channel.created`
  - `channel.muted`
  - `channel.unmuted`
  - `channel.muted`
  - `channel.unmuted`
- Removed `watcherCount` field from the following events as they are not returned with the server response:
  - `message.deleted`
  - `message.read`
  - `message.updated`
  - `notification.mark_read`
- Removed `user` field from the following events as they are not returned with the server response:
  - `notification.channel_deleted`
  - `notification.channel_truncated`
## stream-chat-android-offline
### 🐞 Fixed
- Fixed an issue when CustomFilter was configured with an int value but the value from the API was a double value
### ⚠️ Changed

- Changed the upload logic in `ChannelController` for the images unsupported by the Stream CDN. Now such images are uploaded as files via `ChannelClient::sendFile` method.
### ❌ Removed

## stream-chat-android-ui-common
### ⬆️ Improved
- Updated ExoPlayer version to 2.13.3

### ⚠️ Changed
- Deprecated `MessageInputViewModel::editMessage`. Use `MessageInputViewModel::messageToEdit` and `MessageInputViewModel::postMessageToEdit` instead.
- Changed `MessageInputViewModel::repliedMessage` type to `LiveData`. Use `ChatDomain::setMessageForReply` for setting message for reply.
- Changed `MessageListViewModel::mode` type to `LiveData`. Mode is handled internally and shouldn't be modified outside the SDK.

## stream-chat-android-ui-components
### 🐞 Fixed
- Removed empty badge for selected media attachments.

### ✅ Added
- Added `messageLimit` argument to `ChannelListViewModel` and `ChannelListViewModelFactory` constructors to allow changing the number of fetched messages for each channel in the channel list.

# April 30th, 2021 - 4.9.2
## stream-chat-android-offline
### ✅ Added
- Added `ChatDomain::user`, a new property that provide the current user into a LiveData/StateFlow container

### ⚠️ Changed
- `ChatDomain::currentUser` has been warning-deprecated because it is an unsafe property that could be null, you should subscribe to `ChatDomain::user` instead

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed NPE on MessageInputViewModel when the it was initialized before the user was set

# April 29th, 2021 - 4.9.1
## stream-chat-android
### ⬆️ Improved
* Updated coil dependency to the latest version. This fixes problem with .heic, and .heif attachment metadata parsing.

## stream-chat-android-client
### 🐞 Fixed
- Optimized the number of `ChatClient::addDevice` API calls

### ⬆️ Improved
- Events received after the client closes the connection are rejected

## stream-chat-android-offline
### 🐞 Fixed
- Fixed offline reactions sync

### ✅ Added
- Added new versions with API based on kotlin `StateFlow` for the following classes:
  * `io.getstream.chat.android.offline.ChatDomain`
  * `io.getstream.chat.android.offline.channel.ChannelController`
  * `io.getstream.chat.android.offline.thread.ThreadController`
  * `io.getstream.chat.android.offline.querychannels.QueryChannelsController`

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed crash related to accessing `ChatDomain::currentUser` in `MessageListViewModel` before user is connected

## stream-chat-android-ui-components
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
- Added support for error messages
- Added attrs to `MessageListView` that allow to customize error message text style:
  * `streamUiErrorMessageTextSize`
  * `streamUiErrorMessageTextColor`
  * `streamUiErrorMessageTextFont`
  * `streamUiErrorMessageTextFontAssets`
  * `streamUiErrorMessageTextStyle`

# April 21th, 2021 - 4.9.0
## Common changes for all artifacts
### ✅ Added
Added icon to show when channel is muted in ChannelListView.
It is possible to customize the color and the drawable of the icon.

## stream-chat-android
### 🐞 Fixed
- Fixed multiline messages which were displayed in a single line

### ❌ Removed
- Removed deprecated `MessageListView::setViewHolderFactory` method
- Removed deprecated `Chat` interface

## stream-chat-android-client
### 🐞 Fixed
- Fixed: local cached hidden channels stay hidden even though new message is received.
- Make `Flag::approvedAt` nullable
- Fixed error event parsing with new serialization implementation

### ✅ Added
- Added `ChatClient::updateChannelPartial` and `ChannelClient::updatePartial` methods for partial updates of channel data.

### ⚠️ Changed
- Deprecated `ChannelClient::unBanUser` method
- Deprecated `ChatClient::unBanUser` method
- Deprecated `ChatClient::unMuteChannel` method

### ❌ Removed
- Removed deprecated `ChatObservable` class and all its uses
- Removed deprecated `ChannelControler` interface

## stream-chat-android-offline
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
- Added `ChatDomain::removeMembers` method
- Added `ChatDomain::createDistinctChannel` A use-case for creating a channel based on its members.
- Added `ChatDomain::removeMembers` method

### ⚠️ Changed
- Deprecated `ChatDomain.useCases`. It has `DeprecationLevel.Warning` and still can be used. However, it will be not available in the future, so please consider migrating to use `ChatDomain` use case functions instead.
- Deprecated `GetUnreadChannelCount`
- Deprecated `GetTotalUnreadCount`

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed compatibility with latest Dagger Hilt versions

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed not perfectly rounded avatars
- `MessageInputView::UserLookupHandler` is not overridden everytime that members livedata is updated
- Fixed doubled command prefix when the command contains user mention
- Fixed handling user mute state in default `MessageListViewOptions` dialog
- Fixed incorrect "last seen" text
- Fixed multiline messages which were displayed in a single line

### ⬆️ Improved
- Setting external SuggestionListView is no longer necessary to display suggestions popup
### ✅ Added
- Added `ChatUI.supportedReactions: SupportedReactions` property, also introduced `SupportedReactions`, and `ReactionDrawable` class.
  It allows defining a set of supported reactions by passing a `Map<String, ReactionDrawable>` in constructor. `ReactionDrawable` is a wrapping class holding two `Drawable` instances - for active and inactive reaction states.
- Added methods and attrs to `MessageListView` that allow to customize visibility of message options:
  * `MessageListView::setDeleteMessageConfirmationEnabled`
  * `MessageListView::setCopyMessageEnabled`
  * `MessageListView::setBlockUserEnabled`
  * `MessageListView::setMuteUserEnabled`
  * `MessageListView::setMessageFlagEnabled`
  * `MessageListView::setReactionsEnabled`
  * `MessageListView::setRepliesEnabled`
  * `MessageListView::setThreadsEnabled`
  * `MessageListView.streamUiFlagMessageOptionEnabled`
  * `MessageListView.streamUiMuteUserOptionEnabled`
  * `MessageListView.streamUiBlockUserOptionEnabled`
  * `MessageListView.streamUiCopyMessageActionEnabled`
- Added confirmation dialog for flagging message option:
  * Added `MessageListView::flagMessageConfirmationEnabled` attribute
- Added `MessageListView::setFlagMessageResultHandler` which allows to handle flag message result
- Added support for system messages
- Added attrs to `MessageListView` that allow to customize system message text style:
  * `streamUiSystemMessageTextSize`
  * `streamUiSystemMessageTextColor`
  * `streamUiSystemMessageTextFont`
  * `streamUiSystemMessageTextFontAssets`
  * `streamUiSystemMessageTextStyle`
- Added attrs to `MessageListView` that allow to customize message option text style:
  * `streamUiMessageOptionsTextSize`
  * `streamUiMessageOptionsTextColor`
  * `streamUiMessageOptionsTextFont`
  * `streamUiMessageOptionsTextFontAssets`
  * `streamUiMessageOptionsTextStyle`
- Added attrs to `MessageListView` that allow to customize user reactions title text style:
  * `streamUiUserReactionsTitleTextSize`
  * `streamUiUserReactionsTitleTextColor`
  * `streamUiUserReactionsTitleTextFont`
  * `streamUiUserReactionsTitleTextFontAssets`
  * `streamUiUserReactionsTitleTextStyle`
- Added attrs to `MessageListView` that allow to customize colors of message options background, user reactions card background, overlay dim color and warning actions color:
  * `streamUiMessageOptionBackgroundColor`
  * `streamUiUserReactionsBackgroundColor`
  * `streamUiOptionsOverlayDimColor`
  * `streamUiWarningActionsTintColor`
- Added `ChatUI.mimeTypeIconProvider: MimeTypeIconProvider` property which allows to customize file attachment icons.

### ⚠️ Changed
- Now the "block user" feature is disabled. We're planning to improve the feature later. Stay tuned!
- Changed gallery background to black in dark mode

# April 8th, 2021 - 4.8.1
## Common changes for all artifacts
### ⚠️ Changed
- We've cleaned up the transitive dependencies that our library exposes to its clients. If you were using other libraries implicitly through our SDK, you'll now have to depend on those libraries directly instead.

## stream-chat-android
### 🐞 Fixed
- Fix Attachment Gravity

### ✅ Added
- Provide AvatarView class

## stream-chat-android-offline
### 🐞 Fixed
- Fix Crash on some devices that are not able to create an Encrypted SharedPreferences
- Fixed the message read indicator in the message list
- Added missing `team` field to `ChannelEntity` and `ChannelData`

### ✅ Added
- Add `ChatDomain::removeMembers` method

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed getting files provided by content resolver.

### ⚠️ Changed
- Added theme to all activities all the SDK. You can override then in your project by redefining the styles:
- StreamUiAttachmentGalleryActivityStyle
- StreamUiAttachmentMediaActivityStyle
- StreamUiAttachmentActivityStyle

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attr streamUiCopyMessageActionEnabled. From color to boolean.
- Now it is possible to change the color of `MessageListHeaderView` from the XML.
- Fixed the `MessageListView::setUserClickListener` method.
- Fixed bugs in handling empty states for `ChannelListView`. Deprecated manual methods for showing/hiding empty state changes.
- Fix `ChannelListHeaderView`'s title position when user avatar or action button is invisible
- Fix UI behaviour for in-progress file uploads
- Fix extension problems with file uploads when attachment names contain spaces
- Fix reaction bubbles which were shown behind message attachment views

### ✅ Added
- Now it is possible to change the back button of MessageListHeaderView using `app:streamUiMessageListHeaderBackButtonIcon`
- Now it is possible to inject `UserLookupHandler` into `MessageInputView` in order to implement custom users' mention lookup algorithm

# March 31th, 2021 - 4.8.0
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

## stream-chat-android
### 🐞 Fixed
- Fixed crash when sending GIF from Samsung keyboard

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

## stream-chat-android-offline
### 🐞 Fixed
- Fixed an issue that didn't find the user when obtaining the list of messages
- Fix refreshing not messaging channels which don't contain current user as a member

## stream-chat-android-ui-common
### ⬆️ Improved
- Show AttachmentMediaActivity for video attachments

### ✅ Added
- `AvatarView.streamUiAvatarOnlineIndicatorColor` and `AvatarView.streamUiAvatarOnlineIndicatorBorderColor` attrs

## stream-chat-android-ui-components
### 🐞 Fixed
- Now replied messages are shown correctly with the replied part in message options
- `MessageListView::enterThreadListener` is properly notified when entering into a thread
- Fix initial controls state in `MessageInputView`
- Fix crashing when open attachments destination

### ⬆️ Improved
- Add support of non-image attachment types to the default attachment click listener.

### ✅ Added
- `MessageInputView` now uses the cursor `stream_ui_message_input_cursor.xml` instead of accent color. To change the cursor, override `stream_ui_message_input_cursor.xml`.
- Replacing `ChatUI` with new `io.getstream.chat.android.ui.ChatUI` implementation
- Added possibility to configure delete message option visibility using `streamUiDeleteMessageEnabled` attribute, and `MessageListView::setDeleteMessageEnabled` method
- Add `streamUiEditMessageEnabled` attribute to `MessageListView` and `MessageListView::setEditMessageEnabled` method to enable/disable the message editing feature
- Add `streamUiMentionsEnabled` attribute to `MessageInputView` and `MessageInputView::setMentionsEnabled` method to enable/disable mentions
- Add `streamUiThreadsEnabled` attribute to `MessageListView` and `MessageListView::setThreadsEnabled` method to enable/disable the thread replies feature
- Add `streamUiCommandsEnabled` attribute to `MessageInputView` and `MessageInputView::setCommandsEnabled` method to enable/disable commands
- Add `ChannelListItemPredicate` to our `channelListView` to allow filter `ChannelListItem` before they are rendered
- Open `AvatarBitmapFactory` class
- Add `ChatUI::avatarBitmapFactory` property to allow custom implementation of `AvatarBitmapFactory`
- Add `AvatarBitmapFactory::userBitmapKey` method to generate cache key for a given User
- Add `AvatarBitmapFactory::channelBitmapKey` method to generate cache key for a given Channel
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
  - Unread count badge on scroll to bottom button
  - Message stroke width and color for mine and theirs types
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
- It is now possible to configure new fields in MessageInputView:
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
- `streamUiMessageInputDividerBackgroundDrawable` - customize the background of divider of MessageInputView

### ⚠️ Changed
- Deprecated `ChatUI` class

# March 8th, 2021 - 4.7.0
## stream-chat-android-client
### ⚠️ Changed
- Refactored `FilterObject` class  - see the [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-FilterObject) for more info

## stream-chat-android-offline
### 🐞 Fixed
- Fixed refreshing channel list after removing member
- Fixed an issue that didn't find the user when obtaining the list of messages

### ⚠️ Changed
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.
- Deprecated constructors for `ChatDomain.Builder` with the `User` type parameter, use constructor with `Context` and `ChatClient` instead.

## stream-chat-android-ui-common
### ⚠️ Changed
- Message options list changed colour for dark version. The colour is a little lighters
  now, what makes it easier to see.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed some rare crashes when `MessageListView` was created without any attribute info present

### ⬆️ Improved
- Updated PhotoView to version 2.3.0

### ✅ Added
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Introduced `TextAndAttachmentsViewHolder` for any combination of attachment content and text

### ❌ Removed
- Deleted `OnlyFileAttachmentsViewHolder`, `OnlyMediaAttachmentsViewHolder`,
  `PlainTextWithMediaAttachmentsViewHolder` and `PlainTextWithFileAttachmentsViewHolder`

# Feb 22th, 2021 - 4.6.0
# New UI-Components Artifact
A new UI-Components artifact has been created with a new design of all our components.
This new artifact is available on MavenCentral and can imported by adding the following dependency:
```
implementation "io.getstream:stream-chat-android-ui-components:4.6.0"
```

## stream-chat-android
- Add `streamMessageActionButtonsTextSize`, `streamMessageActionButtonsTextColor`, `streamMessageActionButtonsTextFont`,
  `streamMessageActionButtonsTextFontAssets`, `streamMessageActionButtonsTextStyle`, `streamMessageActionButtonsIconTint`
  attributes to `MessageListView`
- Add `ChannelHeaderViewModel::resetThread` method and make `ChannelHeaderViewModel::setActiveThread` message parameter non-nullable
- Fix ReadIndicator state
- Using `MessageListView#setViewHolderFactory` is now an error - use `setMessageViewHolderFactory` instead
- Removed `MessageListItemAdapter#replaceEntities` - use `submitList` method instead
- Use proper color values on Dialog Theme
- Increase touchable area on the button to remove an attachment

## stream-chat-android-client
- Introduce ChatClient::setUserWithoutConnecting function
- Handle disconnect event during pending token state
- Remove unneeded user data when creating WS Connection
- Using `User#unreadCount` is now an error - use `totalUnreadCount` instead
- Using `ChannelController` is now an error - use `ChannelClient` instead
- Using `Pagination#get` is now an error - use `toString` instead
- Using the old event APIs is now an error - see the [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-ChatObserver-and-events()-APIs) for more info
- Using `ChatClient#flag` is now an error - use `flagUser` instead

## stream-chat-android-offline
- Introduce `PushMessageSyncHandler` class

- Add UseCase for querying members (`chatDomain.useCases.queryMembers(..., ...).execute()`).
  - If we're online, it executes a remote call through the ChatClient
  - If we're offline, it pulls members from the database for the given channel
- Mark the `SendMessageWithAttachmentsImpl` use case an error

## stream-chat-android-ui-common
- Fix `CaptureMediaContract` chooser on Android API 21
- Using `ChatUI(client, domain, context)` now an error - use simpler constructor instead
- Using the `Chat` interface now an error - use `ChatUI` instead

# Feb 15th, 2021 - 4.5.5
## Common changes for all artifacts
- Updated project dependencies
  - Kotlin 1.4.30
  - Stable AndroidX releases: LiveData 2.3.0, Activity 1.2.0, Fragment 1.3.0
  - For the full list of dependency version changes, see [this PR](https://github.com/GetStream/stream-chat-android/pull/1383)

## stream-chat-android
- Add `streamInputAttachmentsMenuBackground` and `streamInputSuggestionsBackground` attributes to `MessageInputView`
- Add `streamMessageActionButtonsBackground` attributes to `MessageListView`

## stream-chat-android-client
- Remove unused `reason` and `timeout` parameters from `ChannelClient::unBanUser` method

# Feb 11th, 2021 - 4.5.4
## stream-chat-android
- Fix `streamLastMessageDateUnreadTextColor` attribute not being used in ChannelListView
- Fix `streamChannelsItemSeparatorDrawable` attribute not being parsed

## stream-chat-android-client
- Fix `ConcurrentModificationException` on our `NetworkStateProvider`

# Feb 5th, 2021 - 4.5.3
## stream-chat-android
-. `ChatUtils::devToken` is not accessible anymore, it has been moved to `ChatClient::devToken`

## stream-chat-android-client
- **setUser deprecation**
  - The `setUser`, `setGuestUser`, and `setAnonymousUser` methods on `ChatClient` are now deprecated.
  - Prefer to use the `connectUser` (`connectGuestUser`, `connectAnonymousUser`) methods instead, which return `Call` objects.
  - If you want the same async behaviour as with the old methods, use `client.setUser(user, token).enqueue { /* Handle result */ }`.
- Add support for typing events in threads:
  - Add `parentId` to `TypingStartEvent` and `TypingStopEvent`
  - Add `parentId` to ``ChannelClient::keystroke` and `ChannelClient::stopTyping`
- `ChatClient::sendFile` and `ChatClient::sendImage` each now have just one definition with `ProgressCallback` as an optional parameter. These methods both return `Call<String>`, allowing for sync/async execution, and error handling. The old overloads that were asynchronous and returned no value/error have been removed.
- `FileUploader::sendFile` and `FileUploader::sendImages` variations with `ProgressCallback` are no longer async with no return type. Now they are synchronous with `String?` as return type

## stream-chat-android-offline
- Add support for typing events in threads:
  - Add `parentId` to `Keystroke` and `StopTyping` use cases

## stream-chat-android-ui-common
- Add a new `isMessageRead` flag to the `MessageListItem.MessageItem` class, which indicates
  that a particular message is read by other members in this channel.
- Add handling threads typing in `MessageInputViewModel`

# Jan 31th, 2021 - 4.5.2
## stream-chat-android-client
- Use proper data on `ChatClient::reconnectSocket` to reconnect normal/anonymous user
- Add `enforceUnique` parameter to `ChatClient::sendReaction` and `ChannelClient::sendReaction` methods .
  If reaction is sent with `enforceUnique` set to true, new reaction will replace all reactions the user has on this message.
- Add suspending `setUserAndAwait` extension for `ChatClient`
- Replace chat event listener Kotlin functions with ChatEventListener functional interface in order to promote
  a better integration experience for Java clients. Old methods that use the Kotlin function have been deprecated.
  Deprecated interfaces, such as ChannelController, have not been updated. ChannelClient, which inherits from ChannelController
  for the sake of backwards compatibility, has been updated.

## stream-chat-android-offline
- Add `enforceUnique` parameter to `SendReaction` use case. If reaction is sent with `enforceUnique` set to true,
  new reaction will replace all reactions the user has on this message.
- Fix updating `Message::ownReactions` and `Message:latestReactions` after sending or deleting reaction - add missing `userId` to `Reaction`
- Fix Load Thread Replies process

## stream-chat-android-ui-common
- Add a new `isThreadMode` flag to the `MessageListItem.MessageItem` class.
  It shows is a message item should be shown as part of thread mode in chat.
- Add possibility to set `DateSeparatorHandler` via `MessageListViewModel::setDateSeparatorHandler`
  and `MessageListViewModel::setThreadDateSeparatorHandler` which determines when to add date separator between messages
- Add `MessageListViewModel.Event.ReplyAttachment`, `MessageListViewModel.Event.DownloadAttachment`, `MessageListViewModel.Event.ShowMessage`,
  and `MessageListViewModel.Event.RemoveAttachment` classes.
- Deprecate `MessageListViewModel.Event.AttachmentDownload`

# Jan 18th, 2021 - 4.5.1
## stream-chat-android
- Fix `MessageListItemViewHolder::bind` behavior
- Improve connection/reconnection with normal/anonymous user

## stream-chat-android-client
- Create `ChatClient::getMessagesWithAttachments` to filter message with attachments
- Create `ChannelClient::getMessagesWithAttachments` to filter message with attachments
- Add support for pinned messages:
  - Add `pinMessage` and `unpinMessage` methods `ChatClient` and `ChannelClient`
  - Add `Channel::pinnedMessages` property
  - Add `Message:pinned`, `Message::pinnedAt`, `Message::pinExpires`, and `Message:pinnedBy` properties

# Jan 7th, 2021 - 4.5.0
## stream-chat-android
- Now depends explicitly on AndroidX Fragment (fixes a potential crash with result handling)
- Update AndroidX dependencies: Activity 1.2.0-rc01 and Fragment 1.3.0-rc01

## stream-chat-android-client
- Add filtering non image attachments in ChatClient::getImageAttachments
- Add a `channel` property to `notification.message_new` events
- Fix deleting channel error
- 🚨 Breaking change: ChatClient::unmuteUser, ChatClient::unmuteCurrentUser,
  ChannelClient::unmuteUser, and ChannelClient::unmuteCurrentUser now return Unit instead of Mute

## stream-chat-android-offline
- Add LeaveChannel use case
- Add ChannelData::memberCount
- Add DeleteChannel use case
- Improve loading state querying channels
- Improve loading state querying messages

# Dec 18th, 2020 - 4.4.9

## stream-chat-android-client
- improved event recovery behaviour

## stream-chat-android-offline
- improved event recovery behaviour
- fixed the chatDomain.Builder boolean usage between userPresence and recoveryEnabled

# Dec 18th, 2020 - 4.4.8
## stream-chat-android
- Add filtering `shadowed` messages when computing last channel message
- Add filtering `draft` channels
- Add `DateFormatter::formatTime` method to format only time of a date
- Fix `ChatUtils::devToken` method

## stream-chat-android-client
- Improve `banUser` and `unBanUser` methods - make `reason` and `timeout` parameter nullable
- Add support for shadow ban - add `shadowBanUser` and `removeShadowBan` methods to `ChatClient` and `ChannelClient`
- Add `shadowBanned` property to `Member` class
- Add `ChatClient::getImageAttachments` method to obtain image attachments from a channel
- Add `ChatClient::getFileAttachments` method to obtain file attachments from a channel
- Add `ChannelClient::getImageAttachments` method to obtain image attachments from a channel
- Add `ChannelClient::getFileAttachments` method to obtain file attachments from a channel

## stream-chat-android-offline
- Add filtering `shadowed` messages
- Add new usecase `LoadMessageById` to fetch message by id with offset older and newer messages
- Watch Channel if there was previous error

## stream-chat-android-ui-common
- Add `messageId` arg to `MessageListViewModel`'s constructor allowing to load message by id and messages around it

# Dec 14th, 2020 - 4.4.7
## Common changes for all artifacts
- Updated to Kotlin 1.4.21
- For Java clients only: deprecated the `Call.enqueue(Function1)` method, please use `Call.enqueue(Callback)` instead

## stream-chat-android
- Add new attrs to `MessageListView`: `streamDeleteMessageActionEnabled`, `streamEditMessageActionEnabled`
- Improve Channel List Diff
- Add new attrs to `MessageInputView`: `streamInputScrollbarEnabled`, `streamInputScrollbarFadingEnabled`
- Add API for setting custom message date formatter in MessageListView via `setMessageDateFormatter(DateFormatter)`
  - 24 vs 12 hr controlled by user's System settings.

## stream-chat-android-client
- Add `ChatClient::isValidRemoteMessage` method to know if a RemoteMessage is valid for Stream

## stream-chat-android-offline
- Add updating `channelData` after receiving `ChannelUpdatedByUserEvent`
- Fix crash when a push notification arrives from other provider different than Stream

# Dic 4th, 2020 - 4.4.6

## stream-chat-android
- Use custom `StreamFileProvider` instead of androidx `FileProvider` to avoid conflicts
- Add `ChatClient::setGuestUser` method to login as a guest user
- Make `MessageListItemViewHolder` public and open, to allow customization by overriding the `bind` method

## stream-chat-android-offline
- Centralize how channels are stored locally

# Nov 24th, 2020 - 4.4.5
## Common changes for all artifacts
- Stream SDks has been uploaded to MavenCentral and the GroupID has changed to `io.getstream`.

## stream-chat-android
- New artifact name: `io.getstream:stream-chat-android:STREAM_VERSION`

## stream-chat-android-client
- It's no longer required to wait for `setUser` to finish before querying channels
- `ChatClient::setUser` method allows be called without network connection and will retry to connect when network connection is available
- New artifact name: `io.getstream:stream-chat-android-client:STREAM_VERSION`
- Show date of the last message into channels list when data comes from offline storage
- Show text of the last message into channels list when data comes from offline storage
- Accept Invite Message is now optional, if null value is sent, no message will be sent to the rest of members about this action

## stream-chat-android-offline
- Fix bug when channels with newer messages don't go to the first position in the list
- Fix Offline usage of `ChatDomain`
- New artifact name: `io.getstream:stream-chat-android-offline:STREAM_VERSION`
- Provide the last message when data is load from offline storage

# Nov 24th, 2020 - 4.4.4
This version is a rollback to 4.4.2, The previous release (4.4.3) was not valid due to a problem with the build flow.
We are going to release 4.4.5 with the features introduced by 4.4.3 as soon as the build is back working

# Nov 20th, 2020 - 4.4.3
## stream-chat-android-client
- It's no longer required to wait for `setUser` to finish before querying channels
- `ChatClient::setUser` method allows be called without network connection and will retry to connect when network connection is available

## stream-chat-android-offline
- Fix bug when channels with newer messages don't go to the first position in the list
- Fix Offline usage of `ChatDomain`

# Nov 13th, 2020 - 4.4.2

## stream-chat-android
- Remove `ChatClient` and `ChatDomain` as `ChatUI`'s dependencies
- Replace Glide with Coil - SDK doesn't depend on Glide anymore.
- Remove `BaseStyle` class and extract its properties into `AvatarStyle` and `ReadStateStyle`.
  - Use composition with `AvatarStyle` and `ReadStateStyle` instead of inheriting from `BaseStyle`.
  - Convert to kotlin: `ReadStateView`, `MessageListViewStyle`
- Add `streamShowSendAlsoToChannelCheckbox` attr to `MessageInputView` controlling visibility of "send also to channel" checkbox
- The sample app no longer uses Koin for dependency injection
- Add `streamCopyMessageActionEnabled`, `streamFlagMessageActionEnabled`, and `streamStartThreadMessageActionEnabled` attrs to `MessageListView`
- Validate message text length in MessageInputView.
  - Add property `MessageInputView.maxMessageLength: Int` and show warning once the char limit is exceeded
  - Expose `MessageInputViewModel.maxMessageLength: Int` informing about text length limit of the Channel

## stream-chat-android-client
- Deprecate `User::unreadCount` property, replace with `User::totalUnreadCount`
- Added MarkAllReadEvent
- Fix UpdateUsers call

## stream-chat-android-offline
- Update `totalUnreadCount` when user is connected
- Update `channelUnreadCount` when user is connected
- Fix bug when channels could be shown without names
- Added support for marking all channels as read for the current user.
  - Can be accessed via `ChatDomain`'s use cases (`chatDomain.useCases.markAllRead()...`).
- Fix bug when local channels could be sorted not properly
- Typing events can be all tracked with `ChatDomain.typingUpdates`

# Nov 4th, 2020 - 4.4.1
## Common changes for all artifacts
- Updated dependencies to latest versions (AGP 4.1, OkHttp 4.9, Coroutines 1.3.9, ExoPlayer 2.12.1, etc.)
  - See [PR #757](https://github.com/GetStream/stream-chat-android/pull/757) for full list of version updates
- Revamped `Call` implementations
  - The `Call2` type has been removed, the libraries now all use the same `Call` instead for all APIs
  - `Call` now guarantees callbacks to happen on the main thread
  - Coroutine users can now `await()` a `Call` easily with a provided extension

## stream-chat-android
- Add empty state views to channel list view and message list view components
- Allow setting custom empty state views
- Add loading view to message list view
- Allow setting custom loading view
- Add load more threshold for `MessageListView` and `streamLoadMoreThreshold` attribute
- Fix handling of the `streamShowReadState` attribute on `MessageListView`
- Add `streamShowDeliveredState` XML attribute to `MessageListView`
- Add "loading more" indicator to the `MessageListView`
- Messages in ChannelController were split in messages - New messages and oldMessages for messages coming from the history.

## stream-chat-android-client
- Fix guest user authentication
- Changed API of QuerySort class. You have to specify for what model it is being used.
- Rename `ChannelController` to `ChannelClient`. Deprecate `ChannelController`.
- Replace `ChannelController` subscribe related extension functions with corresponding `ChannelClient` functions
- Move `ChannelClient` extension functions to `io.getstream.chat.android.client.channel` package

## stream-chat-android-offline
- Add GetChannelController use cases which allows to get ChannelController for Channel
- Fix not storing channels when run channels fetching after connection recovery.
- Fix read state getting stuck in unread state

# Oct 26th, 2020 - 4.4.0
## stream-chat-android
- Create custom login screen in sample app
- Bump Coil to 1.0.0
- Add message sending/sent indicators in `MessageListView`
- Add possibility to replace default FileUploader
- Fixes a race condition where client.getCurrentUser() was set too late
- Support for hiding channels
- Makes the number of channels return configurable by adding the limit param to ChannelsViewModelFactory
- Add message sending/sent indicators in `MessageListView`
- Provide ChannelViewModelFactory and ChannelsViewModelFactory by the library to simplify setup
- Fixes for https://github.com/GetStream/stream-chat-android/issues/698 and https://github.com/GetStream/stream-chat-android/issues/723
- Don't show read state for the current user

## stream-chat-android-client
- Fix ConcurrentModificationException in `ChatEventsObservable`
- Add possibility to replace default FileUploader
- Fix anonymous user authentication
- Fix fetching color value from TypedArray

## stream-chat-android-offline
- Channel list now correctly updates when you send a new message while offline. This fixes https://github.com/GetStream/stream-chat-android/issues/698
- Channels now stay sorted based on the QuerySort order (previous behaviour was to sort them once)
- New messages now default to type "regular" or type "ephemeral" if they start with a /
- Improved error logging on sendMessage & sendReaction
- Fixed a race condition that in rare circumstances could cause the channel list to show stale (offline) data
- Fixed a bug with channel.hidden not working correctly
- Fixed crash with absence of user in the UserMap

# Oct 19th, 2020 - 4.3.1-beta-2 (stream-chat-android)
- Allow setting custom `NotificationHandler` in `Chat.Builder`
- Fix unresponsive attachment upload buttons
- Removed many internal implementation classes and methods from the SDK's public API
- Fix sending GIFs from keyboard
- Fix unresponsive attachment upload buttons
- Fix method to obtain initials from user to be shown into the avatar
- Fix method to obtain initials from channel to be shown into the avatar
- Allow setting `ChatLoggerHandler` and `ChatLogLevel` in `Chat.Builder`

# Oct 16th, 2020 - 4.3.1-beta-1 (stream-chat-android)
- Significant performance improvements
- Fix a crash related to behaviour changes in 1.3.0-alpha08 of the AndroidX Fragment library
- Replace Glide with Coil in AttachmentViewHolderMedia (Fix GIFs loading issues)
- `MessageListView.BubbleHelper`'s methods now have nullability annotations, and use primitive `boolean` values as parameters
- Update Offline Support to the [last version](https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.6)

# Oct 16th, 2020 - 0.8.6 (stream-chat-android-offline)
- Improve sync data validation in ChatDomain.Builder
- Removed many internal implementation classes and methods from the SDK's public API
- Significant performance improvements to offline storage
- Default message limit for the queryChannels use case changed from 10 to 1. This is a more sensible default for the channel list view of most chat apps
- Fix QuerySort
- Update client to 1.16.8: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.8

# 1.16.8 - Fri 16th of Oct 2020 (stream-chat-android-client)
- Add `lastUpdated` property to `Channel`

# Oct 14th, 2020 - 4.3.0-beta-6 (stream-chat-android)
- Update to Kotlin 1.4.10
- Fix Typing view behavior
- Fix NPE asking for `Attachment::type`
- Fix ChatDomain initialization issue
- Limit max lines displayed in link previews (5 lines by default, customizable via `streamAttachmentPreviewMaxLines` attribute on `MessageListView`)
- Update Offline Support to the [last version](. See changes: )https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.5)

# 1.16.7 - Wed 14th of Oct 2020 (stream-chat-android-client)
- Removed many internal implementation classes and methods from the SDK's public API
- Improved nullability, restricted many generic type parameters to be non-nullable (set `Any` as their upper bound)
- Use AttachmentsHelper to validate imageUrl instead of just url.

# Oct 14th, 2020 - 0.8.5 (stream-chat-android-offline)
- Use `createdLocallyAt` and `updatedLocallyAt` properties in ChannelController and ThreadController
- Update attachments of message with an old image url, if it's still valid.
- Set attachment fields even if the file upload fails
- Fix NPE while ChatEvent was handled
- Improved nullability, restricted some generic type parameters to be non-nullable (set `Any` as their upper bound)
- Fix method to store date of the last message received into a channel
- Update client to 1.16.7: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.7

# Oct 9th, 2020 - 4.3.0-beta-5 (stream-chat-android)
- Improve selecting non-media attachments
- Fix showing attachments captured with camera
- Add setting type and file size when creating AttachmentMetaData from file
- Remove FileAttachmentListAdapter and methods related to opening files chooser
- Replace isMedia flag with getting type from attachment if possible
- Update ExoPlayer dependency to version [2.12.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#2120-2020-09-11)

# 1.16.6 - Fri 9th of Oct 2020 (stream-chat-android-client)
- Add `createdLocallyAt` and `updatedLocallyAt` properties to `Message` type
- Add AttachmentsHelper with hasValidUrl method

# Oct 7th, 2020 - 4.3.0-beta-4 (stream-chat-android)
- For Java clients, the `bindView` methods used to bind a ViewModel and its UI component together are now available with friendlier syntax.
- Calls such as `MessageListViewModelBindingKt.bindView(...);` should be replaced with calls like `MessageListViewModelBinding.bind(...);`
- The `ChannelListViewModelBindingKt` class has been renamed to `ChannelsViewModelBinding`, to match the name of the ViewModel it's associated with.
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.4

# Oct 7th, 2020 - 0.8.4 (stream-chat-android-offline)
- Update client to 1.16.5: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5

# 1.16.5 - Wed 7th of Oct 2020 (stream-chat-android-client)
- Add autocomplete filter
- Add @JvmOverloads to QueryUsersRequest constructor
- Improve java interop of `TokenManager`

# Oct 5th, 2020 - 0.8.3 (stream-chat-android-offline)
- Improved message attachment handling. Message is now first added to local storage and the attachment is uploaded afterwards.
- Editing messages now works while offline
- Deprecate SendMessageWithAttachments in favor of SendMessage while specifying attachment.upload
- Fix a bug that caused messages not to load if member limit wasn't specified
- Fix a crash related to reaction data structure
- Fix a bug where network errors (temporary ones) are detected as permanent errors

# 1.16.4 - Mon 5th of Oct 2020 (stream-chat-android-client)
- Add `attachment.upload` and `attachment.uploadState` fields for livedata upload status. These fields are currently unused if you only use the low level client.

# Oct 2nd, 2020 - 4.3.0-beta-3 (stream-chat-android)
- Removed several parameters of `BaseAttachmentViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `AttachmentViewHolderFactory` as constructor parameters
- Moved `BaseAttachmentViewHolder` to a new package
- Fix setting read state when user's last read equals message created date
- Skip setting user's read status if last read message is his own
- Make MessageListItem properties abstract
- Change default query sort to "last_updated"
- Fixed attachments logic. Save previously attached files when add more.
- Fixed the bug when it was unable to select new files when you have already attached something.
- Moved `MessageInputView` class to a new package.
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.2

# Oct 2nd, 2020 - 0.8.2 (stream-chat-android-offline)
- Request members by default when querying channels

# Sep 30th, 2020 - 4.3.0-beta-2 (stream-chat-android)
- Removed several parameters of `BaseMessageListItemViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `MessageViewHolderFactory` as constructor parameters
- Attachment customization methods moved from `MessageViewHolderFactory` to a separate `AttachmentViewHolderFactory` class
- Removed `position` parameter from `MessageClickListener`
- Moved `BaseMessageListItemViewHolder` to a new package
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.1

# Sep 30th, 2020 - 0.8.1 (stream-chat-android-offline)
- Handle the new `ChannelUpdatedByUserEvent`
- Update client to 1.16.1: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Improve online status handling
- Replace posting an empty channels map when the channels query wasn't run online and offline storage is empty with error

# 1.16.2 - Wed 30 Sep 2020 (stream-chat-android-client)
- Add `ChatClient::enableSlowMode` method to enable slow mode
- Add `ChatClient::disableSlowMode` method to disable slow mode
- Add `ChannelController::enableSlowMode` method to enable slow mode
- Add `ChannelController::disableSlowMode` method to disable slow mode
- Add `Channel::cooldown` property to know how configured `cooldown` time for the channel
- Fix FirebaseMessageParserImpl.verifyPayload() logic
- Fix notification display condition
- Fix Socket connection issues

# 1.16.1 - Wed 25 Sep 2020 (stream-chat-android-client)
- Remove `User` field on `ChannelUpdatedEvent`
- Add new chat event type -> `ChannelUpdatedByUserEvent`
- Add `ChatNotificationHandler::getFirebaseInstanceId` method to provide a custom `FirebaseInstanceId`
- Add `NotificationConfig::useProvidedFirebaseInstance` conf

# Sep 23rd, 2020 - 4.3.0-beta-1 (stream-chat-android)
- Update livedata/client to latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# 1.16.0 - Wed 23 Sep 2020 (stream-chat-android-client)
- Removed message.channel, this is a backwards incompatible change
- Ensure that message.cid is always available

The SDK was providing message.cid and message.channel in some cases, but not always.
Code that relied on those fields being populated caused bugs in production.

If you were relying on message.channel it's likely that you were running into bugs.
We recommend using one of these alternatives:

- message.cid if you just need a reference to the channel
- the channel object provided by client.queryChannel(s) if you need the full channel data
- channelController.channelData livedata object provided by the livedata package (automatically updated if channel data changes)
- channelController.toChannel() function provided by the livedata package

# Sep 23rd, 2020 - 0.8.0 (stream-chat-android-offline)
- Update client to 1.16.0: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# Sep 23rd, 2020 - 0.7.7 (stream-chat-android-offline)
- Fix crash when map channels DB entity to Channel
- Add posting empty channels map when queryChannels fails either offline and online which prevents infinite loader

# 1.15.6 - Wed 23 Sep 2020 (stream-chat-android-client)
- Convert ChatError to plain class. Changes in ChatLogger interface.
- Update events fields related to read status - remove "unread_messages" field and add "unread_channels" to NewMessageEvent, NotificationMarkReadEvent, and NotificationMessageNewEvent
- Mark ChatEvents containing the user property by the UserEvent interface.
- Simplified the event handling APIs, deprecated `ChatObservable`. See [the migration guide](https://github.com/GetStream/stream-chat-android-client/wiki/Migrating-from-the-old-event-subscription-APIs) for details on how to easily adopt the new APIs.

# Sep 23rd, 2020 - 4.2.11-beta-13 (stream-chat-android)
- Adjust ChatSocketListener to new events(NewMessageEvent, NotificationMarkReadEvent, NotificationMessageNewEvent) properties.
- Fix "load more channels"
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.6
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.7

# Sep 18th, 2020 - 4.2.11-beta-12 (stream-chat-android)
- Implement Giphy actions handler
- Fix .gif preview rendering on message list
- Fix thread shown issue after sending message to a channel
- Remove border related attributes from MessageInputView. Add close button background attribute to MessageInputView.
- Improve setting user in sample app
- Add updating message read state after loading first messages
- Wrap Attachment into AttachmentListItem for use in adapter
- Properly show the message date
- Revamp MessageListView adapter customization, introduce ListenerContainer to handle all ViewHolder listeners
- Fix default filters on `ChannelsViewModelImpl`
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.5
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.6

# Sep 18th, 2020 - 0.7.6 (stream-chat-android-offline)
- Store needed users in DB
- Stop trying to execute background sync in case ChatDomain.offlineEnabled is set to false
- Fix Socket Connection/Reconnection
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.5

# 1.15.5 - Fri 18 Sep 2020 (stream-chat-android-client)
- Fix Socket Connection/Reconnection

# Sep 15th, 2020 - 0.7.5 (stream-chat-android-offline)
- Fix offline support for adding and removing reactions
- Fix crash when creating a channel while channel.createdBy is not set

# Sep 14th, 2020 - 0.7.4 (stream-chat-android-offline)
- Remove duplicates of new channels
- Improve tests
- Remove some message's properties that are not used anymore GetStream/stream-chat-android-client#69
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.4

# 1.15.4 - Fri 11 Sep 2020 (stream-chat-android-client)
- Fix Socket Disconnection
- Remove useless message's properties (isStartDay, isYesterday, isToday, date, time and commandInfo)
- Forbid setting new user when previous one wasn't disconnected

# Sep 8th, 2020 - 0.7.3 (stream-chat-android-offline)
- Add usecase to send Giphy command
- Add usecase to shuffle a Gif on Giphy command message
- Add usecase to cancel Giphy Command
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.3

# 1.15.3 - Tue 7 Sep 2020 (stream-chat-android-client)
- Add send action operation to ChannelController
- Fix serialized file names of SendActionRequest
- Fix `ConnectedEvent` parse process

# Sep 4th, 2020 - 4.2.11-beta-11 (stream-chat-android)
- Fix uploading files and capturing images on Android >= 10
- Fix `AvatarView`: Render lastActiveUsers avatars when channel image is not present

# 1.15.2 - Tue 1 Sep 2020 (stream-chat-android-client)
- `ChannelResponse.watchers` is an array of User now
- `Watcher` model has been removed, `User` model should be used instead
- `QueryChannelsRequet` has a new field called `memberLimit` to limit the number of members received per channel

# Aug 28th, 2020 - 4.2.11-beta-9 (stream-chat-android)
- Update event structure
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.2

# 1.15.1 - Thu 28 Aug 2020 (stream-chat-android-client)
- New MapAdapter that omit key that contains null values or emptyMaps
- Null-Check over Watchers response

## Aug 23th, 2020 - 4.2.11-beta-8 (stream-chat-android)
- Fix Upload Files
- Update RecyclerView Lib
- Update Notification Customization

# Aug 28th, 2020 - 0.7.2 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1

# Aug 28th, 2020 - 0.7.1 (stream-chat-android-offline)
- Keep order when retry to send a message
- Fix message sync logic and message sending success event emitting
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.0

# Aug 20th, 2020 - 0.7.0 (stream-chat-android-offline)
- Update to version 0.7.0

# 1.15.0 - Thu 20 Aug 2020 (stream-chat-android-client)
- Refactor ChatEvents Structure

# 1.14.0 - Thu 20 Aug 2020 (stream-chat-android-client)
- Decouple cloud messages handler logic from configuration data
- Fix createChannel methods

# 1.13.3 - Tue 18 Aug 2020 (stream-chat-android-client)
- Set message as optional when updating a channel

# 1.13.2 - Fri 14 Aug 2020 (stream-chat-android-client)
- Reduce TLS Latency

# 1.13.1 - Fri 7 Aug 2020 (stream-chat-android-client)
- Fix DateParser

## Aug 5th, 2020 - 4.2.11-beta-7 (stream-chat-android)
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.6.9
- Fix channel name validation in CreateChannelViewModel
- Add `ChannelsView.setViewHolderFactory(factory: ChannelViewHolderFactory)` function
- Fix Fresco initialization
- Fix method to add/remove reaction

# Aug 3nd, 2020 - 0.6.9 (stream-chat-android-offline)
- Fix `QuerySort`

# 1.13.0 - Tue 28 Jul 2020 (stream-chat-android-client)
- Add `Client.flagUser()` method to flag an User
- Add `Client.flagMessage()` method to flag a Message
- Deprecated method `Client.flag()` because was a bit confusing, you should use `client.flagUser()` instead

# 1.12.3 - Mon 27 Jul 2020 (stream-chat-android-client)
- Fix NPE on TokenManagerImpl
- Upgrade Kotlin to version 1.3.72
- Add Kotlin Proguard Rules

# Jul 20th, 2020 - 0.6.8 (stream-chat-android-offline)
- Fix `NotificationAddedToChannelEvent` event handling

# 1.12.2 - Fri 17 Jul 2020 (stream-chat-android-client)
- Add customer proguard rules

# 1.12.1 - Wed 15 Jul 2020 (stream-chat-android-client)
- Add customer proguard rules

## Jul 13th, 2020 - 4.2.11-beta-6 (stream-chat-android)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.10.0
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.6.7
- Refactor ChannelHeaderView
- Refactor MessageInputView
- Refactor Permission Checker Behavior
- Refactor MessageListVIew
- Fix Send Attachment Behavior
- Fix "Take Picture/Record Video" Behavior
- Add option to show empty view when there are no channels
- Add option to send a message to a thread
- Allow to switch user / logout

# 1.12.0 - Mon 06 Jul 2020 (stream-chat-android-client)
- Add mute and unmute methods to channel controller

# 1.11.0 - Mon 06 Jul 2020 (stream-chat-android-client)
- Fix message mentioned users

# Jul 3nd, 2020 - 0.6.7 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.10.0
- Implement Thread Behavior

# 1.10.0 - Wed 29 June 2020 (stream-chat-android-client)
- Add mute and unmute channels
- Add `notification.channel_mutes_updated` socket even handling
- Add user.channelMutes field
- Improve error logging
- Add invalid date format handling (channel.config dates might be invalid)

# 1.9.3 - Wed 29 June 2020 (stream-chat-android-client)
- Add raw socket events logging. See with tag `Chat:Events`

# Jun 24th, 2020 - 0.6.6 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.9.2

# 1.9.2 - Wed 24 June 2020 (stream-chat-android-client)
- Add `show_in_channel` attribute to `Message` entity

# 1.9.1 - Mue 23 June 2020 (stream-chat-android-client)
- Fix multithreaded date parsing

# 1.9.0 - Mon 22 June 2020 (stream-chat-android-client)
- Fix search message request body
  🚨 Breaking change:
- client.searchMessages signature has been changed: query removed, added channel filter

# 1.8.1 - Thu 18 June 2020 (stream-chat-android-client)
- Fix UTC date for sync endpoint
- Fix inhered events parsing
- Fix custom url setter of ChatClient.Builder

# Jun 16th, 2020 - 0.6.5 (stream-chat-android-offline)
- Fixed crash caused by `NotificationMarkReadEvent.user` value being sent null.
- Solution: using the current user which was set to the ChatDomain instead of relying on event's data.

# 1.8.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add sync api call

# Jun 12th, 2020 - 0.6.4 (stream-chat-android-offline)
- Add attachment.type when upload a file or image

# 1.7.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add query members call

# Jun 11th, 2020 - 0.6.3 (stream-chat-android-offline)
- Create a new UseCase to send messages with attachments

# Jun 11th, 2020 - 0.6.2 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.6.1

# 1.6.1 - Thu 11 June 2020 (stream-chat-android-client)
- Add MimeType on sendFile and sendImage methods

# 1.6.0 - Mon 8 June 2020 (stream-chat-android-client)
- Add translations api call and update message with `i18n` field. Helper `Message` extensions functions are added.

## Jun 4th, 2020 - 4.2.11-beta-5 (stream-chat-android)
- Update livedata dependency to fix crash when NotificationMarkReadEvent received
- Add mavenLocal() repository

## Jun 4th, 2020 - 4.2.11-beta-4 (stream-chat-android)
- Fix crash when command (`/`) is typed.

## Jun 3rd, 2020 - 4.2.11-beta (stream-chat-android)
- Fix `AvatarView` crash when the view is not attached

# 1.5.4 - Wed 3 June 2020 (stream-chat-android-client)
- Add optional `userId` parameter to `Channel.getUnreadMessagesCount` to filter out unread messages for the user

# 1.5.3 - Wed 3 June 2020 (stream-chat-android-client)
- Fix switching users issue: `disconnect` and `setUser` resulted in wrong user connection

# 1.5.2 - Tue 2 June 2020 (stream-chat-android-client)
- Fix `ConcurrentModificationException` on multithread access to socket listeners

# May 30th, 2020 - 0.6.1 (stream-chat-android-offline)
- Use the new low level client syntax for creating a channel with members
- Fallback to a default channel config if the real channel config isn't available yet. This fixes GetStream/stream-chat-android#486

# May 27th, 2020 - 0.6.0 (stream-chat-android-offline)
- Update client to the latest version: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.5.0

# 1.5.1 - Wed 27 May 2020 (stream-chat-android-client)
- Add filter contains with any value

# May 26th, 2020 - 0.5.2 (stream-chat-android-offline)
- Test cases for notification removed from channel had the wrong data structure. This caused a crash when this event was triggered.

# 1.5.0 - Mon 26 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Add new constructor field to `Channel`: `team`
- Add new constructor field to `User`: `teams`

✅ Other changes:
- Add `Filter.contains`

# 1.4.17 - Mon 26 May 2020 (stream-chat-android-client)
- Fix loop on client.create
- Fix crash when backend sends first event without me

# May 25th, 2020 - 0.5.1 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.16

# 1.4.16 - Mon 25 May 2020 (stream-chat-android-client)
Breaking change:
- `Command` fields are mandatory and marked as non-nullable

# May 24th, 2020 - 0.5.0 (stream-chat-android-offline)
Livedata now supports all events exposed by the chat API. The 3 new events are:
- Channel truncated
- Notification channel truncated
- Channel Deleted
  This release also improves how new channels are created.

# May 23rd, 2020 - 0.4.8 (stream-chat-android-offline)
- NotificationMessageNew doesn't specify event.message.cid, this was causing issues with offline storage. The test suite has been updated and the issue is now resolved. Also see: GetStream/stream-chat-android#490

# May 23rd, 2020 - 0.4.7 (stream-chat-android-offline)
- Fixed NPE on MemberRemoved event GetStream/stream-chat-android#476
- Updates low level client to fix GetStream/stream-chat-android#492

# 1.4.15 - Fri 22 May 2020 (stream-chat-android-client)
- Add events: `ChannelTruncated`, `NotificationChannelTruncated`, `NotificationChannelDeleted`

# 1.4.13 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Fields `role` and `isInvited` of ``Member` fields optional

# 1.4.12 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `Member` model is cleaned up from non existing fields

# May 20th, 2020 - 0.4.6 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.11

# 1.4.11 - Tue 19 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix null fields which are not marked as nullable

# 1.4.10 - Tue 19 May 2020 (stream-chat-android-client)
- Fix add member invalid api key

# 1.4.9 - Mon 18 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix `ChannelController.markRead`: was marking read all channels instead of current one
- `ChatClient.markRead` accepts optional `messageId`

# 1.4.8 - Mon 18 May 2020 (stream-chat-android-client)
- Add handling invalid event payload

# May 16th, 2020 - 0.4.5 (stream-chat-android-offline)
- Improved handling of unread counts. Fixes GetStream/stream-chat-android#475

# May 16th, 2020 - 0.4.4 (stream-chat-android-offline)
- GetStream/stream-chat-android#476

## May 15th, 2020 - 4.2.10-beta (stream-chat-android)
- Update to the latest livedata: 0.6.1

# May 15th, 2020 - 0.4.3 (stream-chat-android-offline)
- Resolves this ticket: GetStream/stream-chat-android#479

## May 29th, 2020 - 4.2.9-beta-3 (stream-chat-android)
- Fix AttachmentViewHolder crash when user sends message with plain/no-media url

## May 15th, 2020 - 4.2.9-beta-2 (stream-chat-android)
- Update to the latest livedata: 0.6.0

## May 15th, 2020 - 4.2.8-beta-1 (stream-chat-android)
- Update to the latest livedata: 0.4.6

## May 15th, 2020 - 4.2.6 (stream-chat-android)
- Fix Avatar crash if channel/user initials are empty

# 1.4.7 - Tue 14 May 2020 (stream-chat-android-client)
- Add more channel creation signatures to `Client` and `ChannelController`

# 1.4.6 - Tue 14 May 2020 (stream-chat-android-client)
- Move channel out of message constructor

## May 13th, 2020 - 4.2.5 (stream-chat-android)
- Create new `AvatarView`
- Glide Redirect issues resolved
- Bugfix release for livedata, updated to 0.4.2

# May 13th, 2020 - 0.4.2 (stream-chat-android-offline)
-NotificationAddedToChannelEvent cid parsing didn't work correctly. This has been fixed in 0.4.2

# May 13th, 2020 - 0.4.1 (stream-chat-android-offline)
- There was an issue with the 0.4.0 and the data structure for NotificationMarkRead

# May 13th, 2020 - 0.4.0 (stream-chat-android-offline)
## Features:
- Massive improvement to javadoc/dokka
- Support for user ban events. Exposed via chatDomain.banned
- Muted users are available via chatDomain.muted
- Support for notificationMarkRead, invite and removed from channel events
- Support for deleting channels
- Support for silent messages
- Creating channels with both members and additional data works now
- User presence is enabled

##Bugfixes:
- No longer denormalizing channelData.lastMessageAt
- Fixed an issue with channel event handling and the usage of channel.id vs channel.cid
- Changed channelData.createdBy from lateinit to a regular field

##Other:
- Moved from Travis to Github actions

# 1.4.5 - Tue 12 May 2020 (stream-chat-android-client)
- add message.silent field
- add extension properties `name` and `image` to `Channel` and `User`

## March 11th, 2020 - 3.6.5 (stream-chat-android)
- Fix reaction score parser casting exception

# May 8th, 2020 - 0.3.4 (stream-chat-android-offline)
- added support for muting users
- store the current user in offline storage
- performance tests
- removed launcher icons from lib
- forward compatibility with new event sync endpoint
- support for reaction scores

# 1.4.3 - Thu 7 May 2020 (stream-chat-android-client)
- fix type erasure of parsed collections: `LinkedTreeMap`, but not `List<Reaction>`

# 1.4.2 - Mon 4 May 2020 (stream-chat-android-client)
- add `reactionScores` to `Message`
- fix null write crash of CustomObject nullable field
- fix extraData duplicated fields

# May 2nd, 2020 - 0.3.1 (stream-chat-android-offline)
- Make the channel unread counts easily accessible via channel.unreadCount
- Support for muting users
- Detection for permanent vs temporary errors (which helps improve retry logic)
- Bugfix: Fixes edge cases where recovery flow runs before the existing API calls complete

# 1.4.0 - Fri 1 May 2020 (stream-chat-android-client)
- fix `QueryChannelRequest` when `withMessages/withMembers` is called, but messages were not returned
- add `unreadMessages` to `ChannelUserRead`. Add extension for channel to count total unread messages: `channel.getUnreadMessagesCount()`

# 1.3.0 - Wed 30 Apr 2020 (stream-chat-android-client)
🚨 Breaking changes:
- `TokenProvider` signature enforces async execution
- make socket related classes internal

✅ Other changes
- fix endlessly hanging request in case setUser is not called
- fix expired token case on socket connection
- fix client crash if TokenProvider throws an exception

# Apr 29th, 2020 - 0.3.0 (stream-chat-android-offline)
- Handle edge cases where events are received out of order
- KTlint, travis and coverage reporting
- Interfaces for use cases and controllers for easier testing
- Channel data to isolate channel data vs rest of channel state
- Java version of code examples
- Handle edge cases for channels with more than 100 members
- Test coverage on mark read
- Bugfix queryChannelsController returning duplicate channels
- Support for hiding and showing channels
- Full offline pagination support (including the difference between GTE and GT filters)

# 1.2.2 - Wed 29 Apr 2020 (stream-chat-android-client)
🚨 Breaking changes:
- fields of models are moved to constructors: `io.getstream.chat.android.client.models`
- field of Device `push_provider` renamed to `pushProvider` and moved to constructor

✅ Other changes
- added local error codes with descriptions: `io.getstream.chat.android.client.errors.ChatErrorCode`
- fix uncaught java.lang.ExceptionInInitializerError while parsing custom object

# Apr 22nd, 2020 - 0.2.1 (stream-chat-android-offline)
- Better handling for missing cids

# Apr 22nd, 2020 - 0.2.0 (stream-chat-android-offline)
- Test suite > 100 tests
- Sample app (stream-chat-android) works
- Full offline sync for channels, messages and reactions
- Easy to use livedata objects for building your own UI

# Apr 22nd, 2020 - 0.1.0 (stream-chat-android-offline)
- First Release

## March 3rd, 2020 - 3.6.5 (stream-chat-android)
- Fix crash on sending Google gif

## March 3rd, 2020 - 3.6.4 (stream-chat-android)
- Update default endpoint: from `chat-us-east-1.stream-io-api.com` to `chat-us-east-staging.stream-io-api.com`
- update target api level to 29
- Fixed media playback error on api 29 devices
- Added score field to reaction model

## January 28th, 2020 - 3.6.3 (stream-chat-android)
- ViewModel & ViewHolder classes now use protected instead of private variables to allow customization via subclassing
- ChannelViewHolderFactory is now easier to customize
- Added ChannelViewHolder.messageInputText for 2 way data binding
- Documentation improvements
- Fix problem with wrong scroll position

## January 10th, 2020 - 3.6.2 (stream-chat-android)
- Enable multiline edit text
- Fix deprecated getColumnIndexOrThrow for 29 Api Level

## January 7th, 2020 - 3.6.1 (stream-chat-android)
- Add navigation components with handler to override default behaviour

## Breaking changes:
###
- `OpenCameraViewListener` is replaced with CameraDestination

## January 6th, 2020 - 3.6.0 (stream-chat-android)
- Add `MessageSendListener` interface for sending Message
- Update `README` about Customizing MessageInputView
- Client support for anonymous and guest users
- Client support initialization with Configurator
- Support auto capitalization for keyboard
- Add `NotificationManager` with customization opportunity
- Update `UpdateChannelRequest` for reserved fields
- renamed `MoreActionDialog` to `MessageMoreActionDialog`
- Add `StreamLoggerHandler` interface for custom logging client data
- Add logging customization ability
- fix markdown for mention if there is no space at prefix @
- fix Edit Attachment behavior
- add support for channel.hide with clear history + events
- Fix crash in AttachmentActivity and AttachmentDocumentActivity crash when app is killed in background
- Add utility method StreamChat.isConnected()

#### Breaking changes:

##### Channel hide request
- `Channel:hide` signature has changed: `HideChannelRequest` must be specified as first parameter
- `Client:hideChannel` signature has changed: `HideChannelRequest` must be specified as second parameter
- `ChannelListViewModel:hideChannel` signature has changed: `HideChannelRequest` must be specified as second parameter

##### How to upgrade
To keep the same behavior pass `new HideChannelRequest()` as request parameter to match with the new signature.

## December 9th, 2019 - 3.5.0 (stream-chat-android)
- Fix set typeFace without custom font
- Fix channel.watch (data payload was not sent)
- Fix API 23 compatibility
- Add Attachment Border Color attrs
- Add Message Link Text Color attrs
- Add custom api endpoint config to sample app and SDK

## November 28th, 2019 - 3.4.1 (stream-chat-android)
- Fix Giphy buttons alignments
- Add Giphy error cases handling
- Update http related issues documentation


## November 28th, 2019 - 3.4.0 (stream-chat-android)
- Custom font fot the whole SDK
- Custom font per TextView
- Ignore sample app release unit tests, keep debug tests
- Added AttachmentBackgroundColorMine/Theirs
- Fix Edit/Delete thread parent message
- Replace fadein/fadeout animation of parent/current thread with default RecyclerView animation

## November 5th, 2019 - 3.3.0 (stream-chat-android)
- Fix Concurrent modification when removing member from channel
- Fix automention input issue
- Fix Sent message progress infinite
- Fix channel delete event handling in ChannelList view model
- Fix attachment duplicated issue when message edit
- Add File Upload 2.0
- Add editMessage function in Channel View Model
- Fix JSON encoding always omits null fields
- Sample app: add version header, release version signing
- Add Message Username and Date attrs


## November 5th, 2019 - 3.2.1 (stream-chat-android)
- Fixed transparency issues with user profile images on older devices
- Better channel header title for channels without a name
- Fixed read count difference between own and other users' messages
- Fixed Video length preview
- Catch error body parsing errors
- Do not show commands list UI when all commands are disabled
- Renamed `MessageInputClient` to `MessageInputController`
- Added Large file(20MB) check for uploading file
- Added streamUserNameShow and streamMessageDateShow in `MessageListViewStyle`
- Fixed channel header title position issue when Last Active is hidden


## October 25th, 2019 - 3.2.0 (stream-chat-android)
- Added event interceptors to `ChannelListViewModel`

## October 24th, 2019 - 3.1.0 (stream-chat-android)
- Add channel to list when the user is added
- Add `onUserDisconnected` event
- Make sure channel list view model is cleared when the user disconnects
- Fix bug with `setUser` when user data is not correctly URI encoded
- Add debug/info logging
- Add Attrs for DateSeparator

## Oct 23th, 2019 - 3.0.2 (stream-chat-android)
- Fix NPE with restore from background and null users

## Oct 22th, 2019 - 3.0.1 (stream-chat-android)
- Fix NPE with empty channel lists

## Oct 21th, 2019 - 3.0.0 (stream-chat-android)
- Added support for message search `client.searchMessages`
- Better support for query user options
- Update channel update signature
- Fix disconnection NPE
- Minor bugfixes
- Remove file/image support
- Expose members and watchers pagination options for query channel

#### Breaking changes
- `Channel.update` signature has changed

## Oct 16th, 2019 - 2.3.0 (stream-chat-android)
- Added support for `getReactions` endpoint
- Calls to `ChannelListViewModel#setChannelFilter` will reload the list of channels if necessary
- Added support for `channel.stopWatching()`
- Improved error message for uploading large files
- Remove error messages after you send a message (similar behaviour to Slack)
- Fixed slash command support on threads
- Improved newline handling
- Improved thread display
- Expose ban information for current user (`User#getBanned`)
- Bugfix on attachment size
- Added support for accepting and rejecting channel invites
- Expose current user LiveData with `StreamChat.getCurrentUser()`

## Oct 14th, 2019 - 2.2.1 (stream-chat-android)
- Renamed `FileSendResponse` to `UploadFileResponse`
- Renamed `SendFileCallback` to `UploadFileCallback`
- Removed `SendMessageRequest`
- Updated `sendMessage` and `updateMessage` from `Client`
- Added devToken function for setUser of Client
- Added a callback as an optional last argument for setUser functions
- Added ClientState which stores users, current user, unreadCount and the current user's mutes
- Added notification.mutes_updated event
- Add support for add/remove channel members
- Expose channel unread messages counts for any user in the channel

## Oct 9, 2019 - 2.2.0 (stream-chat-android)
- Limit message input height to 7 rows
- Fixed thread safety issues on Client.java
- Fixed serialization of custom fields for message/user/channel and attachment types
- Added support for distinct channels
- Added support to Channel hide/show
- Improved client error reporting (we now return a parsed error response when available)
- General improvements to Message Input View
- Added ReactionViewClickListener
- Added support for banning and unbanning users
- Added support for deleting a channel
- Add support for switching users via `client.disconnect` and `client.setUser`
- Add `reload` method to `ChannelListViewModel`
- Bugfix: hides attachment drawer after deny permission
- Add support for update channel endpoint
- Add PermissionRequestListener for Permission Request

## September 28, 2019 - 2.1.0 (stream-chat-android)
- Improved support for regenerating expired tokens

#### Breaking changes:
- `MessageInputView#progressCapturedMedia(int requestCode, int resultCode, Intent data)` renamed into `captureMedia(int requestCode, int resultCode, Intent data)`
- `binding.messageInput.permissionResult(requestCode, permissions, grantResults)` in `onRequestPermissionsResult(requestCode, permissions, grantResults) of `ChannelActivity`

## September 28, 2019 - 2.0.1 (stream-chat-android)
- Fix channel list ordering when a channel is added directly from Android
- Better Proguard support

## September 26, 2019 - 2.0.0 (stream-chat-android)
- Simplify random access to channels
- Channel query and watch methods now work the same as they do on all other SDKs

#### Breaking changes:
- `channel.query` does not watch the channel anymore, to retrieve channel state and watch use `channel.watch`
- `client.getChannelByCID` is now private, use one of the `client.channel` methods to get the same (no null checks needed)
