## Common changes for all artifacts

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

## stream-chat-android-offline

## stream-chat-android-ui-common
