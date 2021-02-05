## Common changes for all artifacts

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
