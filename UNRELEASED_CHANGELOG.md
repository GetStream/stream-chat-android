## Common changes for all artifacts

## stream-chat-android

## stream-chat-android-client
- Add `enforceUnique` parameter to `ChatClient::sendReaction` and `ChannelClient::sendReaction` methods .
If reaction is sent with `enforceUnique` set to true, new reaction will replace all reactions the user has on this message.

## stream-chat-android-offline
- Add `enforceUnique` parameter to `SendReaction` use case. If reaction is sent with `enforceUnique` set to true,
 new reaction will replace all reactions the user has on this message.
- Fix updating `Message::ownReactions` and `Message:latestReactions` after sending or deleting reaction - add missing `userId` to `Reaction`

## stream-chat-android-ui-common
