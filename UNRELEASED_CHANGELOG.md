## Common changes for all artifacts

## stream-chat-android
- Add filtering `shadowed` messages when computing last channel message
- Add filtering `draft` channels
- Add `DateFormatter::formatTime` method to format only time of a date

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
