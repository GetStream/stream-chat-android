## Common changes for all artifacts
- Updated project dependencies
    - Kotlin 1.4.30
    - Stable AndroidX releases: LiveData 2.3.0, Activity 1.2.0, Fragment 1.3.0
    - For the full list of dependency version changes, see [this PR](https://github.com/GetStream/stream-chat-android/pull/1383)

## stream-chat-android
- Fix `streamLastMessageDateUnreadTextColor` attribute not being used in ChannelListView
- Fix `streamChannelsItemSeparatorDrawable` attribute not being parsed

## stream-chat-android-client
- Fix `ConcurrentModificationException` on our `NetworkStateProvider`

## stream-chat-android-offline

## stream-chat-android-ui-common
