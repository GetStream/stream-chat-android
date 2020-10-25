# To be released:
## stream-chat-anddroid
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

# 1.16.5 - Wed 7th of Oct 2020 (stream-chat-android-client)
- Add autocomplete filter
- Add @JvmOverloads to QueryUsersRequest constructor
- Improve java interop of `TokenManager`

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

# Sep 30th, 2020 - 4.3.0-beta-2 (stream-chat-android)
- Removed several parameters of `BaseMessageListItemViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `MessageViewHolderFactory` as constructor parameters
- Attachment customization methods moved from `MessageViewHolderFactory` to a separate `AttachmentViewHolderFactory` class
- Removed `position` parameter from `MessageClickListener`
- Moved `BaseMessageListItemViewHolder` to a new package
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.1

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

# 1.15.5 - Fri 18 Sep 2020 (stream-chat-android-client)
- Fix Socket Connection/Reconnection

# 1.15.4 - Fri 11 Sep 2020 (stream-chat-android-client)
- Fix Socket Disconnection
- Remove useless message's properties (isStartDay, isYesterday, isToday, date, time and commandInfo)
- Forbid setting new user when previous one wasn't disconnected

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

# 1.13.0 - Tue 28 Jul 2020 (stream-chat-android-client)
- Add `Client.flagUser()` method to flag an User
- Add `Client.flagMessage()` method to flag a Message
- Deprecated method `Client.flag()` because was a bit confusing, you should use `client.flagUser()` instead

# 1.12.3 - Mon 27 Jul 2020 (stream-chat-android-client)
- Fix NPE on TokenManagerImpl
- Upgrade Kotlin to version 1.3.72
- Add Kotlin Proguard Rules

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

# 1.10.0 - Wed 29 June 2020 (stream-chat-android-client)
- Add mute and unmute channels
- Add `notification.channel_mutes_updated` socket even handling
- Add user.channelMutes field
- Improve error logging
- Add invalid date format handling (channel.config dates might be invalid)

# 1.9.3 - Wed 29 June 2020 (stream-chat-android-client)
- Add raw socket events logging. See with tag `Chat:Events`

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

# 1.8.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add sync api call

# 1.7.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add query members call

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

# 1.5.1 - Wed 27 May 2020 (stream-chat-android-client)
- Add filter contains with any value

# 1.5.0 - Mon 26 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Add new constructor field to `Channel`: `team`
- Add new constructor field to `User`: `teams`

✅ Other changes:
- Add `Filter.contains`

# 1.4.17 - Mon 26 May 2020 (stream-chat-android-client)
- Fix loop on client.create
- Fix crash when backend sends first event without me

# 1.4.16 - Mon 25 May 2020 (stream-chat-android-client)
Breaking change:
- `Command` fields are mandatory and marked as non-nullable

# 1.4.15 - Fri 22 May 2020 (stream-chat-android-client)
- Add events: `ChannelTruncated`, `NotificationChannelTruncated`, `NotificationChannelDeleted`

# 1.4.13 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Fields `role` and `isInvited` of ``Member` fields optional

# 1.4.12 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `Member` model is cleaned up from non existing fields

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

## May 15th, 2020 - 4.2.10-beta (stream-chat-android)
- Update to the latest livedata: 0.6.1

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

# 1.4.5 - Tue 12 May 2020 (stream-chat-android-client)
- add message.silent field
- add extension properties `name` and `image` to `Channel` and `User`

## March 11th, 2020 - 3.6.5 (stream-chat-android)
- Fix reaction score parser casting exception

# 1.4.3 - Thu 7 May 2020 (stream-chat-android-client)
- fix type erasure of parsed collections: `LinkedTreeMap`, but not `List<Reaction>`

# 1.4.2 - Mon 4 May 2020 (stream-chat-android-client)
- add `reactionScores` to `Message`
- fix null write crash of CustomObject nullable field
- fix extraData duplicated fields

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

# 1.2.2 - Wed 29 Apr 2020 (stream-chat-android-client)
🚨 Breaking changes:
- fields of models are moved to constructors: `io.getstream.chat.android.client.models`
- field of Device `push_provider` renamed to `pushProvider` and moved to constructor

✅ Other changes
- added local error codes with descriptions: `io.getstream.chat.android.client.errors.ChatErrorCode`
- fix uncaught java.lang.ExceptionInInitializerError while parsing custom object

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
