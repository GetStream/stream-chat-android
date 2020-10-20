# To be released:
- Allow setting custom `NotificationHandler` in `Chat.Builder`
- Fix unresponsive attachment upload buttons
- Fix sending GIFs from keyboard
- Removed many internal implementation classes and methods from the SDK's public API
- Allow specifying a custom `ChatLogger.Config` in `Chat.Builder`

# Oct 16th, 2020 - 4.3.1-beta-1
- Significant performance improvements
- Fix a crash related to behaviour changes in 1.3.0-alpha08 of the AndroidX Fragment library
- Replace Glide with Coil in AttachmentViewHolderMedia (Fix GIFs loading issues)
- `MessageListView.BubbleHelper`'s methods now have nullability annotations, and use primitive `boolean` values as parameters
- Update Offline Support to the [last version](https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.6)

# Oct 14th, 2020 - 4.3.0-beta-6
- Update to Kotlin 1.4.10
- Fix Typing view behavior
- Fix NPE asking for `Attachment::type`
- Fix ChatDomain initialization issue
- Limit max lines displayed in link previews (5 lines by default, customizable via `streamAttachmentPreviewMaxLines` attribute on `MessageListView`)
- Update Offline Support to the [last version](. See changes: )https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.5)

# Oct 9th, 2020 - 4.3.0-beta-5
- Improve selecting non-media attachments
- Fix showing attachments captured with camera
- Add setting type and file size when creating AttachmentMetaData from file
- Remove FileAttachmentListAdapter and methods related to opening files chooser
- Replace isMedia flag with getting type from attachment if possible
- Update ExoPlayer dependency to version [2.12.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#2120-2020-09-11)

# Oct 7th, 2020 - 4.3.0-beta-4
- For Java clients, the `bindView` methods used to bind a ViewModel and its UI component together are now available with friendlier syntax.
- Calls such as `MessageListViewModelBindingKt.bindView(...);` should be replaced with calls like `MessageListViewModelBinding.bind(...);`
- The `ChannelListViewModelBindingKt` class has been renamed to `ChannelsViewModelBinding`, to match the name of the ViewModel it's associated with.
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.4

# Oct 2nd, 2020 - 4.3.0-beta-3
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

# Sep 30th, 2020 - 4.3.0-beta-2
- Removed several parameters of `BaseMessageListItemViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `MessageViewHolderFactory` as constructor parameters
- Attachment customization methods moved from `MessageViewHolderFactory` to a separate `AttachmentViewHolderFactory` class
- Removed `position` parameter from `MessageClickListener`
- Moved `BaseMessageListItemViewHolder` to a new package
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.1

# Sep 23rd, 2020 - 4.3.0-beta-1

- Update livedata/client to latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# Sep 23rd, 2020 - 4.2.11-beta-13
- Adjust ChatSocketListener to new events(NewMessageEvent, NotificationMarkReadEvent, NotificationMessageNewEvent) properties.
- Fix "load more channels"
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.6
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.7

# Sep 18th, 2020 - 4.2.11-beta-12
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

# Sep 4th, 2020 - 4.2.11-beta-11

- Fix uploading files and capturing images on Android >= 10
- Fix `AvatarView`: Render lastActiveUsers avatars when channel image is not present

# Aug 28th, 2020 - 4.2.11-beta-9

- Update event structure
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.2

## Aug 23th, 2020 - 4.2.11-beta-8
- Fix Upload Files
- Update RecyclerView Lib
- Update Notification Customization

## Aug 5th, 2020 - 4.2.11-beta-7

- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.6.9
- Fix channel name validation in CreateChannelViewModel
- Add `ChannelsView.setViewHolderFactory(factory: ChannelViewHolderFactory)` function
- Fix Fresco initialization
- Fix method to add/remove reaction

## Jul 13th, 2020 - 4.2.11-beta-6

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

## Jun 4th, 2020 - 4.2.11-beta-5

- Update livedata dependency to fix crash when NotificationMarkReadEvent received
- Add mavenLocal() repository

## Jun 4th, 2020 - 4.2.11-beta-4

- Fix crash when command (`/`) is typed.

## Jun 3rd, 2020 - 4.2.11-beta

- Fix `AvatarView` crash when the view is not attached

## May 15th, 2020 - 4.2.10-beta

- Update to the latest livedata: 0.6.1

## May 29th, 2020 - 4.2.9-beta-3

- Fix AttachmentViewHolder crash when user sends message with plain/no-media url

## May 15th, 2020 - 4.2.9-beta-2

- Update to the latest livedata: 0.6.0

## May 15th, 2020 - 4.2.8-beta-1

- Update to the latest livedata: 0.4.6

## May 15th, 2020 - 4.2.6

- Fix Avatar crash if channel/user initials are empty

## May 13th, 2020 - 4.2.5

- Create new `AvatarView`
- Glide Redirect issues resolved
- Bugfix release for livedata, updated to 0.4.2

## March 11th, 2020 - 3.6.5

- Fix reaction score parser casting exception

## March 3rd, 2020 - 3.6.5

- Fix crash on sending Google gif

## March 3rd, 2020 - 3.6.4

- Update default endpoint: from `chat-us-east-1.stream-io-api.com` to `chat-us-east-staging.stream-io-api.com`
- update target api level to 29
- Fixed media playback error on api 29 devices
- Added score field to reaction model

## January 28th, 2020 - 3.6.3

- ViewModel & ViewHolder classes now use protected instead of private variables to allow customization via subclassing
- ChannelViewHolderFactory is now easier to customize
- Added ChannelViewHolder.messageInputText for 2 way data binding
- Documentation improvements
- Fix problem with wrong scroll position

## January 10th, 2020 - 3.6.2

- Enable multiline edit text
- Fix deprecated getColumnIndexOrThrow for 29 Api Level

## January 7th, 2020 - 3.6.1

- Add navigation components with handler to override default behaviour

## Breaking changes:

###
- `OpenCameraViewListener` is replaced with CameraDestination

## January 6th, 2020 - 3.6.0

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

## December 9th, 2019 - 3.5.0

- Fix set typeFace without custom font
- Fix channel.watch (data payload was not sent)
- Fix API 23 compatiblity
- Add Attachment Border Color attrs
- Add Message Link Text Color attrs
- Add custom api endpoint config to sample app and SDK

## November 28th, 2019 - 3.4.1

- Fix Giphy buttons alignments
- Add Giphy error cases handling
- Update http related issues documentation


## November 28th, 2019 - 3.4.0

- Custom font fot the whole SDK
- Custom font per TextView
- Ignore sample app release unit tests, keep debug tests
- Added AttachmentBackgroundColorMine/Theirs
- Fix Edit/Delete thread parent message
- Replace fadein/fadeout animation of parent/current thread with default RecyclerView animation

## November 5th, 2019 - 3.3.0

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


## November 5th, 2019 - 3.2.1

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


## October 25th, 2019 - 3.2.0

- Added event interceptors to `ChannelListViewModel`

## October 24th, 2019 - 3.1.0

- Add channel to list when the user is added
- Add `onUserDisconnected` event
- Make sure channel list view model is cleared when the user disconnects
- Fix bug with `setUser` when user data is not correctly URI encoded
- Add debug/info logging
- Add Attrs for DateSeparator
## Oct 23th, 2019 - 3.0.2

- Fix NPE with restore from background and null users

## Oct 22th, 2019 - 3.0.1

- Fix NPE with empty channel lists

## Oct 21th, 2019 - 3.0.0

- Added support for message search `client.searchMessages`
- Better support for query user options
- Update channel update signature
- Fix disconnection NPE
- Minor bugfixes
- Remove file/image support
- Expose members and watchers pagination options for query channel 

#### Breaking changes

- `Channel.update` signature has changed

## Oct 16th, 2019 - 2.3.0

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

## Oct 14th, 2019 - 2.2.1

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

## Oct 9, 2019 - 2.2.0

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

## September 28, 2019 - 2.1.0

- Improved support for regenerating expired tokens

#### Breaking changes:

- `MessageInputView#progressCapturedMedia(int requestCode, int resultCode, Intent data)` renamed into `captureMedia(int requestCode, int resultCode, Intent data)`
- `binding.messageInput.permissionResult(requestCode, permissions, grantResults)` in `onRequestPermissionsResult(requestCode, permissions, grantResults) of `ChannelActivity`

## September 28, 2019 - 2.0.1

- Fix channel list ordering when a channel is added directly from Android
- Better Proguard support

## September 26, 2019 - 2.0.0

- Simplify random access to channels
- Channel query and watch methods now work the same as they do on all other SDKs

#### Breaking changes:

- `channel.query` does not watch the channel anymore, to retrieve channel state and watch use `channel.watch`
- `client.getChannelByCID` is now private, use one of the `client.channel` methods to get the same (no null checks needed)
