## Common changes for all artifacts

## stream-chat-android
- Add `streamMessageActionButtonsTextSize`, `streamMessageActionButtonsTextColor`, `streamMessageActionButtonsTextFont`,
 `streamMessageActionButtonsTextFontAssets`, `streamMessageActionButtonsTextStyle`, `streamMessageActionButtonsIconTint`
 attributes to `MessageListView`
- Add `ChannelHeaderViewModel::resetThread` method and make `ChannelHeaderViewModel::setActiveThread` message parameter non-nullable
- Fix ReadIndicator state
- Using `MessageListView#setViewHolderFactory` is now an error - use `setMessageViewHolderFactory` instead
- Removed `MessageListItemAdapter#replaceEntities` - use `submitList` method instead

## stream-chat-android-client
- Introduce ChatClient::setUserWithoutConnecting function
- Handle disconnect event during pending token state
- Using `User#unreadCount` is now an error - use `totalUnreadCount` instead
- Using `ChannelController` is now an error - use `ChannelClient` instead
- Using `Pagination#get` is now an error - use `toString` instead
- Using the old event APIs is now an error - see the [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-ChatObserver-and-events()-APIs) for more info
- Using `ChatClient#flag` is now an error - use `flagUser` instead

## stream-chat-android-offline
- Introduce `PushMessageSyncHandler` class

- Add UseCase for querying members (`chatDomain.useCases.queryMembers(..., ...).execute()`).
    - If we're online, it executes a remote call through the ChatClient
    - If we're offline, it pulls members from the database for the given channel
- Mark the `SendMessageWithAttachmentsImpl` use case an error

## stream-chat-android-ui-common
- Fix `CaptureMediaContract` chooser on Android API 21
- Using `ChatUI(client, domain, context)` now an error - use simpler constructor instead
- Using the `Chat` interface now an error - use `ChatUI` instead
