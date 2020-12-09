## Common changes for all artifacts
- Updated to Kotlin 1.4.21
- For Java clients only: deprecated the `Call.enqueue(Function1)` method, please use `Call.enqueue(Callback)` instead

## stream-chat-android
- Add new attrs to `MessageListView`: `streamDeleteMessageActionEnabled`, `streamEditMessageActionEnabled`
- Improve Channel List Diff
- Add new attrs to `MessageInputView`: `streamInputScrollbarEnabled`, `streamInputScrollbarFadingEnabled`
- Add API for setting custom message date formatter in MessageListView via `setMessageDateFormatter(DateFormatter)`
    - 24 vs 12 hr controlled by user's System settings.

## stream-chat-android-client

## stream-chat-android-offline
- Add updating `channelData` after receiving `ChannelUpdatedByUserEvent`
