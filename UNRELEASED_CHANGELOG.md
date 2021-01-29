## Common changes for all artifacts

## stream-chat-android

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

## stream-chat-android-ui-common
- Add a new `isThreadMode` flag to the `MessageListItem.MessageItem` class. 
  It shows is a message item should be shown as part of thread mode in chat.
- Add possibility to set `DateSeparatorHandler` via `MessageListViewModel::setDateSeparatorHandler`
  and `MessageListViewModel::setThreadDateSeparatorHandler` which determines when to add date separator between messages
- Add `MessageListViewModel.Event.ReplyAttachment`, `MessageListViewModel.Event.DownloadAttachment`, `MessageListViewModel.Event.ShowMessage`,
  and `MessageListViewModel.Event.RemoveAttachment` classes.
- Deprecate `MessageListViewModel.Event.AttachmentDownload`