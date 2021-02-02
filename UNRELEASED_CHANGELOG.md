## Common changes for all artifacts

## stream-chat-android

## stream-chat-android-client
- Add support for typing events in threads:
    - Add `parentId` to `TypingStartEvent` and `TypingStopEvent`
    - Add `parentId` to ``ChannelClient::keystroke` and `ChannelClient::stopTyping`
- `ChatClient::sendFile` is merged into one variation with `ProgressCallback` as an optional parameter. Now the method returns `Call<String>`, the option with asyonchronous call with no return is removed. 
- `FileUploader::sendFile` and `FileUploader::sendImages` variations with `ProgressCallback` are no longer async with no return type. Now they are synchronous with `String?` as return type

## stream-chat-android-offline
- Add support for typing events in threads:
    - Add `parentId` to `Keystroke` and `StopTyping` use cases

## stream-chat-android-ui-common
- Add a new `isMessageRead` flag to the `MessageListItem.MessageItem` class, which indicates 
  that a particular message is read by other members in this channel.
- Add handling threads typing in `MessageInputViewModel`
