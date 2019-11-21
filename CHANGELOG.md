## Upcoming

- Fix Concurrent modification when removing member from channel
- Fix automention input issue
- Fix Sent message progress infinite
- Fix channel delete event handling in ChannelList view model
- Fix attachment duplicated issue when message edit
- Add File Upload 2.0
- Add editMessage function in Channel View Model
- Add MessageInputManager
- Update CustomMessageInputView
- Remove CustomMessageInputView2

## November 5th, 2019 - 3.2.1

- Fixed transparency issues with user profile images on older devices
- Better channel header title for channels without a name
- Fixed read count difference between own and other users' messages
- Fixed Video length preview
- Catch error body parsing errors
- Do not show commands list UI when all commands are disabled
- Renamed `MessageInputClient` to `MessageInputController`
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
