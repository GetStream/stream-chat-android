# To be released:
## Common changes for all artifacts

## stream-chat-android-ui-component

## stream-chat-android

## stream-chat-android-client

## stream-chat-android-offline

# Nov 13th, 2020 - 4.4.2
## stream-chat-android-ui-component
Added new MessageInputView structure

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
- Typing events can be all tracked from `ChatDomain`

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
- Refactor MesageInputView
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
- Fix API 23 compatiblity
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
