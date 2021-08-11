## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- Added support for several languages:
  - French
  - Hindi
  - Italian
  - Japanese
  - Korean
  - Spanish
 You can disable them by explicitly setting `resConfigs` inside `build.gradle` file
### ⚠️ Changed
- 🚨 Breaking change: Firebase dependencies have been extracted from our SDK. If you want to continue working with Firebase Push Notification you need to add `stream-chat-android-pushprovider-firebase` artifact to your App
- Updated the Kotlin version to latest supported - `1.5.21`.

### ❌ Removed


## stream-chat-android
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-client
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- `PushMessage` class created to store Push Notification data
- `PushDeviceGenerator` interface to obtain the Push Token and create the `Device`

### ⚠️ Changed
- `Device` class has an extra attribute with the `PushProvider` used on this device
- Breaking change: `ChatClient.setDevice()` and `ChatClient.addDevice()` now receive a `device` instance, instead of only receive the push provider token
- `RemoteMessage` from Firebase is not used anymore inside of our SDK, now it needs to be used with `PushMessage` class
- `NotificationConfig` has a new list of `PushDeviceGenerator` instance to be used for generating the Push Notification Token. If you were using `Firebase` as your Push Notification Provider, you need to add `FirebasePushDeviceGenerator` to your `NotificationConfig` object to continue working as before. `FirebasePushDeviceGenerator` receive by constructor the default `FirebaseMessaging` instance to be used, if you would like to use your own instance and no the default one, you can inject it by constructor. Unneeded Firebase properties have been removed from this class.

### ❌ Removed
- 🚨 Breaking change: Remove `ChatClient.isValidRemoteMessage()` method. It needs to be handled outside
- 🚨 Breaking change: Remove `ChatClient.handleRemoteMessage(RemoteMessage)`. Now it needs to be used `ChatClient.handlePushMessage(PushMessage)`


## stream-chat-android-offline
### 🐞 Fixed
- Fixed the event sync process when connection is recovered

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-ui-common
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed
- Removed unnecessary "draft" filter from the default channel list filter as it is only relevant to the sample app

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attachments of camera. Now multiple videos and pictures can be taken from the camera.
- Added the possibility to force light and dark theme. Set it in inside ChatUI to make all views, fragments and activity of the SDK light.
- Fixed applying style to `SuggestionListView` when using it as a standalone component. You can modify the style using `suggestionListViewTheme` or `TransformStyle::suggestionListStyleTransformer`

### ⬆️ Improved

### ✅ Added
- Added `MessageListView::setDeletedMessageListItemPredicate` function. It's responsible for adjusting visibility of the deleted `MessageListItem.MessageItem` elements.
- Added `streamUiAttachmentSelectionBackgroundColor` for configuring attachment's icon background in `AttachmentSelectionDialogFragment`
- Added `streamUiAttachmentSelectionAttachIcon` for configuring attach icon in `AttachmentSelectionDialogFragment`
- Added support for pinned messages:
  - Added a button to pin/unpin a message to the message options overlay
  - Added `MessageListView::setMessagePinHandler` and `MessageListView::setMessageUnpinHandler` methods to provide custom handlers for aforementioned button
  - Added `PinnedMessageListView` to display a list of pinned messages. The view is supposed to be used with `PinnedMessageListViewModel` and `PinnedMessageListViewModelFactory`
- Possibility to transform MessageItems before the are displayed in the screen.
Use the `MessageListView.setMessageItemTransformer` for make the necessary transformation. This example makes groups of messages if they were created less than one hour apart:
```
binding.messageListView.setMessageItemTransformer { list ->
  list.mapIndexed { i, messageItem ->
        var newMessageItem = messageItem

        if (i < list.lastIndex) {
            val nextMessageItem = list[i + 1]

            if (messageItem is MessageListItem.MessageItem &&
                nextMessageItem is MessageListItem.MessageItem
            ) {
                val thisInstant = messageItem.message.createdAt?.time?.let(Instant::ofEpochMilli)
                val nextInstant = nextMessageItem.message.createdAt?.time?.let(Instant::ofEpochMilli)

                if (nextInstant?.isAfter(thisInstant?.plus(1, ChronoUnit.HOURS)) == true) {
                    newMessageItem = messageItem.copy(positions = listOf(MessageListItem.Position.BOTTOM))
                } else {
                    newMessageItem =
                        messageItem.copy(positions = messageItem.positions - MessageListItem.Position.BOTTOM)
                }
            }
        }

        newMessageItem
    }
}
```


### ⚠️ Changed
- 🚨 Breaking change: the deleted `MessageListItem.MessageItem` elements are now displayed by default to all the users. This default behavior can be customized using `MessageListView::setDeletedMessageListItemPredicate` function. This function takes an instance of `MessageListItemPredicate`. You can pass one of the following objects:
    * `DeletedMessageListItemPredicate.VisibleToEveryone`
    * `DeletedMessageListItemPredicate.NotVisibleToAnyone`
    * or `DeletedMessageListItemPredicate.VisibleToAuthorOnly`
    Alternatively you can pass your custom implementation by implementing the `MessageListItemPredicate` interface if you need to customize it more deeply.

### ❌ Removed


## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where we didn't use the `Channel.getDisplayName()` logic for the `MessageListHeader`.
- Fixed a bug where lazy loading for `Channel`s wasn't working consistently 

### ⬆️ Improved
- Updated Jetpack Compose to `1.0.1`
- Updated Accompanist libraries to `0.16.1`
- Updated KTX Activity to `1.3.1`
- Exposed functionality for getting the `displayName` of `Channel`s.
- Added updated logic to Link preview attachments, which chooses either the `titleLink` or the `ogUrl` when loading the data, depending on which exists .

### ✅ Added
- Added the `emptyContent` and `loadingContent` parameters to `ChannelList` and `MessageList` components. Now you can customize the UI of those two states.
- Added lots of improvements to Avatars - added a `UserAvatar`, `ChannelAvatar` and an `InitialsAvatar` to load different types of data.
- We now show a matrix of user images in case we're in a group DM.
- We also show initials in case the user doesn't have an image.
- Added a way to customize the leading content in the `ChannelListHeader`.

### ⚠️ Changed
- `ViewModel`s now initialize automatically, so you no longer have to call `start()` on them. This is aimed to improve the consistency between our SDKs.
- Added a `Shape` parameter to `Avatar` to customize the shape.
- The `User` parameter in the `ChannelListHeader` is nullable and used to display the default leading content.

### ❌ Removed

## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- Create this new artifact. To use Firebase Push Notification you need do the following steps:
  1. Add the artifact to your `build.gradle` file -> `implementation "io.getstream:stream-chat-android-pushprovider-firebase:$streamVersion"`
  2. Add `FirebaseDeviceGenerator` to your `NotificationConfig`
        ```
            val notificationConfig = NotificationConfig(   
                [...]
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())           
                )       
        ```

### ⚠️ Changed

### ❌ Removed
