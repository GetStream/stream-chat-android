## Common changes for all artifacts

## stream-chat-android
- Now depends explicitly on AndroidX Fragment (fixes a potential crash with result handling)
- Update AndroidX dependencies: Activity 1.2.0-rc01 and Fragment 1.3.0-rc01

## stream-chat-android-client
- Add filtering non image attachments in ChatClient::getImageAttachments
- Add a `channel` property to `notification.message_new` events
- Fix deleting channel error

## stream-chat-android-offline
- Add LeaveChannel use case
- Add ChannelData::memberCount
- Improve loading state querying channels
- Improve loading state querying messages

## stream-chat-android-ui-common
