## Common changes for all artifacts

## stream-chat-android

## stream-chat-android-client
- Use proper data on `ChatClient::reconnectSocket` to reconnect normal/anonymous user
- Add `enforceUnique` parameter to `ChatClient::sendReaction` and `ChannelClient::sendReaction` methods .
If reaction is sent with `enforceUnique` set to true, new reaction will replace all reactions the user has on this message.
- Add suspending `setUserAndAwait` extension for `ChatClient`

## stream-chat-android-offline
- Add `enforceUnique` parameter to `SendReaction` use case. If reaction is sent with `enforceUnique` set to true,
 new reaction will replace all reactions the user has on this message.
- Fix updating `Message::ownReactions` and `Message:latestReactions` after sending or deleting reaction - add missing `userId` to `Reaction`

## stream-chat-android-ui-common
- Add a new `isThreadMode` flag to the `MessageListItem.MessageItem` class. 
  It shows is a message item should be shown as part of thread mode in chat.
- Add possibility to set `DateSeparatorHandler` via `MessageListViewModel::setDateSeparatorHandler`
  and `MessageListViewModel::setThreadDateSeparatorHandler` which determines when to add date separator between messages
