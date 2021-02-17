## Common changes for all artifacts

## stream-chat-android
- Add `streamMessageActionButtonsTextSize`, `streamMessageActionButtonsTextColor`, `streamMessageActionButtonsTextFont`,
 `streamMessageActionButtonsTextFontAssets`, `streamMessageActionButtonsTextStyle`, `streamMessageActionButtonsIconTint`
 attributes to `MessageListView`
- Add `ChannelHeaderViewModel::resetThread` method and make `ChannelHeaderViewModel::setActiveThread` message parameter non-nullable

## stream-chat-android-client
- Introduce ChatClient::setUserWithoutConnecting function

## stream-chat-android-offline
- Introduce `PushMessageSyncHandler` class

## stream-chat-android-ui-common
