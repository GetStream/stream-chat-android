# UNRELEASED CHANGELOG
## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

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

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-offline
### 🐞 Fixed

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

## stream-chat-android-ui-components
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where attachments weren't properly stored when editing a message

### ⬆️ Improved
- General improvements in the Attachments API and the way we build different attachments
- Allowed for better long clicks on attachments

### ✅ Added

### ⚠️ Changed
- Removed AttachmentPicker option when editing messages
- Removed Attachment previews when editing messages with attachments
- Improved the ease of use of the AttachmentState API by keeping it state & actions only
- Moved the `modifier` parameter outside of the AttachmentState to the AttachmentFactory
- Updated Attachments to hold `Message` items instead of `MessageItem`s

### ❌ Removed

## stream-chat-android-pushprovider-firebase
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

## stream-chat-android-pushprovider-huawei
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed

### ❌ Removed

# September 15th, 2021 - 4.19.0
## Common changes for all artifacts
### ✅ Added
- Create new artifact to integrate Huawei Push Kit with Stream. You will need to add  `stream-chat-android-pushprovider-huawei` artifact to your App. Check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei) for further details.

## stream-chat-android
### ✅ Added
- Added a method to dismiss all notifications from a channel. It is handled internally from the SDK but you are able to dismiss channel notification at whatever time calling `ChatClient::dismissChannelNotifications`
- Notifications are dismissed after the user logout the SDK

## stream-chat-android-client
### 🐞 Fixed
- Fixed sending messages using `ChatClient::sendMessage` without explicitly specifying the sender user id.
- Fixed sending custom attachments without files to upload
- Fixed deserialization issues when parsing `ChannelTruncatedEvent` and `MessageDeletedEvent` events with an absent user.

### ⬆️ Improved
- Custom attachment types are now preserved after file uploads

### ✅ Added
- Added `hardDelete` field to `MessageDeletedEvent`.

### ⚠️ Changed
- Now it is possible to hard delete messages. Insert a flag `hard = true` in the `ChatClient.deleteMessage` and it will be deleted in the backend. **This action can't be undone!**

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed bug with light mode.
- Removed `streamUiValidTheme`, as we don't support extending our base theme any longer. Please don't extend our base theme and set the `streamUiTheme` in your applcation theme instead.

## stream-chat-android-ui-components
### ✅ Added
- Notifications are dismissed after the user go into the channel conversation when you are using `MessageListView`
- Added `bubbleBorderColorMine`, `bubbleBorderColorTheirs`, `bubbleBorderWidthMine`, `bubbleBorderWidthTheirs` to `ViewReactionsViewStyle` for customizing reactions` border

## stream-chat-android-compose
### ⬆️ Improved
- Updated the Compose framework version (1.0.2)
- Updated the Accompanist library version (0.18.0)

### ✅ Added
- Added an uploading indicator to files and images
- Images being uploaded are now preloaded from the system
- Upload indicators show the upload progress and how much data is left to send
- Added more image options to the ImagePreviewActivity such as download, delete, reply to message...
- Added an Image Gallery feature to the ImagePreviewActivity where users can browse all the images
- Notifications are dismissed after the user go into the channel conversation when you are using `MessageList`

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
- Changed the `AttachmentState` `message` property name to `messageItem`
- Added an `isFocused` property to `MessageItem`
- Added an `onImagePreviewResult` callback/parameter to various Messages screen components

### ❌ Removed

## stream-chat-android-pushprovider-firebase
### ✅ Added
- Added a `FirebaseMessagingDelegate` class to simplify custom implementations of `FirebaseMessagingService` that forward messages to the SDK. See [Using a Custom Firebase Messaging Service](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service) for more details.

## stream-chat-android-pushprovider-huawei
### ✅ Added
- Added a `HuaweiMessagingDelegate` class to simplify custom implementations of `HmsMessageService` that forward messages to the SDK. See [Using a Custom Huawei Messaging Service](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei#using-a-custom-huawei-messaging-service) for more details.

# September 15th, 2021 - 4.18.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed setting notification's `contentTitle` when a Channel doesn't have the name. It will now show members names instead

### ✅ Added
- Added a new way to paginate through search message results using limit and next/previous values.

### ⚠️ Changed
- Deprecated `Channel#name`, `Channel#image`, `User#name`, `Ues#image` extension properties. Use class members instead.

### ❌ Removed
- Completely removed the old serialization implementation. You can no longer opt-out of using the new serialization implementation.
- Removed the `UpdateUsersRequest` class.

## stream-chat-android-offline
### ⬆️ Improved
- Improving logs for Message deletion error.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed theme for `AttachmentDocumentActivity`. Now it is applied: `Theme.AppCompat.DayNight.NoActionBar`

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the bug when MessageInputView let send a message with large attachments. Such message is never sent.
- Fixed bug related to `ScrollHelper` when `MessageListView` is initialised more than once.

### ⬆️ Improved
- The search for mentions now includes transliteration, diacritics removal, and ignore typos. To use transliteration, pass the id of the desired alphabets to `DefaultStreamTransliterator`, add it to DefaultUserLookupHandler and set it using `MessageInputView.setUserLookupHandler`. Transliteration works only for android API 29. If you like to add your own transliteration use https://unicode-org.github.io/icu/userguide/icu4j/.
- Improved scroll of message when many gif images are present in `MessageListView`

### ✅ Added
- Added scroll behaviour to `MessageListViewStyle`.

## stream-chat-android-compose
### 🐞 Fixed
- Fixed a bug where the Message list flickered when sending new messages
- Fixed a few bugs where some attachments had upload state and weren't file/image uploads

### ⬆️ Improved
- Improved the Message list scrolling behavior and scroll to bottom actions
- Added an unread count on the Message list's scroll to bottom CTA
- Improved the way we build items in the Message list
- Added line limit to link attachment descriptions
- Added a way to customize the default line limit for link descriptions
- Improved the `MessageListHeader` with more customization options

### ✅ Added
- Added an uploading indicator to files and images
- Images being uploaded are now preloaded from the system
- Upload indicators show the upload progress and how much data is left to send
- Added UploadAttachmentFactory that handles attachment uploads

### ⚠️ Changed
- `StreamAttachment.defaultFactories()` is a function now, instead of a property.
- Updated all default value factories to functions (e.g. StreamTypography)
- Re-organized all attachment factories and split up code in multiple packages
- Changed the `AttachmentState` `message` property name to `messageItem`
- Added a `Channel` parameter to the `MessagesScreen`'s `onHeaderActionClick` lambda
- Changed the way the `MessageListHeader` is structured by adding slot components

# August 30th, 2021 - 4.17.2
## stream-chat-android-ui-client
### 🐞 Fixed
- Fixed bug which can lead to crash when immediate logout after login

# August 30th, 2021 - 4.17.2
## stream-chat-android-ui-components
### 🐞 Fixed
- Fixes a bug related to incorrect theme of AttachmentActivity.

# August 30th, 2021 - 4.17.1
## Common changes for all artifacts
### ⬆️ Improved
- Now we provide SNAPSHOT versions of our SDK for every commit arrives to the `develop` branch.
  They shouldn't be used for a production release because they could contains some known bugs or breaking changes that will be fixed before a normal version is released, but you can use them to fetch last changes from our SDK
  To use them you need add a new maven repository to your `build.gradle` file and use the SNAPSHOT.
```
 maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
```
Giving that our last SDK version is `X.Y.Z`, the SNAPSHOT version would be `X.Y.(Z+1)-SNAPSHOT`

## stream-chat-android-client
### 🐞 Fixed
- `TooManyRequestsException` caused to be subscribed multiple times to the `ConnectivityManager`

### ⬆️ Improved
- Reconnection process

## stream-chat-android-offline
### ✅ Added
- Added `ChatDomain#Builder#uploadAttachmentsWorkerNetworkType` for customizing `UploadAttachmentsWorker` network type constraint

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed a bug in state handling for anonymous users.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fix for position of deleted messages for other users
- Fix glitch in selectors of file

### ✅ Added
- Added style attributes for `AttachmentGalleryActivity` to control menu options like enabling/disabling reply button etc.
- Now it is possible to customize when the avatar appears in the conversation. It is possible to use an avatar in messages from other users and for messages of the current user. You can check it here:  https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#configure-when-avatar-appears
- Added support for slow mode. Users are no longer able to send messages during cooldown interval.
- Added possibility to customize the appearance of cooldown timer in the `MessageInputView` using the following attributes:
  - `streamUiCooldownTimerTextSize`, `streamUiCooldownTimerTextColor`, `streamUiCooldownTimerFontAssets`, `streamUiCooldownTimerFont`, `streamUiCooldownTimerTextStyle` attributes to customize cooldown timer text
  - `cooldownTimerBackgroundDrawable`- the background drawable for cooldown timer

# August 24th, 2021 - 4.17.0
## Common changes for all artifacts
### ⬆️ Improved
- Updated Target API Level to 30
- Updated dependency versions
  - Coil 1.3.2
  - AndroidX Activity 1.3.1
  - AndroidX Startup 1.1.0
  - AndroidX ConstraintLayout 2.1.0
  - Lottie 4.0.0

## stream-chat-android-client
### 🐞 Fixed
- Fixed a serialization error when editing messages that are replies

### ✅ Added
- Added the `expiration` parameter to `ChatClient::muteChannel`, `ChannelClient:mute` methods
- Added the `timeout` parameter to `ChatClient::muteUser`, `ChannelClient:mute::muteUser` methods

### ⚠️ Changed
- Allow specifying multiple attachment's type when getting messages with attachments:
  - Deprecated `ChatClient::getMessagesWithAttachments` with `type` parameter. Use `ChatClient::getMessagesWithAttachments` function with types list instead
  - Deprecated `ChannelClient::getMessagesWithAttachments` with `type` parameter. Use `ChannelClient::getMessagesWithAttachments` function with types list instead

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed a bug in state handling for anonymous users.

## stream-chat-android-ui-components
### ✅ Added
- Added self-contained higher-level UI components:
  - `ChannelListFragment` - channel list screen which internally contains `ChannelListHeaderView`, `ChannelListView`, `SearchInputView`, `SearchResultListView`.
  - `ChannelListActivity` - thin wrapper around `ChannelListFragment`
  - `MessageListFragment` - message list screen which internally contains `MessageListHeaderView`, `MessageListView`, `MessageInputView`.
  - `MessageListActivity` - thin wrapper around `MessageListFragment`
    Check [ChannelListScreen](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list-screen/) and [MessageListScreen](https://getstream.io/chat/docs/sdk/android/ui/components/message-list-screen/) docs for further details.

## stream-chat-android-compose
### 🐞 Fixed
- Added missing `emptyContent` and `loadingContent` parameters to `MessageList` inner components.
- Fixed a bug where selected File attachment icons were clipped.
- Fixed a bug where image file attachments weren't shown as thumbnails.
- Added an overlay to the `ChannelInfo` that blocks outside clicks.
- Updated the `ChannelInfoUserItem` to use the `UserAvatar`.

### ⬆️ Improved
- Added default date and time formatting to Channel and Message items.
- Improved attachments API by providing cleaner examples of attachment factories.
- Updated documentation & examples.
- Decoupled attachment content to specific attachment files.
- Decoupled message attachment content to a `MessageAttachmentsContent` component.
- Re-structured SDK module to accommodate a new `attachment` package.

### ✅ Added
- Added `DateFormatter` option to the `ChatTheme`, to allow for date format customization across the app.
- Added a `Timestamp` component that encapsulates date formatting.
- Added a way to customize and override if messages use unique reactions.
- Added a `GiphyAttachmentFactory` for GIF specific attachments.
- Added support for loading GIFs using a custom `ImageLoader` for Coil.


# August 12th, 2021 - 4.16.0
## Common changes for all artifacts
### ✅ Added
- Added support for several languages:
  - French
  - Hindi
  - Italian
  - Japanese
  - Korean
  - Spanish
    You can disable them by explicitly setting `resConfigs` inside `build.gradle` file. Check our [docs](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-translations/) for further details.
### ⚠️ Changed
- 🚨 Breaking change: Firebase dependencies have been extracted from our SDK. If you want to continue working with Firebase Push Notification you need to add `stream-chat-android-pushprovider-firebase` artifact to your App
  Check our [docs](https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/) for further details.
- Updated the Kotlin version to latest supported - `1.5.21`.

## stream-chat-android
### 🐞 Fixed
- Fixed markdown links rendering using custom linkify implementation.

## stream-chat-android-client
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

## stream-chat-android-ui-common
### ❌ Removed
- Removed unnecessary "draft" filter from the default channel list filter as it is only relevant to the sample app

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attachments of camera. Now multiple videos and pictures can be taken from the camera.
- Added the possibility to force light and dark theme. Set it in inside ChatUI to make all views, fragments and activity of the SDK light.
- Fixed applying style to `SuggestionListView` when using it as a standalone component. You can modify the style using `suggestionListViewTheme` or `TransformStyle::suggestionListStyleTransformer`
- Fixed markdown links rendering using custom linkify implementation.

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
- Added possibility to customize the appearance of pinned message in the`MessageListView` using the following attributes:
  - `streamUiPinMessageEnabled` - attribute to enable/disable "pin message" feature
  - `streamUiPinOptionIcon` - icon for pin message option
  - `streamUiUnpinOptionIcon` - icon for unpin message option
  - `streamUiPinnedMessageIndicatorTextSize`, `streamUiPinnedMessageIndicatorTextColor`, `streamUiPinnedMessageIndicatorTextFontAssets`, `streamUiPinnedMessageIndicatorTextFont`, `streamUiPinnedMessageIndicatorTextStyle` attributes to customize "pinned by" text
  - `streamUiPinnedMessageIndicatorIcon` - icon in the message list indicating that a message was pinned
  - `streamUiPinnedMessageBackgroundColor` - the background color of a pinned message in the message list
- Added possibility to customize `PinnedMessageListView` style using `streamUiPinnedMessageListStyle` theme attribute or `TransformStyle.pinnedMessageListViewStyleTransformer`. The list of available style attributes can be found in `attrs_pinned_message_list_view.xml`. The default style for `PinnedMessageListView` is `StreamUi.PinnedMessageList`.

### ⚠️ Changed
- 🚨 Breaking change: the deleted `MessageListItem.MessageItem` elements are now displayed by default to all the users. This default behavior can be customized using `MessageListView::setDeletedMessageListItemPredicate` function. This function takes an instance of `MessageListItemPredicate`. You can pass one of the following objects:
  * `DeletedMessageListItemPredicate.VisibleToEveryone`
  * `DeletedMessageListItemPredicate.NotVisibleToAnyone`
  * or `DeletedMessageListItemPredicate.VisibleToAuthorOnly`
    Alternatively you can pass your custom implementation by implementing the `MessageListItemPredicate` interface if you need to customize it more deeply.

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

## stream-chat-android-pushprovider-firebase
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


# August 5th, 2021 - 4.15.1
## stream-chat-android-client
### ⬆️ Improved
- Improved `ChatClient::pinMessage` and `ChatClient::unpinMessage`. Now the methods use partial message updates and the data in other `Message` fields is not lost.

### ✅ Added
- Added `Channel::isMutedFor` extension function which might be used to check if the Channel is muted for User
- Added `ChatClient::partialUpdateMessage` method to update specific `Message` fields retaining the other fields

## stream-chat-android-offline
### 🐞 Fixed
- Fixed updating `ChannelController::muted` value

### ⬆️ Improved
- The following `Message` fields are now persisted to the database: `pinned`, `pinnedAt`, `pinExpires`, `pinnedBy`, `channelInfo`, `replyMessageId`.

## stream-chat-android-ui-components
### 🐞 Fixed
- Added a fix for default view for empty state of ChannelListView.
- Fixed memory leaks for FileAttachmentsView.

### ✅ Added
- Added `MessageListItem.ThreadPlaceholderItem` and corresponding `THREAD_PLACEHOLDER` view type which can be used to implement an empty thread placeholder.
- Added `authorLink` to `Attachment` - the link to the website

### ❌ Removed
- Removed `UrlSigner` class

## stream-chat-android-compose
### ⬆️ Improved
- Exposed `DefaultMessageContainer` as a public component so users can use it as a fallback
- Exposed an `isMine` property on `MessageItem`s, for ease of use.
- Allowed for customization of `MessageList` (specifically `Messages`) component background, through a `modifier.background()` parameter.
- Allowed for better message customization before sending the message.

### ⚠️ Changed
- Moved permissions and queries from the compose sample app `AndroidManifest.xml` to the SDK `AndroidManifest.xml` so users don't have to add permissions themselves.
- Changed the exposed type of the `MessageComposer`'s `onSendMessage` handler. This way people can customize messages before we send them to the API.

### ❌ Removed
- Removed `currentUser` parameter from `DefaultMessageContainer` and some other components that relied on ID comparison to know which message is ours/theirs.
- Removed default background color on `Messages` component, so that users can customize it by passing in a `modifier`.


# July 29th, 2021 - 4.15.0
## New Jetpack Compose UI Components 🎉

Starting from this release, we have a new `stream-chat-android-compose` artifact that contains a UI implementation for Chat built in Jetpack Compose.

The new artifact is available as a beta for now (note the postfix in the version number):

```groovy
implementation "io.getstream:stream-chat-android-compose:4.15.0-beta"
```

Learn more in the [announcement blog post](https://getstream.io/blog/jetpack-compose-sdk/), check out the [documentation of the Compose UI Components](https://getstream.io/chat/docs/sdk/android/compose/overview/), and try them today with the [Compose Chat tutorial](https://getstream.io/chat/compose/tutorial/)!

## Common changes for all artifacts

### 🐞 Fixed
- Fixed adding `MessageListItem.TypingItem` to message list

### ⬆️ Improved
- ⚠ Downgraded Kotlin version to 1.5.10 to support Jetpack Compose
- Removed AndroidX Media dependency
- Updated dependency versions
  - Coil 1.3.0
  - AndroidX Activity 1.3.0
  - AndroidX AppCompat 1.3.1
  - Android Ktx 1.6.0
  - AndroidX RecyclerView 1.2.1
  - Kotlin Coroutines 1.5.1
  - Dexter 6.2.3
  - Lottie 3.7.2

## stream-chat-android-client
### ⬆️ Improved
- Improved the names of properties in the `Config` class

## stream-chat-android-ui-common
### ✅ Added
Now it is possible to style the AttachmentActivity. Just replace the activity's theme
in your Manifest file:

```
<activity
    android:name="io.getstream.chat.android.ui.gallery.AttachmentActivity"
    android:theme="@style/yourTheme"
    tools:replace="android:theme"
    />
```

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed "operator $ne is not supported for custom fields" error when querying channels

### ✅ Added
- Now you can configure the style of `MessageListItem`. Added:
  - streamUiMessageTextColorThreadSeparator
  - streamUiMessageTextFontThreadSeparator
  - streamUiMessageTextFontAssetsThreadSeparator
  - streamUiMessageTextStyleThreadSeparator
  - streamUiMessageTextSizeLinkLabel
  - streamUiMessageTextColorLinkLabel
  - streamUiMessageTextFontLinkLabel
  - streamUiMessageTextFontAssetsLinkLabel
  - streamUiMessageTextStyleLinkLabel
  - streamUiMessageListLoadingView
  - streamUiEmptyStateTextSize
  - streamUiEmptyStateTextColor
  - streamUiEmptyStateTextFont
  - streamUiEmptyStateTextFontAssets
  - streamUiEmptyStateTextStyle

- Now you can configure the style of `AttachmentMediaActivity`
- Added `streamUiLoadingView`, `streamUiEmptyStateView` and `streamUiLoadingMoreView` attributes to `ChannelListView` and `ChannelListViewStyle`
- Added possibility to customize `ChannelListView` using `streamUiChannelListViewStyle`. Check `StreamUi.ChannelListView` style
- Added `edgeEffectColor` attribute to `ChannelListView` and `ChannelListViewStyle` to allow configuring edge effect color
- Added possibility to customize `MentionListView` style via `TransformStyle.mentionListViewStyleTransformer`
- Added `streamUiSearchResultListViewStyle` attribute to application to customize `SearchResultListView`. The attribute references a style with the following attributes:
  - `streamUiSearchResultListSearchInfoBarBackground` - background for search info bar
  - `streamUiSearchResultListSearchInfoBarTextSize`, `streamUiSearchResultListSearchInfoBarTextColor`, `streamUiSearchResultListSearchInfoBarTextFont`, `streamUiSearchResultListSearchInfoBarTextFontAssets`, `streamUiSearchResultListSearchInfoBarTextStyle` attributes to customize text displayed in search info bar
  - `streamUiSearchResultListEmptyStateIcon` - icon for empty state view
  - `streamUiSearchResultListEmptyStateTextSize`, `streamUiSearchResultListEmptyStateTextColor`, `streamUiSearchResultListEmptyStateTextFont`, `streamUiSearchResultListEmptyStateTextFontAssets`, `streamUiSearchResultListEmptyStateTextStyle` attributes to customize empty state text
  - `streamUiSearchResultListProgressBarIcon` - animated progress drawable
  - `streamUiSearchResultListSenderNameTextSize`, `streamUiSearchResultListSenderNameTextColor`, `streamUiSearchResultListSenderNameTextFont`, `streamUiSearchResultListSenderNameTextFontAssets`, `streamUiSearchResultListSenderNameTextStyle` attributes to customize message sender text
  - `streamUiSearchResultListMessageTextSize`, `streamUiSearchResultListMessageTextColor`, `streamUiSearchResultListMessageTextFont`, `streamUiSearchResultListMessageTextFontAssets`, `streamUiSearchResultListMessageTextStyle` attributes to customize message text
  - `streamUiSearchResultListMessageTimeTextSize`, `streamUiSearchResultListMessageTimeTextColor`, `streamUiSearchResultListMessageTimeTextFont`, `streamUiSearchResultListMessageTimeTextFontAssets`, `streamUiSearchResultListMessageTimeTextStyle` attributes to customize message time text
- Added possibility to customize `SearchResultListView` style via `TransformStyle.searchResultListViewStyleTransformer`
- Added `streamUiTypingIndicatorViewStyle` attribute to application to customize `TypingIndicatorView`. The attribute references a style with the following attributes:
  - `streamUiTypingIndicatorAnimationView` - typing view
  - `streamUiTypingIndicatorUsersTextSize`, `streamUiTypingIndicatorUsersTextColor`, `streamUiTypingIndicatorUsersTextFont`, `streamUiTypingIndicatorUsersTextFontAssets`, `streamUiTypingIndicatorUsersTextStyle` attributes to customize typing users text
- Added possibility to customize `TypingIndicatorView` style via `TransformStyle.typingIndicatorViewStyleTransformer`
- Added new properties allowing customizing `MessageInputView` using `MessageInputViewStyle` and `AttachmentSelectionDialogStyle`:
  - `MessageInputViewStyle.fileNameTextStyle`
  - `MessageInputViewStyle.fileSizeTextStyle`
  - `MessageInputViewStyle.fileCheckboxSelectorDrawable`
  - `MessageInputViewStyle.fileCheckboxTextColor`
  - `MessageInputViewStyle.fileAttachmentEmptyStateTextStyle`
  - `MessageInputViewStyle.mediaAttachmentEmptyStateTextStyle`
  - `MessageInputViewStyle.fileAttachmentEmptyStateText`
  - `MessageInputViewStyle.mediaAttachmentEmptyStateText`
  - `MessageInputViewStyle.dismissIconDrawable`
  - `AttachmentSelectionDialogStyle.allowAccessToGalleryText`
  - `AttachmentSelectionDialogStyle.allowAccessToFilesText`
  - `AttachmentSelectionDialogStyle.allowAccessToCameraText`
  - `AttachmentSelectionDialogStyle.allowAccessToGalleryIcon`
  - `AttachmentSelectionDialogStyle.allowAccessToFilesIcon`
  - `AttachmentSelectionDialogStyle.allowAccessToCameraIcon`
  - `AttachmentSelectionDialogStyle.grantPermissionsTextStyle`
  - `AttachmentSelectionDialogStyle.recentFilesTextStyle`
  - `AttachmentSelectionDialogStyle.recentFilesText`
  - `AttachmentSelectionDialogStyle.fileManagerIcon`
  - `AttachmentSelectionDialogStyle.videoDurationTextStyle`
  - `AttachmentSelectionDialogStyle.videoIconDrawable`
  - `AttachmentSelectionDialogStyle.videoIconVisible`
  - `AttachmentSelectionDialogStyle.videoLengthLabelVisible`
- Added `StreamUi.MessageInputView` theme allowing to customize all of the `MessageInputViewStyle` properties:
  - streamUiAttachButtonEnabled
  - streamUiAttachButtonIcon
  - streamUiLightningButtonEnabled
  - streamUiLightningButtonIcon
  - streamUiMessageInputTextSize
  - streamUiMessageInputTextColor
  - streamUiMessageInputHintTextColor
  - streamUiMessageInputScrollbarEnabled
  - streamUiMessageInputScrollbarFadingEnabled
  - streamUiSendButtonEnabled
  - streamUiSendButtonEnabledIcon
  - streamUiSendButtonDisabledIcon
  - streamUiShowSendAlsoToChannelCheckbox
  - streamUiSendAlsoToChannelCheckboxGroupChatText
  - streamUiSendAlsoToChannelCheckboxDirectChatText
  - streamUiSendAlsoToChannelCheckboxTextSize
  - streamUiSendAlsoToChannelCheckboxTextColor
  - streamUiSendAlsoToChannelCheckboxTextStyle
  - streamUiMentionsEnabled
  - streamUiMessageInputTextStyle
  - streamUiMessageInputHintText
  - streamUiCommandsEnabled
  - streamUiMessageInputEditTextBackgroundDrawable
  - streamUiMessageInputDividerBackgroundDrawable
  - streamUiPictureAttachmentIcon
  - streamUiFileAttachmentIcon
  - streamUiCameraAttachmentIcon
  - streamUiAllowAccessToCameraIcon
  - streamUiAllowAccessToFilesIcon
  - streamUiAllowAccessToGalleryIcon
  - streamUiAllowAccessToGalleryText
  - streamUiAllowAccessToFilesText
  - streamUiAllowAccessToCameraText
  - streamUiGrantPermissionsTextSize
  - streamUiGrantPermissionsTextColor
  - streamUiGrantPermissionsTextStyle
  - streamUiAttachmentsRecentFilesTextSize
  - streamUiAttachmentsRecentFilesTextColor
  - streamUiAttachmentsRecentFilesTextStyle
  - streamUiAttachmentsRecentFilesText
  - streamUiAttachmentsFileManagerIcon
  - streamUiAttachmentVideoLogoIcon
  - streamUiAttachmentVideoLengthVisible
  - streamUiAttachmentVideoIconVisible
  - streamUiCommandInputCancelIcon
  - streamUiCommandInputBadgeBackgroundDrawable
  - streamUiCommandInputBadgeIcon
  - streamUiCommandInputBadgeTextSize
  - streamUiCommandInputBadgeTextColor
  - streamUiCommandInputBadgeStyle
  - streamUiAttachmentsFileNameTextSize
  - streamUiAttachmentsFileNameTextColor
  - streamUiAttachmentsFileNameTextStyle
  - streamUiAttachmentsFileSizeTextSize
  - streamUiAttachmentsFileSizeTextColor
  - streamUiAttachmentsFileSizeTextStyle
  - streamUiFileCheckBoxSelectorTextColor
  - streamUiFileCheckBoxSelectorDrawable
  - streamUiAttachmentsFilesEmptyStateTextSize
  - streamUiAttachmentsFilesEmptyStateTextColor
  - streamUiAttachmentsFilesEmptyStateStyle
  - streamUiAttachmentsMediaEmptyStateTextSize
  - streamUiAttachmentsMediaEmptyStateTextColor
  - streamUiAttachmentsMediaEmptyStateStyle
  - streamUiAttachmentsFilesEmptyStateText
  - streamUiAttachmentsMediaEmptyStateText
  - streamUiMessageInputCloseButtonIconDrawable
- Added `streamUiMessageListFileAttachmentStyle` theme attribute to customize the appearance of file attachments within messages.

### ⚠️ Changed
- Made `Channel::getLastMessage` function public
- `AttachmentSelectionDialogFragment::newInstance` requires instance of `MessageInputViewStyle` as a parameter. You can obtain a default implementation of `MessageInputViewStyle` with `MessageInputViewStyle::createDefault` method.
- Renamed `FileAttachmentsViewStyle` class to `FileAttachmentViewStyle`

### ❌ Removed
- 🚨 Breaking change: `MessageListItemStyle::reactionsEnabled` was deleted as doubling of the same flag from `MessageListViewStyle`


# July 19th, 2021 - 4.14.2
## stream-chat-android-client
### ❌ Removed
- Removed `Channel::isMuted` extension. Use `User::channelMutes` or subscribe for `NotificationChannelMutesUpdatedEvent` to get information about muted channels.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed crash caused by missing `streamUiReplyAvatarStyle` and `streamUiMessageOptionsAvatarStyle`

### ⬆️ Improved
- "Copy Message" option is now hidden when the message contains no text to copy.

### ✅ Added
- Now you can configure the style of `AttachmentMediaActivity`.

# July 14th, 2021 - 4.14.1
## stream-chat-android-ui-components
### ✅ Added
- Added `MessageListView::requireStyle` which expose `MessageListViewStyle`. Be sure to invoke it when view is initialized already.

# July 13th, 2021 - 4.14.0
## Common changes for all artifacts
### 🐞 Fixed
- Fix scroll bug in the `MessageListView` that produces an exception related to index out of bounds.

## stream-chat-android-client
### ⬆️ Improved
- Improved `ChatClient::enableSlowMode`, `ChatClient::disableSlowMode`, `ChannelClient::enableSlowMode`, `ChannelClient::disableSlowMode` methods. Now the methods do partial channel updates so that other channel fields are not affected.

### ✅ Added
- Added `ChatClient::partialUpdateUser` method for user partial updates.

## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug related to editing message in offline mode. The bug was causing message to reset to the previous one after connection was recovered.
- Fixed violation of comparison contract for nullable fields in `QuerySort::comparator`

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed the alignment of the titles in `MessageListHeaderView` when the avatar is hidden.

### ✅ Added
- Added `streamUiMessagesStart` that allows to controll if the stack of messages starts at the bottom or the top.
- Added `streamUiThreadMessagesStart` that allows to controll if the stack of thread messages starts at the bottom or the top.
- Added `streamUiSuggestionListViewStyle` that allows to customize `SuggestionListView` with a theme
- Added `streamUiChannelListHeaderStyle` that allows to customize ChannelListHeaderView.
- `MentionListView` can be customisable with XML parameters and with a theme.
- Added possibility to customize all avatar using themes. Create
  ```
  <style name="StreamTheme" parent="@style/StreamUiTheme">
  ```
  and customize all the avatars that you would like. All options are available here:
  https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs.xml
- Now you can use the style `streamUiChannelListHeaderStyle` to customize ChannelListHeaderView.

### ⚠️ Changed
- 🚨 Breaking change: removed `MessageListItemStyle.threadsEnabled` property. You should use only the `MessageListViewStyle.threadsEnabled` instead. E.g. The following code will disable both _Thread reply_ message option and _Thread reply_ footnote view visible below the message list item:
```kotlin
        TransformStyle.messageListStyleTransformer = StyleTransformer {
  it.copy(threadsEnabled = false)
}
```

# July 1st, 2021 - 4.13.0
## Common changes for all artifacts
### ⬆️ Improved
- Updated to Kotlin 1.5.20

## stream-chat-android
### ✅ Added
- Added `ChatUi.Builder#withImageHeadersProvider` to allow adding custom headers to image requests

## stream-chat-android-client
### ⚠️ Changed
- Using the `useNewSerialization` option on the `ChatClient.Builder` to opt out from using the new serialization implementation is now an error. Please start using the new serialization implementation, or report any issues keeping you from doing so. The old implementation will be removed soon.

## stream-chat-android-offline
### 🐞 Fixed
- By default we use backend request to define is new message event related to our query channels specs or not. Now filtering by BE only fields works for channels

## stream-chat-android-ui-components
### ✅ Added
- Added new attributes to `MessageInputView` allowing to customize the style of input field during command input:
  - `streamUiCommandInputBadgeTextSize`, `streamUiCommandInputBadgeTextColor`, `streamUiCommandInputBadgeFontAssets`, `streamUiCommandInputBadgeFont`, `streamUiCommandInputBadgeStyle` attributes to customize the text appearance of command name inside command badge
  - `streamUiCommandInputCancelIcon` attribute to customize the icon for cancel button
  - `streamUiCommandInputBadgeIcon` attribute to customize the icon inside command badge
  - `streamUiCommandInputBadgeBackgroundDrawable` attribute to customize the background shape of command badge
- Added possibility to customize `MessageListHeaderView` style via `streamUiMessageListHeaderStyle` theme attribute and via `TransformStyle.messageListHeaderStyleTransformer`.
- Added new attributes to `MessageInputView`:
  - `streamUiCommandIcon` attribute to customize the command icon displayed for each command item in the suggestion list popup
  - `streamUiLightningIcon` attribute to customize the lightning icon displayed in the top left corner of the suggestion list popup
- Added support for customizing `SearchInputView`
  - Added `SearchInputViewStyle` class allowing customization using `TransformStyle` API
  - Added XML attrs for `SearchInputView`:
    - `streamUiSearchInputViewHintText`
    - `streamUiSearchInputViewSearchIcon`
    - `streamUiSearchInputViewClearInputIcon`
    - `streamUiSearchInputViewBackground`
    - `streamUiSearchInputViewTextColor`
    - `streamUiSearchInputViewHintColor`
    - `streamUiSearchInputViewTextSize`
- Added `ChatUi#imageHeadersProvider` to allow adding custom headers to image requests

### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.
- Made `SuggestionListController` and `SuggestionListUi` public. Note that both of these are _experimental_, which means that the API might change at any time in the future (even without a deprecation cycle).
- Made `AttachmentSelectionDialogFragment` _experimental_ which means that the API might change at any time in the future (even without a deprecation cycle).


# June 23th, 2021 - 4.12.1
## stream-chat-android-client
### ✅ Added
- Added `ChannelClient::sendEvent` method which allows to send custom events.
- Added nullable `User` field to `UnknownEvent`.

### ❌ Removed
- Removed the `Message::attachmentsSyncStatus` field


## stream-chat-android-offline
### 🐞 Fixed
- Fixed `in` and `nin` filters when filtering by extra data field that is an array.
- Fixed crash when adding a reaction to a thread message.

### ⬆️ Improved
- Now attachments can be sent while being in offline


## stream-chat-android-ui-common
### ✅ Added
- Made `AttachmentSelectionDialogFragment` public. Use `newInstance` to create instances of this Fragment.


## stream-chat-android-ui-components
### ⬆️ Improved
- Hide suggestion list popup when keyboard is hidden.

### ✅ Added
- Added the `MessageInputView::hideSuggestionList` method to hide the suggestion list popup.


# June 15th, 2021 - 4.12.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed thrown exception type while checking if `ChatClient` is initialized

## stream-chat-android-offline
### 🐞 Fixed
- Fixed bug where reactions of other users were sometimes displayed as reactions of the current user.
- Fixed bug where deleted user reactions were sometimes displayed on the message options overlay.

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed bug where files without extension in their name lost the mime type.
- Using offline.ChatDomain instead of livedata.ChatDomain in ChannelListViewModel.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixing the save of pictures from AttachmentGalleryActivity. When external storage
  permission is not granted, now it asks for it.
### ⬆️ Improved
- Added default implementation of "Leave channel" click listener to `ChannelListViewModelBinding`

### ✅ Added
- Added `streamUiChannelActionsDialogStyle` attribute to application theme and `ChannelListView` to customize channel actions dialog appearance. The attribute references a style with the following attributes:
  - `streamUiChannelActionsMemberNamesTextSize`, `streamUiChannelActionsMemberNamesTextColor`, `streamUiChannelActionsMemberNamesTextFont`, `streamUiChannelActionsMemberNamesTextFontAssets`, `streamUiChannelActionsMemberNamesTextStyle` attributes to customize dialog title with member names
  - `streamUiChannelActionsMemberInfoTextSize`, `streamUiChannelActionsMemberInfoTextColor`, `streamUiChannelActionsMemberInfoTextFont`, `streamUiChannelActionsMemberInfoTextFontAssets`, `streamUiChannelActionsMemberInfoTextStyle` attributes to customize dialog subtitle with member info
  - `streamUiChannelActionsItemTextSize`, `streamUiChannelActionsItemTextColor`, `streamUiChannelActionsItemTextFont`, `streamUiChannelActionsItemTextFontAssets`, `streamUiChannelActionsItemTextStyle` attributes to customize action item text style
  - `streamUiChannelActionsWarningItemTextSize`, `streamUiChannelActionsWarningItemTextColor`, `streamUiChannelActionsWarningItemTextFont`, `streamUiChannelActionsWarningItemTextFontAssets`, `streamUiChannelActionsWarningItemTextStyle` attributes to customize warning action item text style
  - `streamUiChannelActionsViewInfoIcon` attribute to customize "View Info" action icon
  - `streamUiChannelActionsViewInfoEnabled` attribute to hide/show "View Info" action item
  - `streamUiChannelActionsLeaveGroupIcon` attribute to customize "Leave Group" action icon
  - `streamUiChannelActionsLeaveGroupEnabled` attribute to hide/show "Leave Group" action item
  - `streamUiChannelActionsDeleteConversationIcon` attribute to customize "Delete Conversation" action icon
  - `streamUiChannelActionsDeleteConversationEnabled` attribute to hide/show "Delete Conversation" action item
  - `streamUiChannelActionsCancelIcon` attribute to customize "Cancel" action icon
  - `streamUiChannelActionsCancelEnabled` attribute to hide/show "Cancel" action item
  - `streamUiChannelActionsBackground` attribute for dialog's background
- Added `streamUiIconOnlyVisibleToYou` attribute to `MessageListView` to allow customizing "Only visible to you" icon placed in messages footer
- Added `GiphyViewHolderStyle` to `MessageListViewStyle` to allow customizing `GiphyViewHolder`. The new style comes together with following `MessageListView` attributes:
  - `streamUiGiphyCardBackgroundColor` attribute to customize card's background color
  - `streamUiGiphyCardElevation` attribute to customize card's elevation
  - `streamUiGiphyCardButtonDividerColor` attribute to customize dividers' colors
  - `streamUiGiphyIcon` attribute to customize Giphy icon
  - `streamUiGiphyLabelTextSize`, `streamUiGiphyLabelTextColor`, `streamUiGiphyLabelTextFont`, `streamUiGiphyLabelTextFontAssets`, `streamUiGiphyLabelTextStyle` attributes to customize label
  - `streamUiGiphyQueryTextSize`, `streamUiGiphyQueryTextColor`, `streamUiGiphyQueryTextFont`, `streamUiGiphyQueryTextFontAssets`, `streamUiGiphyQueryTextStyle` attributes to customize query text
  - `streamUiGiphyCancelButtonTextSize`, `streamUiGiphyCancelButtonTextColor`, `streamUiGiphyCancelButtonTextFont`, `streamUiGiphyCancelButtonTextFontAssets`, `streamUiGiphyCancelButtonTextStyle` attributes to customize cancel button text
  - `streamUiGiphyShuffleButtonTextSize`, `streamUiGiphyShuffleButtonTextColor`, `streamUiGiphyShuffleButtonTextFont`, `streamUiGiphyShuffleButtonTextFontAssets`, `streamUiGiphyShuffleButtonTextStyle` attributes to customize shuffle button text
  - `streamUiGiphySendButtonTextSize`, `streamUiGiphySendButtonTextColor`, `streamUiGiphySendButtonTextFont`, `streamUiGiphySendButtonTextFontAssets`, `streamUiGiphySendButtonTextStyle` attributes to customize send button text
- Adding extra XML attrs allowing to customize "Send also to channel" CheckBox at `MessageInputView` component:
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDrawable`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxDirectChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxGroupChatText`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextStyle`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextColor`
  - `MessageInputView.streamUiSendAlsoToChannelCheckboxTextSize`
- Added `streamUiWarningMessageOptionsTextSize`, `streamUiWarningMessageOptionsTextColor`, `streamUiWarningMessageOptionsTextFont`, `streamUiWarningMessageOptionsFontAssets`, `streamUiWarningMessageOptionsTextStyle` attributes to `MessageListView` for customizing warning actions text appearance
- Deprecated multiple views' tint properties and attributes. Use custom drawables instead.
- Added `MediaAttachmentViewStyle` to allow customizing the appearance of media attachments in the message list. The new style comes together with following `MediaAttachmentView` attributes:
  - `progressIcon` - attribute to customize animated progress drawable when image is loading
  - `giphyIcon` - attribute to customize Giphy icon
  - `imageBackgroundColor` - attribute to customize image background color
  - `moreCountOverlayColor` - attribute to customize the color of "more count" semi-transparent overlay
  - `moreCountTextStyle` - attribute to customize text appearance of more count text
- Added `MessageReplyStyle` class allowing to customize MessageReply item view on MessageListView.
  Customization can be done using `TransformStyle` API or XML attributes of `MessageListView`:
  - `streamUiMessageReplyBackgroundColorMine`
  - `streamUiMessageReplyBackgroundColorTheirs`
  - `streamUiMessageReplyTextSizeMine`
  - `streamUiMessageReplyTextColorMine`
  - `streamUiMessageReplyTextFontMine`
  - `streamUiMessageReplyTextFontAssetsMine`
  - `streamUiMessageReplyTextStyleMine`
  - `streamUiMessageReplyTextSizeTheirs`
  - `streamUiMessageReplyTextColorTheirs`
  - `streamUiMessageReplyTextFontTheirs`
  - `streamUiMessageReplyTextFontAssetsTheirs`
  - `streamUiMessageReplyTextStyleTheirs`
  - `streamUiMessageReplyLinkColorMine`
  - `streamUiMessageReplyLinkColorTheirs`
  - `streamUiMessageReplyLinkBackgroundColorMine`
  - `streamUiMessageReplyLinkBackgroundColorTheirs`
  - `streamUiMessageReplyStrokeColorMine`
  - `streamUiMessageReplyStrokeWidthMine`
  - `streamUiMessageReplyStrokeColorTheirs`
  - `streamUiMessageReplyStrokeWidthTheirs`
- Added `FileAttachmentsViewStyle` class allowing to customize FileAttachmentsView item view on MessageListView.
- Added `MessageInputView::setSuggestionListViewHolderFactory` method which allows to provide custom views from suggestion list popup.

### ⚠️ Changed
- Changed the naming of string resources. The updated names can be reviewed in:
  - `strings_common.xml`
  - `strings_attachment_gallery.xml`
  - `strings_channel_list.xml`
  - `strings_channel_list_header.xml`
  - `strings_mention_list.xml`
  - `strings_message_input.xml`
  - `strings_message_list.xml`
  - `strings_message_list_header.xml`
  - `strings_search.xml`

# May 2nd, 2021 - 4.11.0
## Common changes for all artifacts
### 🐞 Fixed
- Fixed channel list sorting
### ⬆️ Improved
- Updated to Kotlin 1.5.10, coroutines 1.5.0
- Updated to Android Gradle Plugin 4.2.1
- Updated Room version to 2.3.0
- Updated Firebase, AndroidX, and other dependency versions to latest, [see here](https://github.com/GetStream/stream-chat-android/pull/1895) for more details
- Marked many library interfaces that should not be implemented by clients as [sealed](https://kotlinlang.org/docs/sealed-classes.html)
- Removed Fresco, PhotoDraweeView, and FrescoImageViewer dependencies (replaced by StfalconImageViewer)

## stream-chat-android
### 🐞 Fixed
- Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelsView`.
- Fixed link preview UI issues in old-ui package
- Fixed crashes when opening the image gallery.

## stream-chat-android-client
### 🐞 Fixed
- Fixed querying banned users using new serialization.
- Fixed the bug when wrong credentials lead to inability to login
- Fixed issues with Proguard stripping response classes in new serialization implementation incorrectly

### ⬆️ Improved
- Improved handling push notifications:
  - Added `ChatClient.handleRemoteMessage` for remote message handling
  - Added `ChatClient.setFirebaseToken` for setting Firebase token
  - Added `NotificationConfig::pushNotificationsEnabled` for disabling push notifications
  - Deprecated `ChatClient.onMessageReceived`
  - Deprecated `ChatClient.onNewTokenReceived`
  - Changed `ChatNotificationHandler::buildNotification` signature - it now receives `Channel` and `Message` and returns `NotificationCompat.Builder` for better customization
  - Deprecated `ChatNotificationHandler.getSmallIcon`
  - Deprecated `ChatNotificationHandler.getFirebaseMessageIdKey`
  - Deprecated `ChatNotificationHandler.getFirebaseChannelIdKey`
  - Deprecated `ChatNotificationHandler.getFirebaseChannelTypeKey`
  - Changed `ChatNotificationHandler::onChatEvent` - it now doesn't handle events by default and receives `NewMessageEvent` instead of generic `ChatEvent`
- Improved error description provided by `ChatClient::sendImage`, `ChatClient::sendFile`, `ChannelClient::sendImage` and `ChannelClient::sendFile` methods if upload fails.

### ✅ Added
- Added `ChatClient::truncateChannel` and `ChannelClient::truncate` methods to remove messages from a channel.
- Added `DisconnectCause` to `DisconnectedEvent`
- Added method `SocketListener::onDisconnected(cause: DisconnectCause)`
- Added possibility to group notifications:
  - Notifications grouping is disabled by default and can be enabled using `NotificationConfig::shouldGroupNotifications`
  - If enabled, by default notifications are grouped by Channel's cid
  - Notifications grouping can be configured using `ChatNotificationHandler` and `NotificationConfig`
- Added `ChatNotificationHandler::getFirebaseMessaging()` method in place of `ChatNotificationHandler::getFirebaseInstanceId()`.
  It should be used now to fetch Firebase token in the following way: `handler.getFirebaseMessaging()?.token?.addOnCompleteListener {...}`.
- Added `Message.attachmentsSyncStatus: SyncStatus` property.

### ⚠️ Changed
- Changed the return type of `FileUploader` methods from nullable string to `Result<String>`.
- Updated `firebase-messaging` library to the version `22.0.0`. Removed deprecated `FirebaseInstanceId` invocations from the project.

### ❌ Removed
- `ChatNotificationHandler::getFirebaseInstanceId()` due to `FirebaseInstanceId` being deprecated. It's replaced now with `ChatNotificationHandler::getFirebaseMessaging()`.

## stream-chat-android-ui-components
### 🐞 Fixed
Fixing filter for draft channels. Those channels were not showing in the results, even when the user asked for them. Now this is fixed and the draft channels can be included in the `ChannelListView`.
Fixed bug when for some video attachments activity with media player wasn't shown.

### ✅ Added
- Added `topLeft`, `topRight`, `bottomLeft`, `bottomRight` options to the `streamUiAvatarOnlineIndicatorPosition` attribute of `AvatarView` and corresponding constants to `AvatarView.OnlineIndicatorPosition` enum.

### ⚠️ Changed
- Swipe options of `ChannelListView` component:
  - "Channel more" option is now not shown by default because we are not able to provide generic, default implementation for it.
    If you want to make this option visible, you need to set `app:streamUiChannelOptionsEnabled="true"` explicitly to `io.getstream.chat.android.ui.channel.list.ChannelListView` component.
  - "Channel delete" option has now default implementation. Clicking on the "delete" icon shows AlertDialog asking to confirm Channel deletion operation.

# May 11th, 2021 - 4.10.0
## stream-chat-android-client
### 🐞 Fixed
- Fixed the usage of `ProgressCallback` in `ChannelClient::sendFile` and `ChannelClient::sendImage` methods.

### ✅ Added
- Added `ChannelClient::deleteFile` and `ChannelClient::deleteImage` methods.
- Added `NotificationInviteRejectedEvent`
- Added `member` field to the `NotificationRemovedFromChannel` event
- Added `totalUnreadCount` and `unreadChannels` fields to the following events:
- `notification.channel_truncated`
- `notification.added_to_channel`
- `notification.channel_deleted`
- Added `channel` field to the `NotificationInviteAcceptedEvent` event
- Added `channel` field to the `NotificationInviteRejectedEvent` event

### ⚠️ Changed
- **The client now uses a new serialization implementation by default**, which was [previously](https://github.com/GetStream/stream-chat-android/releases/tag/4.8.0) available as an opt-in API.
  - This new implementation is more performant and greatly improves type safety in the networking code of the SDK.
  - If you experience any issues after upgrading to this version of the SDK, you can call `useNewSerialization(false)` when building your `ChatClient` to revert to using the old implementation. Note however that we'll be removing the old implementation soon, so please report any issues found.
  - To check if the new implementation is causing any failures in your app, enable error logging on `ChatClient` with the `logLevel` method, and look for the `NEW_SERIALIZATION_ERROR` tag in your logs while using the SDK.
- Made the `user` field in `channel.hidden` and `notification.invite_accepter` events non nullable.
- Updated channels state after `NotificationInviteRejectedEvent` or `NotificationInviteAcceptedEvent` is received

### ❌ Removed
- Removed redundant events which can only be received by using webhooks:
  - `channel.created`
  - `channel.muted`
  - `channel.unmuted`
  - `channel.muted`
  - `channel.unmuted`
- Removed `watcherCount` field from the following events as they are not returned with the server response:
  - `message.deleted`
  - `message.read`
  - `message.updated`
  - `notification.mark_read`
- Removed `user` field from the following events as they are not returned with the server response:
  - `notification.channel_deleted`
  - `notification.channel_truncated`
## stream-chat-android-offline
### 🐞 Fixed
- Fixed an issue when CustomFilter was configured with an int value but the value from the API was a double value
### ⚠️ Changed

- Changed the upload logic in `ChannelController` for the images unsupported by the Stream CDN. Now such images are uploaded as files via `ChannelClient::sendFile` method.
### ❌ Removed

## stream-chat-android-ui-common
### ⬆️ Improved
- Updated ExoPlayer version to 2.13.3

### ⚠️ Changed
- Deprecated `MessageInputViewModel::editMessage`. Use `MessageInputViewModel::messageToEdit` and `MessageInputViewModel::postMessageToEdit` instead.
- Changed `MessageInputViewModel::repliedMessage` type to `LiveData`. Use `ChatDomain::setMessageForReply` for setting message for reply.
- Changed `MessageListViewModel::mode` type to `LiveData`. Mode is handled internally and shouldn't be modified outside the SDK.

## stream-chat-android-ui-components
### 🐞 Fixed
- Removed empty badge for selected media attachments.

### ✅ Added
- Added `messageLimit` argument to `ChannelListViewModel` and `ChannelListViewModelFactory` constructors to allow changing the number of fetched messages for each channel in the channel list.

# April 30th, 2021 - 4.9.2
## stream-chat-android-offline
### ✅ Added
- Added `ChatDomain::user`, a new property that provide the current user into a LiveData/StateFlow container

### ⚠️ Changed
- `ChatDomain::currentUser` has been warning-deprecated because it is an unsafe property that could be null, you should subscribe to `ChatDomain::user` instead

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed NPE on MessageInputViewModel when the it was initialized before the user was set

# April 29th, 2021 - 4.9.1
## stream-chat-android
### ⬆️ Improved
* Updated coil dependency to the latest version. This fixes problem with .heic, and .heif attachment metadata parsing.

## stream-chat-android-client
### 🐞 Fixed
- Optimized the number of `ChatClient::addDevice` API calls

### ⬆️ Improved
- Events received after the client closes the connection are rejected

## stream-chat-android-offline
### 🐞 Fixed
- Fixed offline reactions sync

### ✅ Added
- Added new versions with API based on kotlin `StateFlow` for the following classes:
  * `io.getstream.chat.android.offline.ChatDomain`
  * `io.getstream.chat.android.offline.channel.ChannelController`
  * `io.getstream.chat.android.offline.thread.ThreadController`
  * `io.getstream.chat.android.offline.querychannels.QueryChannelsController`

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed crash related to accessing `ChatDomain::currentUser` in `MessageListViewModel` before user is connected

## stream-chat-android-ui-components
### ⬆️ Improved
* Updated coil dependency to the latest version. This fixes problem with .heic, and .heif attachment metadata parsing.

### ✅ Added
Customization of icons in Attachment selection dialog
you can use:
- app:streamUiPictureAttachmentIcon
  Change the icon for the first item in the list of icons
- app:streamUiPictureAttachmentIconTint
  Change the tint color for icon of picture selection
- app:streamUiFileAttachmentIcon
  Change the icon for the second item in the list of icons
- app:streamUiFileAttachmentIconTint
  Change the tint color for icon of file selection
- app:streamUiCameraAttachmentIcon
  Change the icon for the third item in the list of icons
- app:streamUiCameraAttachmentIconTint
  Change the tint color for icon of camera selection
- Added support for error messages
- Added attrs to `MessageListView` that allow to customize error message text style:
  * `streamUiErrorMessageTextSize`
  * `streamUiErrorMessageTextColor`
  * `streamUiErrorMessageTextFont`
  * `streamUiErrorMessageTextFontAssets`
  * `streamUiErrorMessageTextStyle`

# April 21th, 2021 - 4.9.0
## Common changes for all artifacts
### ✅ Added
Added icon to show when channel is muted in ChannelListView.
It is possible to customize the color and the drawable of the icon.

## stream-chat-android
### 🐞 Fixed
- Fixed multiline messages which were displayed in a single line

### ❌ Removed
- Removed deprecated `MessageListView::setViewHolderFactory` method
- Removed deprecated `Chat` interface

## stream-chat-android-client
### 🐞 Fixed
- Fixed: local cached hidden channels stay hidden even though new message is received.
- Make `Flag::approvedAt` nullable
- Fixed error event parsing with new serialization implementation

### ✅ Added
- Added `ChatClient::updateChannelPartial` and `ChannelClient::updatePartial` methods for partial updates of channel data.

### ⚠️ Changed
- Deprecated `ChannelClient::unBanUser` method
- Deprecated `ChatClient::unBanUser` method
- Deprecated `ChatClient::unMuteChannel` method

### ❌ Removed
- Removed deprecated `ChatObservable` class and all its uses
- Removed deprecated `ChannelControler` interface

## stream-chat-android-offline
### ✅ Added
- Added the following use case functions to `ChatDomain` which are supposed to replace `ChatDomain.useCases` property:
  * `ChatDomain::replayEventsForActiveChannels` Adds the provided channel to the active channels and replays events for all active channels.
  * `ChatDomain::getChannelController` Returns a `ChannelController` for given cid.
  * `ChatDomain::watchChannel` Watches the given channel and returns a `ChannelController`.
  * `ChatDomain::queryChannels` Queries offline storage and the API for channels matching the filter. Returns a queryChannelsController.
  * `ChatDomain::getThread` Returns a thread controller for the given channel and message id.
  * `ChatDomain::loadOlderMessages` Loads older messages for the channel.
  * `ChatDomain::loadNewerMessages` Loads newer messages for the channel.
  * `ChatDomain::loadMessageById` Loads message for a given message id and channel id.
  * `ChatDomain::queryChannelsLoadMore` Load more channels for query.
  * `ChatDomain::threadLoadMore` Loads more messages for the specified thread.
  * `ChatDomain::createChannel` Creates a new channel.
  * `ChatDomain::sendMessage` Sends the message.
  * `ChatDomain::cancelMessage` Cancels the message of "ephemeral" type.
  * `ChatDomain::shuffleGiphy` Performs giphy shuffle operation.
  * `ChatDomain::sendGiphy` Sends selected giphy message to the channel.
  * `ChatDomain::editMessage` Edits the specified message.
  * `ChatDomain::deleteMessage` Deletes the specified message.
  * `ChatDomain::sendReaction` Sends the reaction.
  * `ChatDomain::deleteReaction` Deletes the specified reaction.
  * `ChatDomain::keystroke` It should be called whenever a user enters text into the message input.
  * `ChatDomain::stopTyping` It should be called when the user submits the text and finishes typing.
  * `ChatDomain::markRead` Marks all messages of the specified channel as read.
  * `ChatDomain::markAllRead` Marks all messages as read.
  * `ChatDomain::hideChannel` Hides the channel with the specified id.
  * `ChatDomain::showChannel` Shows a channel that was previously hidden.
  * `ChatDomain::leaveChannel` Leaves the channel with the specified id.
  * `ChatDomain::deleteChannel` Deletes the channel with the specified id.
  * `ChatDomain::setMessageForReply` Set the reply state for the channel.
  * `ChatDomain::downloadAttachment` Downloads the selected attachment to the "Download" folder in the public external storage directory.
  * `ChatDomain::searchUsersByName` Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name in local database.
  * `ChatDomain::queryMembers` Query members of a channel.
- Added `ChatDomain::removeMembers` method
- Added `ChatDomain::createDistinctChannel` A use-case for creating a channel based on its members.
- Added `ChatDomain::removeMembers` method

### ⚠️ Changed
- Deprecated `ChatDomain.useCases`. It has `DeprecationLevel.Warning` and still can be used. However, it will be not available in the future, so please consider migrating to use `ChatDomain` use case functions instead.
- Deprecated `GetUnreadChannelCount`
- Deprecated `GetTotalUnreadCount`

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed compatibility with latest Dagger Hilt versions

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed not perfectly rounded avatars
- `MessageInputView::UserLookupHandler` is not overrided everytime that members livedata is updated
- Fixed doubled command prefix when the command contains user mention
- Fixed handling user mute state in default `MessageListViewOptions` dialog
- Fixed incorrect "last seen" text
- Fixed multiline messages which were displayed in a single line

### ⬆️ Improved
- Setting external SuggestionListView is no longer necessary to display suggestions popup
### ✅ Added
- Added `ChatUI.supportedReactions: SupportedReactions` property, also introduced `SupportedReactions`, and `ReactionDrawable` class.
  It allows defining a set of supported reactions by passing a `Map<String, ReactionDrawable>` in constructor. `ReactionDrawable` is a wrapping class holding two `Drawable` instances - for active and inactive reaction states.
- Added methods and attrs to `MessageListView` that allow to customize visibility of message options:
  * `MessageListView::setDeleteMessageConfirmationEnabled`
  * `MessageListView::setCopyMessageEnabled`
  * `MessageListView::setBlockUserEnabled`
  * `MessageListView::setMuteUserEnabled`
  * `MessageListView::setMessageFlagEnabled`
  * `MessageListView::setReactionsEnabled`
  * `MessageListView::setRepliesEnabled`
  * `MessageListView::setThreadsEnabled`
  * `MessageListView.streamUiFlagMessageOptionEnabled`
  * `MessageListView.streamUiMuteUserOptionEnabled`
  * `MessageListView.streamUiBlockUserOptionEnabled`
  * `MessageListView.streamUiCopyMessageActionEnabled`
- Added confirmation dialog for flagging message option:
  * Added `MessageListView::flagMessageConfirmationEnabled` attribute
- Added `MessageListView::setFlagMessageResultHandler` which allows to handle flag message result
- Added support for system messages
- Added attrs to `MessageListView` that allow to customize system message text style:
  * `streamUiSystemMessageTextSize`
  * `streamUiSystemMessageTextColor`
  * `streamUiSystemMessageTextFont`
  * `streamUiSystemMessageTextFontAssets`
  * `streamUiSystemMessageTextStyle`
- Added attrs to `MessageListView` that allow to customize message option text style:
  * `streamUiMessageOptionsTextSize`
  * `streamUiMessageOptionsTextColor`
  * `streamUiMessageOptionsTextFont`
  * `streamUiMessageOptionsTextFontAssets`
  * `streamUiMessageOptionsTextStyle`
- Added attrs to `MessageListView` that allow to customize user reactions title text style:
  * `streamUiUserReactionsTitleTextSize`
  * `streamUiUserReactionsTitleTextColor`
  * `streamUiUserReactionsTitleTextFont`
  * `streamUiUserReactionsTitleTextFontAssets`
  * `streamUiUserReactionsTitleTextStyle`
- Added attrs to `MessageListView` that allow to customize colors of message options background, user reactions card background, overlay dim color and warning actions color:
  * `streamUiMessageOptionBackgroundColor`
  * `streamUiUserReactionsBackgroundColor`
  * `streamUiOptionsOverlayDimColor`
  * `streamUiWarningActionsTintColor`
- Added `ChatUI.mimeTypeIconProvider: MimeTypeIconProvider` property which allows to customize file attachment icons.

### ⚠️ Changed
- Now the "block user" feature is disabled. We're planning to improve the feature later. Stay tuned!
- Changed gallery background to black in dark mode

# April 8th, 2021 - 4.8.1
## Common changes for all artifacts
### ⚠️ Changed
- We've cleaned up the transitive dependencies that our library exposes to its clients. If you were using other libraries implicitly through our SDK, you'll now have to depend on those libraries directly instead.

## stream-chat-android
### 🐞 Fixed
- Fix Attachment Gravity

### ✅ Added
- Provide AvatarView class

## stream-chat-android-offline
### 🐞 Fixed
- Fix Crash on some devices that are not able to create an Encrypted SharedPreferences
- Fixed the message read indicator in the message list
- Added missing `team` field to `ChannelEntity` and `ChannelData`

### ✅ Added
- Add `ChatDomain::removeMembers` method

## stream-chat-android-ui-common
### 🐞 Fixed
- Fixed getting files provided by content resolver.

### ⚠️ Changed
- Added theme to all activities all the SDK. You can override then in your project by redefining the styles:
- StreamUiAttachmentGalleryActivityStyle
- StreamUiAttachmentMediaActivityStyle
- StreamUiAttachmentActivityStyle

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed attr streamUiCopyMessageActionEnabled. From color to boolean.
- Now it is possible to change the color of `MessageListHeaderView` from the XML.
- Fixed the `MessageListView::setUserClickListener` method.
- Fixed bugs in handling empty states for `ChannelListView`. Deprecated manual methods for showing/hiding empty state changes.
- Fix `ChannelListHeaderView`'s title position when user avatar or action button is invisible
- Fix UI behaviour for in-progress file uploads
- Fix extension problems with file uploads when attachment names contain spaces
- Fix reaction bubbles which were shown behind message attachment views

### ✅ Added
- Now it is possible to change the back button of MessageListHeaderView using `app:streamUiMessageListHeaderBackButtonIcon`
- Now it is possible to inject `UserLookupHandler` into `MessageInputView` in order to implement custom users' mention lookup algorithm

# March 31th, 2021 - 4.8.0
## Common changes for all artifacts
### 🐞 Fixed
Group channels with 1<>1 behaviour the same way as group channels with many users
It is not possible to remove users from distinct channels anymore.
### ⬆️ Improved
it is now possible to configure the max lines of a link description. Just use
`app:streamUiLinkDescriptionMaxLines` when defining MessageListView

It is now possible to configure the max size of files and an alert is shown when
a files bigger than this is selected.
### ✅ Added
Configure enable/disable of replies using XML in `MessageListView`
Option `app:streamUiReactionsEnabled` in `MessageListView` to enable or disable reactions
It is possible now to configure the max size of the file upload using
`app:streamUiAttachmentMaxFileSizeMb`

## stream-chat-android
### 🐞 Fixed
- Fixed crash when sending GIF from Samsung keyboard

## stream-chat-android-client
### 🐞 Fixed
- Fixed parsing of `createdAt` property in `MessageDeletedEvent`

### ⬆️ Improved
- Postponed queries as run as non-blocking

### ✅ Added
- **Added a brand new serialization implementation, available as an opt-in API for now.** This can be enabled by making a `useNewSerialization(true)` call on the `ChatClient.Builder`.
  - This new implementation will be more performant and greatly improve type safety in the networking code of the SDK.
  - The old implementation remains the default for now, while we're making sure the new one is bug-free.
  - We recommend that you opt-in to the new implementation and test your app with it, so that you can report any issues early and we can get them fixed before a general rollout.
- Added `unflagMessage(messageId)` and `unflagUser(userId)` methods to `ChatClient`
- Added support for querying banned users - added `ChatClient::queryBannedUsers` and `ChannelClient::queryBannedUsers`
- Added `uploadsEnabled`, `urlEnrichmentEnabled`, `customEventsEnabled`, `pushNotificationsEnabled`, `messageRetention`, `automodBehavior` and `blocklistBehavior` fields to channel config

### ⚠️ Changed
- Renamed `ChannelId` property to `channelId` in both `ChannelDeletedEvent` and `NotificationChannelDeletedEvent`
- Deprecated `ChatClient::unMuteChannel`, the `ChatClient::unmuteChannel` method should be used instead
- Deprecated `ChatClient::unBanUser`, the `ChatClient::unbanUser` method should be used instead
- Deprecated `ChannelClient::unBanUser`, the `ChannelClient::unbanUser` method should be used instead
- Deprecated `ChannelController::unBanUser`, the `ChannelController::unbanUser` method should be used instead

## stream-chat-android-offline
### 🐞 Fixed
- Fixed an issue that didn't find the user when obtaining the list of messages
- Fix refreshing not messaging channels which don't contain current user as a member

## stream-chat-android-ui-common
### ⬆️ Improved
- Show AttachmentMediaActivity for video attachments

### ✅ Added
- `AvatarView.streamUiAvatarOnlineIndicatorColor` and `AvatarView.streamUiAvatarOnlineIndicatorBorderColor` attrs

## stream-chat-android-ui-components
### 🐞 Fixed
- Now replied messages are shown correctly with the replied part in message options
- `MessageListView::enterThreadListener` is properly notified when entering into a thread
- Fix initial controls state in `MessageInputView`
- Fix crashing when open attachments destination

### ⬆️ Improved
- Add support of non-image attachment types to the default attachment click listener.

### ✅ Added
- `MessageInputView` now uses the cursor `stream_ui_message_input_cursor.xml` instead of accent color. To change the cursor, override `stream_ui_message_input_cursor.xml`.
- Replacing `ChatUI` with new `io.getstream.chat.android.ui.ChatUI` implementation
- Added possibility to configure delete message option visibility using `streamUiDeleteMessageEnabled` attribute, and `MessageListView::setDeleteMessageEnabled` method
- Add `streamUiEditMessageEnabled` attribute to `MessageListView` and `MessageListView::setEditMessageEnabled` method to enable/disable the message editing feature
- Add `streamUiMentionsEnabled` attribute to `MessageInputView` and `MessageInputView::setMentionsEnabled` method to enable/disable mentions
- Add `streamUiThreadsEnabled` attribute to `MessageListView` and `MessageListView::setThreadsEnabled` method to enable/disable the thread replies feature
- Add `streamUiCommandsEnabled` attribute to `MessageInputView` and `MessageInputView::setCommandsEnabled` method to enable/disable commands
- Add `ChannelListItemPredicate` to our `channelListView` to allow filter `ChannelListItem` before they are rendered
- Open `AvatarBitmapFactory` class
- Add `ChatUI::avatarBitmapFactory` property to allow custom implementation of `AvatarBitmapFactory`
- Add `AvatarBitmapFactory::userBitmapKey` method to generate cache key for a given User
- Add `AvatarBitmapFactory::channelBitmapKey` method to generate cache key for a given Channel
- Add `StyleTransformer` class to allow application-wide style customizations
- Add the default font field to `TextStyle`
- Add new method `ChatFonts::setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface)`
- Add attributes for `MessageListView` in order to customize styles of:
  - Mine message text
  - Theirs message text
  - User name text in footer of Message
  - Message date in footer of Message
  - Thread replies counter in footer of Message
  - Link title text
  - Link description text
  - Date separator text
  - Deleted message text and background
  - Reactions style in list view and in options view
  - Indicator icons in footer of Message
  - Unread count badge on scroll to bottom button
  - Message stroke width and color for mine and theirs types
    It is now possible to customize the following attributes for `ChannelListView`:
- `streamUiChannelOptionsIcon` - customize options icon
- `streamUiChannelDeleteIcon` - customize delete icon
- `streamUiChannelOptionsEnabled` - hide/show options icon
- `streamUiChannelDeleteEnabled` - hide/show delete button
- `streamUiSwipeEnabled` - enable/disable swipe action
- `streamUiBackgroundLayoutColor` - customize the color of "background layout"
- `streamUiChannelTitleTextSize` - customize channel name text size
- `streamUiChannelTitleTextColor` - customize channel name text color
- `streamUiChannelTitleTextFont` - customize channel name text font
- `streamUiChannelTitleFontAssets` - customize channel name font asset
- `streamUiChannelTitleTextStyle` - customize channel name text style (normal / bold / italic)
- `streamUiLastMessageTextSize` - customize last message text size
- `streamUiLastMessageTextColor` - customize last message text color
- `streamUiLastMessageTextFont` - customize last message text font
- `streamUiLastMessageFontAssets` - customize last message font asset
- `streamUiLastMessageTextStyle` - customize last message text style (normal / bold / italic)
- `streamUiLastMessageDateTextSize` - customize last message date text size
- `streamUiLastMessageDateTextColor` - customize last message date text color
- `streamUiLastMessageDateTextFont` - customize last message date text font
- `streamUiLastMessageDateFontAssets` - customize last message date font asset
- `streamUiLastMessageDateTextStyle` - customize last message date text style (normal / bold / italic)
- `streamUiIndicatorSentIcon` - customize drawable indicator for sent
- `streamUiIndicatorReadIcon` - customize drawable indicator for read
- `streamUiIndicatorPendingSyncIcon` - customize drawable indicator for pending sync
- `streamUiForegroundLayoutColor` - customize the color of "foreground layout"
- `streamUiUnreadMessageCounterBackgroundColor` - customize the color of message counter badge
- `streamUiUnreadMessageCounterTextSize` - customize message counter text size
- `streamUiUnreadMessageCounterTextColor` - customize message counter text color
- `streamUiUnreadMessageCounterTextFont` - customize message counter text font
- `streamUiUnreadMessageCounterFontAssets` - customize message counter font asset
- `streamUiUnreadMessageCounterTextStyle` - customize message counter text style (normal / bold / italic)
- Option `app:streamUiReactionsEnabled` in `MessageListView` to enable or disable reactions
- It is now possible to configure new fields in MessageInputView:
- `streamUiMessageInputTextStyle` - customize message input text style.
- `streamUiMessageInputFont` - customize message input text font.
- `streamUiMessageInputFontAssets` - customize message input text font assets.
- `streamUiMessageInputEditTextBackgroundDrawable` - customize message input EditText drawable.
- `streamUiMessageInputCustomCursorDrawable` - customize message input EditText cursor drawable.
- `streamUiCommandsTitleTextSize` - customize command title text size
- `streamUiCommandsTitleTextColor` - customize command title text color
- `streamUiCommandsTitleFontAssets` - customize command title text color
- `streamUiCommandsTitleTextColor` - customize command title font asset
- `streamUiCommandsTitleFont` - customize command title text font
- `streamUiCommandsTitleStyle` - customize command title text style
- `streamUiCommandsNameTextSize` - customize command name text size
- `streamUiCommandsNameTextColor` - customize command name text color
- `streamUiCommandsNameFontAssets` - customize command name text color
- `streamUiCommandsNameTextColor` - customize command name font asset
- `streamUiCommandsNameFont` - customize command name text font
- `streamUiCommandsNameStyle` - customize command name text style
- `streamUiCommandsDescriptionTextSize` - customize command description text size
- `streamUiCommandsDescriptionTextColor` - customize command description text color
- `streamUiCommandsDescriptionFontAssets` - customize command description text color
- `streamUiCommandsDescriptionTextColor` - customize command description font asset
- `streamUiCommandsDescriptionFont` - customize command description text font
- `streamUiCommandsDescriptionStyle` - customize command description text style
- `streamUiSuggestionBackgroundColor` - customize suggestion view background
- `streamUiMessageInputDividerBackgroundDrawable` - customize the background of divider of MessageInputView

### ⚠️ Changed
- Deprecated `ChatUI` class

# March 8th, 2021 - 4.7.0
## stream-chat-android-client
### ⚠️ Changed
- Refactored `FilterObject` class  - see the [migration guide](https://github.com/GetStream/stream-chat-android/wiki/Migration-guide:-FilterObject) for more info

## stream-chat-android-offline
### 🐞 Fixed
- Fixed refreshing channel list after removing member
- Fixed an issue that didn't find the user when obtaining the list of messages

### ⚠️ Changed
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.
- Deprecated constructors for `ChatDomain.Builder` with the `User` type parameter, use constructor with `Context` and `ChatClient` instead.

## stream-chat-android-ui-common
### ⚠️ Changed
- Message options list changed colour for dark version. The colour is a little lighters
  now, what makes it easier to see.

## stream-chat-android-ui-components
### 🐞 Fixed
- Fixed some rare crashes when `MessageListView` was created without any attribute info present

### ⬆️ Improved
- Updated PhotoView to version 2.3.0

### ✅ Added
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Introduced `TextAndAttachmentsViewHolder` for any combination of attachment content and text

### ❌ Removed
- Deleted `OnlyFileAttachmentsViewHolder`, `OnlyMediaAttachmentsViewHolder`,
  `PlainTextWithMediaAttachmentsViewHolder` and `PlainTextWithFileAttachmentsViewHolder`

# Feb 22th, 2021 - 4.6.0
# New UI-Components Artifact
A new UI-Components artifact has been created with a new design of all our components.
This new artifact is available on MavenCentral and can imported by adding the following dependency:
```
implementation "io.getstream:stream-chat-android-ui-components:4.6.0"
```

## stream-chat-android
- Add `streamMessageActionButtonsTextSize`, `streamMessageActionButtonsTextColor`, `streamMessageActionButtonsTextFont`,
  `streamMessageActionButtonsTextFontAssets`, `streamMessageActionButtonsTextStyle`, `streamMessageActionButtonsIconTint`
  attributes to `MessageListView`
- Add `ChannelHeaderViewModel::resetThread` method and make `ChannelHeaderViewModel::setActiveThread` message parameter non-nullable
- Fix ReadIndicator state
- Using `MessageListView#setViewHolderFactory` is now an error - use `setMessageViewHolderFactory` instead
- Removed `MessageListItemAdapter#replaceEntities` - use `submitList` method instead
- Use proper color values on Dialog Theme
- Increase touchable area on the button to remove an attachment

## stream-chat-android-client
- Introduce ChatClient::setUserWithoutConnecting function
- Handle disconnect event during pending token state
- Remove unneeded user data when creating WS Connection
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

# Feb 15th, 2021 - 4.5.5
## Common changes for all artifacts
- Updated project dependencies
  - Kotlin 1.4.30
  - Stable AndroidX releases: LiveData 2.3.0, Activity 1.2.0, Fragment 1.3.0
  - For the full list of dependency version changes, see [this PR](https://github.com/GetStream/stream-chat-android/pull/1383)

## stream-chat-android
- Add `streamInputAttachmentsMenuBackground` and `streamInputSuggestionsBackground` attributes to `MessageInputView`
- Add `streamMessageActionButtonsBackground` attributes to `MessageListView`

## stream-chat-android-client
- Remove unused `reason` and `timeout` parameters from `ChannelClient::unBanUser` method

# Feb 11th, 2021 - 4.5.4
## stream-chat-android
- Fix `streamLastMessageDateUnreadTextColor` attribute not being used in ChannelListView
- Fix `streamChannelsItemSeparatorDrawable` attribute not being parsed

## stream-chat-android-client
- Fix `ConcurrentModificationException` on our `NetworkStateProvider`

# Feb 5th, 2021 - 4.5.3
## stream-chat-android
-. `ChatUtils::devToken` is not accessible anymore, it has been moved to `ChatClient::devToken`

## stream-chat-android-client
- **setUser deprecation**
  - The `setUser`, `setGuestUser`, and `setAnonymousUser` methods on `ChatClient` are now deprecated.
  - Prefer to use the `connectUser` (`connectGuestUser`, `connectAnonymousUser`) methods instead, which return `Call` objects.
  - If you want the same async behaviour as with the old methods, use `client.setUser(user, token).enqueue { /* Handle result */ }`.
- Add support for typing events in threads:
  - Add `parentId` to `TypingStartEvent` and `TypingStopEvent`
  - Add `parentId` to ``ChannelClient::keystroke` and `ChannelClient::stopTyping`
- `ChatClient::sendFile` and `ChatClient::sendImage` each now have just one definition with `ProgressCallback` as an optional parameter. These methods both return `Call<String>`, allowing for sync/async execution, and error handling. The old overloads that were asynchronous and returned no value/error have been removed.
- `FileUploader::sendFile` and `FileUploader::sendImages` variations with `ProgressCallback` are no longer async with no return type. Now they are synchronous with `String?` as return type

## stream-chat-android-offline
- Add support for typing events in threads:
  - Add `parentId` to `Keystroke` and `StopTyping` use cases

## stream-chat-android-ui-common
- Add a new `isMessageRead` flag to the `MessageListItem.MessageItem` class, which indicates
  that a particular message is read by other members in this channel.
- Add handling threads typing in `MessageInputViewModel`

# Jan 31th, 2021 - 4.5.2
## stream-chat-android-client
- Use proper data on `ChatClient::reconnectSocket` to reconnect normal/anonymous user
- Add `enforceUnique` parameter to `ChatClient::sendReaction` and `ChannelClient::sendReaction` methods .
  If reaction is sent with `enforceUnique` set to true, new reaction will replace all reactions the user has on this message.
- Add suspending `setUserAndAwait` extension for `ChatClient`
- Replace chat event listener Kotlin functions with ChatEventListener functional interface in order to promote
  a better integration experience for Java clients. Old methods that use the Kotlin function have been deprecated.
  Deprecated interfaces, such as ChannelController, have not been updated. ChannelClient, which inherits from ChannelController
  for the sake of backwards compatibility, has been updated.

## stream-chat-android-offline
- Add `enforceUnique` parameter to `SendReaction` use case. If reaction is sent with `enforceUnique` set to true,
  new reaction will replace all reactions the user has on this message.
- Fix updating `Message::ownReactions` and `Message:latestReactions` after sending or deleting reaction - add missing `userId` to `Reaction`
- Fix Load Thread Replies process

## stream-chat-android-ui-common
- Add a new `isThreadMode` flag to the `MessageListItem.MessageItem` class.
  It shows is a message item should be shown as part of thread mode in chat.
- Add possibility to set `DateSeparatorHandler` via `MessageListViewModel::setDateSeparatorHandler`
  and `MessageListViewModel::setThreadDateSeparatorHandler` which determines when to add date separator between messages
- Add `MessageListViewModel.Event.ReplyAttachment`, `MessageListViewModel.Event.DownloadAttachment`, `MessageListViewModel.Event.ShowMessage`,
  and `MessageListViewModel.Event.RemoveAttachment` classes.
- Deprecate `MessageListViewModel.Event.AttachmentDownload`

# Jan 18th, 2021 - 4.5.1
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

# Jan 7th, 2021 - 4.5.0
## stream-chat-android
- Now depends explicitly on AndroidX Fragment (fixes a potential crash with result handling)
- Update AndroidX dependencies: Activity 1.2.0-rc01 and Fragment 1.3.0-rc01

## stream-chat-android-client
- Add filtering non image attachments in ChatClient::getImageAttachments
- Add a `channel` property to `notification.message_new` events
- Fix deleting channel error
- 🚨 Breaking change: ChatClient::unmuteUser, ChatClient::unmuteCurrentUser,
  ChannelClient::unmuteUser, and ChannelClient::unmuteCurrentUser now return Unit instead of Mute

## stream-chat-android-offline
- Add LeaveChannel use case
- Add ChannelData::memberCount
- Add DeleteChannel use case
- Improve loading state querying channels
- Improve loading state querying messages

# Dec 18th, 2020 - 4.4.9

## stream-chat-android-client
- improved event recovery behaviour

## stream-chat-android-offline
- improved event recovery behaviour
- fixed the chatDomain.Builder boolean usage between userPresence and recoveryEnabled

# Dec 18th, 2020 - 4.4.8
## stream-chat-android
- Add filtering `shadowed` messages when computing last channel message
- Add filtering `draft` channels
- Add `DateFormatter::formatTime` method to format only time of a date
- Fix `ChatUtils::devToken` method

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

# Dec 14th, 2020 - 4.4.7
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
- Add `ChatClient::isValidRemoteMessage` method to know if a RemoteMessage is valid for Stream

## stream-chat-android-offline
- Add updating `channelData` after receiving `ChannelUpdatedByUserEvent`
- Fix crash when a push notification arrives from other provider different than Stream

# Dic 4th, 2020 - 4.4.6

## stream-chat-android
- Use custom `StreamFileProvider` instead of androidx `FileProvider` to avoid conflicts
- Add `ChatClient::setGuestUser` method to login as a guest user
- Make `MessageListItemViewHolder` public and open, to allow customization by overriding the `bind` method

## stream-chat-android-offline
- Centralize how channels are stored locally

# Nov 24th, 2020 - 4.4.5
## Common changes for all artifacts
- Stream SDks has been uploaded to MavenCentral and the GroupID has changed to `io.getstream`.

## stream-chat-android
- New artifact name: `io.getstream:stream-chat-android:STREAM_VERSION`

## stream-chat-android-client
- It's no longer required to wait for `setUser` to finish before querying channels
- `ChatClient::setUser` method allows be called without network connection and will retry to connect when network connection is available
- New artifact name: `io.getstream:stream-chat-android-client:STREAM_VERSION`
- Show date of the last message into channels list when data comes from offline storage
- Show text of the last message into channels list when data comes from offline storage
- Accept Invite Message is now optional, if null value is sent, no message will be sent to the rest of members about this action

## stream-chat-android-offline
- Fix bug when channels with newer messages don't go to the first position in the list
- Fix Offline usage of `ChatDomain`
- New artifact name: `io.getstream:stream-chat-android-offline:STREAM_VERSION`
- Provide the last message when data is load from offline storage

# Nov 24th, 2020 - 4.4.4
This version is a rollback to 4.4.2, The previous release (4.4.3) was not valid due to a problem with the build flow.
We are going to release 4.4.5 with the features introduced by 4.4.3 as soon as the build is back working

# Nov 20th, 2020 - 4.4.3
## stream-chat-android-client
- It's no longer required to wait for `setUser` to finish before querying channels
- `ChatClient::setUser` method allows be called without network connection and will retry to connect when network connection is available

## stream-chat-android-offline
- Fix bug when channels with newer messages don't go to the first position in the list
- Fix Offline usage of `ChatDomain`

# Nov 13th, 2020 - 4.4.2

## stream-chat-android
- Remove `ChatClient` and `ChatDomain` as `ChatUI`'s dependencies
- Replace Glide with Coil - SDK doesn't depend on Glide anymore.
- Remove `BaseStyle` class and extract its properties into `AvatarStyle` and `ReadStateStyle`.
  - Use composition with `AvatarStyle` and `ReadStateStyle` instead of inheriting from `BaseStyle`.
  - Convert to kotlin: `ReadStateView`, `MessageListViewStyle`
- Add `streamShowSendAlsoToChannelCheckbox` attr to `MessageInputView` controlling visibility of "send also to channel" checkbox
- The sample app no longer uses Koin for dependency injection
- Add `streamCopyMessageActionEnabled`, `streamFlagMessageActionEnabled`, and `streamStartThreadMessageActionEnabled` attrs to `MessageListView`
- Validate message text length in MessageInputView.
  - Add property `MessageInputView.maxMessageLength: Int` and show warning once the char limit is exceeded
  - Expose `MessageInputViewModel.maxMessageLength: Int` informing about text length limit of the Channel

## stream-chat-android-client
- Deprecate `User::unreadCount` property, replace with `User::totalUnreadCount`
- Added MarkAllReadEvent
- Fix UpdateUsers call

## stream-chat-android-offline
- Update `totalUnreadCount` when user is connected
- Update `channelUnreadCount` when user is connected
- Fix bug when channels could be shown without names
- Added support for marking all channels as read for the current user.
  - Can be accessed via `ChatDomain`'s use cases (`chatDomain.useCases.markAllRead()...`).
- Fix bug when local channels could be sorted not properly
- Typing events can be all tracked with `ChatDomain.typingUpdates`

# Nov 4th, 2020 - 4.4.1
## Common changes for all artifacts
- Updated dependencies to latest versions (AGP 4.1, OkHttp 4.9, Coroutines 1.3.9, ExoPlayer 2.12.1, etc.)
  - See [PR #757](https://github.com/GetStream/stream-chat-android/pull/757) for full list of version updates
- Revamped `Call` implementations
  - The `Call2` type has been removed, the libraries now all use the same `Call` instead for all APIs
  - `Call` now guarantees callbacks to happen on the main thread
  - Coroutine users can now `await()` a `Call` easily with a provided extension

## stream-chat-android
- Add empty state views to channel list view and message list view components
- Allow setting custom empty state views
- Add loading view to message list view
- Allow setting custom loading view
- Add load more threshold for `MessageListView` and `streamLoadMoreThreshold` attribute
- Fix handling of the `streamShowReadState` attribute on `MessageListView`
- Add `streamShowDeliveredState` XML attribute to `MessageListView`
- Add "loading more" indicator to the `MessageListView`
- Messages in ChannelController were split in messages - New messages and oldMessages for messages coming from the history.

## stream-chat-android-client
- Fix guest user authentication
- Changed API of QuerySort class. You have to specify for what model it is being used.
- Rename `ChannelController` to `ChannelClient`. Deprecate `ChannelController`.
- Replace `ChannelController` subscribe related extension functions with corresponding `ChannelClient` functions
- Move `ChannelClient` extension functions to `io.getstream.chat.android.client.channel` package

## stream-chat-android-offline
- Add GetChannelController use cases which allows to get ChannelController for Channel
- Fix not storing channels when run channels fetching after connection recovery.
- Fix read state getting stuck in unread state

# Oct 26th, 2020 - 4.4.0
## stream-chat-android
- Create custom login screen in sample app
- Bump Coil to 1.0.0
- Add message sending/sent indicators in `MessageListView`
- Add possibility to replace default FileUploader
- Fixes a race condition where client.getCurrentUser() was set too late
- Support for hiding channels
- Makes the number of channels return configurable by adding the limit param to ChannelsViewModelFactory
- Add message sending/sent indicators in `MessageListView`
- Provide ChannelViewModelFactory and ChannelsViewModelFactory by the library to simplify setup
- Fixes for https://github.com/GetStream/stream-chat-android/issues/698 and https://github.com/GetStream/stream-chat-android/issues/723
- Don't show read state for the current user

## stream-chat-android-client
- Fix ConcurrentModificationException in `ChatEventsObservable`
- Add possibility to replace default FileUploader
- Fix anonymous user authentication
- Fix fetching color value from TypedArray

## stream-chat-android-offline
- Channel list now correctly updates when you send a new message while offline. This fixes https://github.com/GetStream/stream-chat-android/issues/698
- Channels now stay sorted based on the QuerySort order (previous behaviour was to sort them once)
- New messages now default to type "regular" or type "ephemeral" if they start with a /
- Improved error logging on sendMessage & sendReaction
- Fixed a race condition that in rare circumstances could cause the channel list to show stale (offline) data
- Fixed a bug with channel.hidden not working correctly
- Fixed crash with absence of user in the UserMap

# Oct 19th, 2020 - 4.3.1-beta-2 (stream-chat-android)
- Allow setting custom `NotificationHandler` in `Chat.Builder`
- Fix unresponsive attachment upload buttons
- Removed many internal implementation classes and methods from the SDK's public API
- Fix sending GIFs from keyboard
- Fix unresponsive attachment upload buttons
- Fix method to obtain initials from user to be shown into the avatar
- Fix method to obtain initials from channel to be shown into the avatar
- Allow setting `ChatLoggerHandler` and `ChatLogLevel` in `Chat.Builder`

# Oct 16th, 2020 - 4.3.1-beta-1 (stream-chat-android)
- Significant performance improvements
- Fix a crash related to behaviour changes in 1.3.0-alpha08 of the AndroidX Fragment library
- Replace Glide with Coil in AttachmentViewHolderMedia (Fix GIFs loading issues)
- `MessageListView.BubbleHelper`'s methods now have nullability annotations, and use primitive `boolean` values as parameters
- Update Offline Support to the [last version](https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.6)

# Oct 16th, 2020 - 0.8.6 (stream-chat-android-offline)
- Improve sync data validation in ChatDomain.Builder
- Removed many internal implementation classes and methods from the SDK's public API
- Significant performance improvements to offline storage
- Default message limit for the queryChannels use case changed from 10 to 1. This is a more sensible default for the channel list view of most chat apps
- Fix QuerySort
- Update client to 1.16.8: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.8

# 1.16.8 - Fri 16th of Oct 2020 (stream-chat-android-client)
- Add `lastUpdated` property to `Channel`

# Oct 14th, 2020 - 4.3.0-beta-6 (stream-chat-android)
- Update to Kotlin 1.4.10
- Fix Typing view behavior
- Fix NPE asking for `Attachment::type`
- Fix ChatDomain initialization issue
- Limit max lines displayed in link previews (5 lines by default, customizable via `streamAttachmentPreviewMaxLines` attribute on `MessageListView`)
- Update Offline Support to the [last version](. See changes: )https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.5)

# 1.16.7 - Wed 14th of Oct 2020 (stream-chat-android-client)
- Removed many internal implementation classes and methods from the SDK's public API
- Improved nullability, restricted many generic type parameters to be non-nullable (set `Any` as their upper bound)
- Use AttachmentsHelper to validate imageUrl instead of just url.

# Oct 14th, 2020 - 0.8.5 (stream-chat-android-offline)
- Use `createdLocallyAt` and `updatedLocallyAt` properties in ChannelController and ThreadController
- Update attachments of message with an old image url, if it's still valid.
- Set attachment fields even if the file upload fails
- Fix NPE while ChatEvent was handled
- Improved nullability, restricted some generic type parameters to be non-nullable (set `Any` as their upper bound)
- Fix method to store date of the last message received into a channel
- Update client to 1.16.7: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.7

# Oct 9th, 2020 - 4.3.0-beta-5 (stream-chat-android)
- Improve selecting non-media attachments
- Fix showing attachments captured with camera
- Add setting type and file size when creating AttachmentMetaData from file
- Remove FileAttachmentListAdapter and methods related to opening files chooser
- Replace isMedia flag with getting type from attachment if possible
- Update ExoPlayer dependency to version [2.12.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#2120-2020-09-11)

# 1.16.6 - Fri 9th of Oct 2020 (stream-chat-android-client)
- Add `createdLocallyAt` and `updatedLocallyAt` properties to `Message` type
- Add AttachmentsHelper with hasValidUrl method

# Oct 7th, 2020 - 4.3.0-beta-4 (stream-chat-android)
- For Java clients, the `bindView` methods used to bind a ViewModel and its UI component together are now available with friendlier syntax.
- Calls such as `MessageListViewModelBindingKt.bindView(...);` should be replaced with calls like `MessageListViewModelBinding.bind(...);`
- The `ChannelListViewModelBindingKt` class has been renamed to `ChannelsViewModelBinding`, to match the name of the ViewModel it's associated with.
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.4

# Oct 7th, 2020 - 0.8.4 (stream-chat-android-offline)
- Update client to 1.16.5: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5

# 1.16.5 - Wed 7th of Oct 2020 (stream-chat-android-client)
- Add autocomplete filter
- Add @JvmOverloads to QueryUsersRequest constructor
- Improve java interop of `TokenManager`

# Oct 5th, 2020 - 0.8.3 (stream-chat-android-offline)
- Improved message attachment handling. Message is now first added to local storage and the attachment is uploaded afterwards.
- Editing messages now works while offline
- Deprecate SendMessageWithAttachments in favor of SendMessage while specifying attachment.upload
- Fix a bug that caused messages not to load if member limit wasn't specified
- Fix a crash related to reaction data structure
- Fix a bug where network errors (temporary ones) are detected as permanent errors

# 1.16.4 - Mon 5th of Oct 2020 (stream-chat-android-client)
- Add `attachment.upload` and `attachment.uploadState` fields for livedata upload status. These fields are currently unused if you only use the low level client.

# Oct 2nd, 2020 - 4.3.0-beta-3 (stream-chat-android)
- Removed several parameters of `BaseAttachmentViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `AttachmentViewHolderFactory` as constructor parameters
- Moved `BaseAttachmentViewHolder` to a new package
- Fix setting read state when user's last read equals message created date
- Skip setting user's read status if last read message is his own
- Make MessageListItem properties abstract
- Change default query sort to "last_updated"
- Fixed attachments logic. Save previously attached files when add more.
- Fixed the bug when it was unable to select new files when you have already attached something.
- Moved `MessageInputView` class to a new package.
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.2

# Oct 2nd, 2020 - 0.8.2 (stream-chat-android-offline)
- Request members by default when querying channels

# Sep 30th, 2020 - 4.3.0-beta-2 (stream-chat-android)
- Removed several parameters of `BaseMessageListItemViewHolder#bind`, `Context` is now available as a property instead, others should be passed in through the `MessageViewHolderFactory` as constructor parameters
- Attachment customization methods moved from `MessageViewHolderFactory` to a separate `AttachmentViewHolderFactory` class
- Removed `position` parameter from `MessageClickListener`
- Moved `BaseMessageListItemViewHolder` to a new package
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.8.1

# Sep 30th, 2020 - 0.8.1 (stream-chat-android-offline)
- Handle the new `ChannelUpdatedByUserEvent`
- Update client to 1.16.1: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Improve online status handling
- Replace posting an empty channels map when the channels query wasn't run online and offline storage is empty with error

# 1.16.2 - Wed 30 Sep 2020 (stream-chat-android-client)
- Add `ChatClient::enableSlowMode` method to enable slow mode
- Add `ChatClient::disableSlowMode` method to disable slow mode
- Add `ChannelController::enableSlowMode` method to enable slow mode
- Add `ChannelController::disableSlowMode` method to disable slow mode
- Add `Channel::cooldown` property to know how configured `cooldown` time for the channel
- Fix FirebaseMessageParserImpl.verifyPayload() logic
- Fix notification display condition
- Fix Socket connection issues

# 1.16.1 - Wed 25 Sep 2020 (stream-chat-android-client)
- Remove `User` field on `ChannelUpdatedEvent`
- Add new chat event type -> `ChannelUpdatedByUserEvent`
- Add `ChatNotificationHandler::getFirebaseInstanceId` method to provide a custom `FirebaseInstanceId`
- Add `NotificationConfig::useProvidedFirebaseInstance` conf

# Sep 23rd, 2020 - 4.3.0-beta-1 (stream-chat-android)
- Update livedata/client to latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# 1.16.0 - Wed 23 Sep 2020 (stream-chat-android-client)
- Removed message.channel, this is a backwards incompatible change
- Ensure that message.cid is always available

The SDK was providing message.cid and message.channel in some cases, but not always.
Code that relied on those fields being populated caused bugs in production.

If you were relying on message.channel it's likely that you were running into bugs.
We recommend using one of these alternatives:

- message.cid if you just need a reference to the channel
- the channel object provided by client.queryChannel(s) if you need the full channel data
- channelController.channelData livedata object provided by the livedata package (automatically updated if channel data changes)
- channelController.toChannel() function provided by the livedata package

# Sep 23rd, 2020 - 0.8.0 (stream-chat-android-offline)
- Update client to 1.16.0: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# Sep 23rd, 2020 - 0.7.7 (stream-chat-android-offline)
- Fix crash when map channels DB entity to Channel
- Add posting empty channels map when queryChannels fails either offline and online which prevents infinite loader

# 1.15.6 - Wed 23 Sep 2020 (stream-chat-android-client)
- Convert ChatError to plain class. Changes in ChatLogger interface.
- Update events fields related to read status - remove "unread_messages" field and add "unread_channels" to NewMessageEvent, NotificationMarkReadEvent, and NotificationMessageNewEvent
- Mark ChatEvents containing the user property by the UserEvent interface.
- Simplified the event handling APIs, deprecated `ChatObservable`. See [the migration guide](https://github.com/GetStream/stream-chat-android-client/wiki/Migrating-from-the-old-event-subscription-APIs) for details on how to easily adopt the new APIs.

# Sep 23rd, 2020 - 4.2.11-beta-13 (stream-chat-android)
- Adjust ChatSocketListener to new events(NewMessageEvent, NotificationMarkReadEvent, NotificationMessageNewEvent) properties.
- Fix "load more channels"
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.6
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.7

# Sep 18th, 2020 - 4.2.11-beta-12 (stream-chat-android)
- Implement Giphy actions handler
- Fix .gif preview rendering on message list
- Fix thread shown issue after sending message to a channel
- Remove border related attributes from MessageInputView. Add close button background attribute to MessageInputView.
- Improve setting user in sample app
- Add updating message read state after loading first messages
- Wrap Attachment into AttachmentListItem for use in adapter
- Properly show the message date
- Revamp MessageListView adapter customization, introduce ListenerContainer to handle all ViewHolder listeners
- Fix default filters on `ChannelsViewModelImpl`
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.5
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.6

# Sep 18th, 2020 - 0.7.6 (stream-chat-android-offline)
- Store needed users in DB
- Stop trying to execute background sync in case ChatDomain.offlineEnabled is set to false
- Fix Socket Connection/Reconnection
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.5

# 1.15.5 - Fri 18 Sep 2020 (stream-chat-android-client)
- Fix Socket Connection/Reconnection

# Sep 15th, 2020 - 0.7.5 (stream-chat-android-offline)
- Fix offline support for adding and removing reactions
- Fix crash when creating a channel while channel.createdBy is not set

# Sep 14th, 2020 - 0.7.4 (stream-chat-android-offline)
- Remove duplicates of new channels
- Improve tests
- Remove some message's properties that are not used anymore GetStream/stream-chat-android-client#69
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.4

# 1.15.4 - Fri 11 Sep 2020 (stream-chat-android-client)
- Fix Socket Disconnection
- Remove useless message's properties (isStartDay, isYesterday, isToday, date, time and commandInfo)
- Forbid setting new user when previous one wasn't disconnected

# Sep 8th, 2020 - 0.7.3 (stream-chat-android-offline)
- Add usecase to send Giphy command
- Add usecase to shuffle a Gif on Giphy command message
- Add usecase to cancel Giphy Command
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.3

# 1.15.3 - Tue 7 Sep 2020 (stream-chat-android-client)
- Add send action operation to ChannelController
- Fix serialized file names of SendActionRequest
- Fix `ConnectedEvent` parse process

# Sep 4th, 2020 - 4.2.11-beta-11 (stream-chat-android)
- Fix uploading files and capturing images on Android >= 10
- Fix `AvatarView`: Render lastActiveUsers avatars when channel image is not present

# 1.15.2 - Tue 1 Sep 2020 (stream-chat-android-client)
- `ChannelResponse.watchers` is an array of User now
- `Watcher` model has been removed, `User` model should be used instead
- `QueryChannelsRequet` has a new field called `memberLimit` to limit the number of members received per channel

# Aug 28th, 2020 - 4.2.11-beta-9 (stream-chat-android)
- Update event structure
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.7.2

# 1.15.1 - Thu 28 Aug 2020 (stream-chat-android-client)
- New MapAdapter that omit key that contains null values or emptyMaps
- Null-Check over Watchers response

## Aug 23th, 2020 - 4.2.11-beta-8 (stream-chat-android)
- Fix Upload Files
- Update RecyclerView Lib
- Update Notification Customization

# Aug 28th, 2020 - 0.7.2 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1

# Aug 28th, 2020 - 0.7.1 (stream-chat-android-offline)
- Keep order when retry to send a message
- Fix message sync logic and message sending success event emitting
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.0

# Aug 20th, 2020 - 0.7.0 (stream-chat-android-offline)
- Update to version 0.7.0

# 1.15.0 - Thu 20 Aug 2020 (stream-chat-android-client)
- Refactor ChatEvents Structure

# 1.14.0 - Thu 20 Aug 2020 (stream-chat-android-client)
- Decouple cloud messages handler logic from configuration data
- Fix createChannel methods

# 1.13.3 - Tue 18 Aug 2020 (stream-chat-android-client)
- Set message as optional when updating a channel

# 1.13.2 - Fri 14 Aug 2020 (stream-chat-android-client)
- Reduce TLS Latency

# 1.13.1 - Fri 7 Aug 2020 (stream-chat-android-client)
- Fix DateParser

## Aug 5th, 2020 - 4.2.11-beta-7 (stream-chat-android)
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.6.9
- Fix channel name validation in CreateChannelViewModel
- Add `ChannelsView.setViewHolderFactory(factory: ChannelViewHolderFactory)` function
- Fix Fresco initialization
- Fix method to add/remove reaction

# Aug 3nd, 2020 - 0.6.9 (stream-chat-android-offline)
- Fix `QuerySort`

# 1.13.0 - Tue 28 Jul 2020 (stream-chat-android-client)
- Add `Client.flagUser()` method to flag an User
- Add `Client.flagMessage()` method to flag a Message
- Deprecated method `Client.flag()` because was a bit confusing, you should use `client.flagUser()` instead

# 1.12.3 - Mon 27 Jul 2020 (stream-chat-android-client)
- Fix NPE on TokenManagerImpl
- Upgrade Kotlin to version 1.3.72
- Add Kotlin Proguard Rules

# Jul 20th, 2020 - 0.6.8 (stream-chat-android-offline)
- Fix `NotificationAddedToChannelEvent` event handling

# 1.12.2 - Fri 17 Jul 2020 (stream-chat-android-client)
- Add customer proguard rules

# 1.12.1 - Wed 15 Jul 2020 (stream-chat-android-client)
- Add customer proguard rules

## Jul 13th, 2020 - 4.2.11-beta-6 (stream-chat-android)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.10.0
- Update Stream Livedata to the last version. See changes: https://github.com/GetStream/stream-chat-android-livedata/releases/tag/0.6.7
- Refactor ChannelHeaderView
- Refactor MesageInputView
- Refactor Permission Checker Behavior
- Refactor MessageListVIew
- Fix Send Attachment Behavior
- Fix "Take Picture/Record Video" Behavior
- Add option to show empty view when there are no channels
- Add option to send a message to a thread
- Allow to switch user / logout

# 1.12.0 - Mon 06 Jul 2020 (stream-chat-android-client)
- Add mute and unmute methods to channel controller

# 1.11.0 - Mon 06 Jul 2020 (stream-chat-android-client)
- Fix message mentioned users

# Jul 3nd, 2020 - 0.6.7 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.10.0
- Implement Thread Behavior

# 1.10.0 - Wed 29 June 2020 (stream-chat-android-client)
- Add mute and unmute channels
- Add `notification.channel_mutes_updated` socket even handling
- Add user.channelMutes field
- Improve error logging
- Add invalid date format handling (channel.config dates might be invalid)

# 1.9.3 - Wed 29 June 2020 (stream-chat-android-client)
- Add raw socket events logging. See with tag `Chat:Events`

# Jun 24th, 2020 - 0.6.6 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.9.2

# 1.9.2 - Wed 24 June 2020 (stream-chat-android-client)
- Add `show_in_channel` attribute to `Message` entity

# 1.9.1 - Mue 23 June 2020 (stream-chat-android-client)
- Fix multithreaded date parsing

# 1.9.0 - Mon 22 June 2020 (stream-chat-android-client)
- Fix search message request body
  🚨 Breaking change:
- client.searchMessages signature has been changed: query removed, added channel filter

# 1.8.1 - Thu 18 June 2020 (stream-chat-android-client)
- Fix UTC date for sync endpoint
- Fix inhered events parsing
- Fix custom url setter of ChatClient.Builder

# Jun 16th, 2020 - 0.6.5 (stream-chat-android-offline)
- Fixed crash caused by `NotificationMarkReadEvent.user` value being sent null.
- Solution: using the current user which was set to the ChatDomain instead of relying on event's data.

# 1.8.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add sync api call

# Jun 12th, 2020 - 0.6.4 (stream-chat-android-offline)
- Add attachment.type when upload a file or image

# 1.7.0 - Thu 12 June 2020 (stream-chat-android-client)
- Add query members call

# Jun 11th, 2020 - 0.6.3 (stream-chat-android-offline)
- Create a new UseCase to send messages with attachments

# Jun 11th, 2020 - 0.6.2 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.6.1

# 1.6.1 - Thu 11 June 2020 (stream-chat-android-client)
- Add MimeType on sendFile and sendImage methods

# 1.6.0 - Mon 8 June 2020 (stream-chat-android-client)
- Add translations api call and update message with `i18n` field. Helper `Message` extensions functions are added.

## Jun 4th, 2020 - 4.2.11-beta-5 (stream-chat-android)
- Update livedata dependency to fix crash when NotificationMarkReadEvent received
- Add mavenLocal() repository

## Jun 4th, 2020 - 4.2.11-beta-4 (stream-chat-android)
- Fix crash when command (`/`) is typed.

## Jun 3rd, 2020 - 4.2.11-beta (stream-chat-android)
- Fix `AvatarView` crash when the view is not attached

# 1.5.4 - Wed 3 June 2020 (stream-chat-android-client)
- Add optional `userId` parameter to `Channel.getUnreadMessagesCount` to filter out unread messages for the user

# 1.5.3 - Wed 3 June 2020 (stream-chat-android-client)
- Fix switching users issue: `disconnect` and `setUser` resulted in wrong user connection

# 1.5.2 - Tue 2 June 2020 (stream-chat-android-client)
- Fix `ConcurrentModificationException` on multithread access to socket listeners

# May 30th, 2020 - 0.6.1 (stream-chat-android-offline)
- Use the new low level client syntax for creating a channel with members
- Fallback to a default channel config if the real channel config isn't available yet. This fixes GetStream/stream-chat-android#486

# May 27th, 2020 - 0.6.0 (stream-chat-android-offline)
- Update client to the latest version: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.5.0

# 1.5.1 - Wed 27 May 2020 (stream-chat-android-client)
- Add filter contains with any value

# May 26th, 2020 - 0.5.2 (stream-chat-android-offline)
- Test cases for notification removed from channel had the wrong data structure. This caused a crash when this event was triggered.

# 1.5.0 - Mon 26 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Add new constructor field to `Channel`: `team`
- Add new constructor field to `User`: `teams`

✅ Other changes:
- Add `Filter.contains`

# 1.4.17 - Mon 26 May 2020 (stream-chat-android-client)
- Fix loop on client.create
- Fix crash when backend sends first event without me

# May 25th, 2020 - 0.5.1 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.16

# 1.4.16 - Mon 25 May 2020 (stream-chat-android-client)
Breaking change:
- `Command` fields are mandatory and marked as non-nullable

# May 24th, 2020 - 0.5.0 (stream-chat-android-offline)
Livedata now supports all events exposed by the chat API. The 3 new events are:
- Channel truncated
- Notification channel truncated
- Channel Deleted
  This release also improves how new channels are created.

# May 23rd, 2020 - 0.4.8 (stream-chat-android-offline)
- NotificationMessageNew doesn't specify event.message.cid, this was causing issues with offline storage. The test suite has been updated and the issue is now resolved. Also see: GetStream/stream-chat-android#490

# May 23rd, 2020 - 0.4.7 (stream-chat-android-offline)
- Fixed NPE on MemberRemoved event GetStream/stream-chat-android#476
- Updates low level client to fix GetStream/stream-chat-android#492

# 1.4.15 - Fri 22 May 2020 (stream-chat-android-client)
- Add events: `ChannelTruncated`, `NotificationChannelTruncated`, `NotificationChannelDeleted`

# 1.4.13 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- Fields `role` and `isInvited` of ``Member` fields optional

# 1.4.12 - Fri 22 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `Member` model is cleaned up from non existing fields

# May 20th, 2020 - 0.4.6 (stream-chat-android-offline)
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.11

# 1.4.11 - Tue 19 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix null fields which are not marked as nullable

# 1.4.10 - Tue 19 May 2020 (stream-chat-android-client)
- Fix add member invalid api key

# 1.4.9 - Mon 18 May 2020 (stream-chat-android-client)
🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix `ChannelController.markRead`: was marking read all channels instead of current one
- `ChatClient.markRead` accepts optional `messageId`

# 1.4.8 - Mon 18 May 2020 (stream-chat-android-client)
- Add handling invalid event payload

# May 16th, 2020 - 0.4.5 (stream-chat-android-offline)
- Improved handling of unread counts. Fixes GetStream/stream-chat-android#475

# May 16th, 2020 - 0.4.4 (stream-chat-android-offline)
- GetStream/stream-chat-android#476

## May 15th, 2020 - 4.2.10-beta (stream-chat-android)
- Update to the latest livedata: 0.6.1

# May 15th, 2020 - 0.4.3 (stream-chat-android-offline)
- Resolves this ticket: GetStream/stream-chat-android#479

## May 29th, 2020 - 4.2.9-beta-3 (stream-chat-android)
- Fix AttachmentViewHolder crash when user sends message with plain/no-media url

## May 15th, 2020 - 4.2.9-beta-2 (stream-chat-android)
- Update to the latest livedata: 0.6.0

## May 15th, 2020 - 4.2.8-beta-1 (stream-chat-android)
- Update to the latest livedata: 0.4.6

## May 15th, 2020 - 4.2.6 (stream-chat-android)
- Fix Avatar crash if channel/user initials are empty

# 1.4.7 - Tue 14 May 2020 (stream-chat-android-client)
- Add more channel creation signatures to `Client` and `ChannelController`

# 1.4.6 - Tue 14 May 2020 (stream-chat-android-client)
- Move channel out of message constructor

## May 13th, 2020 - 4.2.5 (stream-chat-android)
- Create new `AvatarView`
- Glide Redirect issues resolved
- Bugfix release for livedata, updated to 0.4.2

# May 13th, 2020 - 0.4.2 (stream-chat-android-offline)
-NotificationAddedToChannelEvent cid parsing didn't work correctly. This has been fixed in 0.4.2

# May 13th, 2020 - 0.4.1 (stream-chat-android-offline)
- There was an issue with the 0.4.0 and the data structure for NotificationMarkRead

# May 13th, 2020 - 0.4.0 (stream-chat-android-offline)
## Features:
- Massive improvement to javadoc/dokka
- Support for user ban events. Exposed via chatDomain.banned
- Muted users are available via chatDomain.muted
- Support for notificationMarkRead, invite and removed from channel events
- Support for deleting channels
- Support for silent messages
- Creating channels with both members and additional data works now
- User presence is enabled

##Bugfixes:
- No longer denormalizing channelData.lastMessageAt
- Fixed an issue with channel event handling and the usage of channel.id vs channel.cid
- Changed channelData.createdBy from lateinit to a regular field

##Other:
- Moved from Travis to Github actions

# 1.4.5 - Tue 12 May 2020 (stream-chat-android-client)
- add message.silent field
- add extension properties `name` and `image` to `Channel` and `User`

## March 11th, 2020 - 3.6.5 (stream-chat-android)
- Fix reaction score parser casting exception

# May 8th, 2020 - 0.3.4 (stream-chat-android-offline)
- added support for muting users
- store the current user in offline storage
- performance tests
- removed launcher icons from lib
- forward compatibility with new event sync endpoint
- support for reaction scores

# 1.4.3 - Thu 7 May 2020 (stream-chat-android-client)
- fix type erasure of parsed collections: `LinkedTreeMap`, but not `List<Reaction>`

# 1.4.2 - Mon 4 May 2020 (stream-chat-android-client)
- add `reactionScores` to `Message`
- fix null write crash of CustomObject nullable field
- fix extraData duplicated fields

# May 2nd, 2020 - 0.3.1 (stream-chat-android-offline)
- Make the channel unread counts easily accessible via channel.unreadCount
- Support for muting users
- Detection for permanent vs temporary errors (which helps improve retry logic)
- Bugfix: Fixes edge cases where recovery flow runs before the existing API calls complete

# 1.4.0 - Fri 1 May 2020 (stream-chat-android-client)
- fix `QueryChannelRequest` when `withMessages/withMembers` is called, but messages were not returned
- add `unreadMessages` to `ChannelUserRead`. Add extension for channel to count total unread messages: `channel.getUnreadMessagesCount()`

# 1.3.0 - Wed 30 Apr 2020 (stream-chat-android-client)
🚨 Breaking changes:
- `TokenProvider` signature enforces async execution
- make socket related classes internal

✅ Other changes
- fix endlessly hanging request in case setUser is not called
- fix expired token case on socket connection
- fix client crash if TokenProvider throws an exception

# Apr 29th, 2020 - 0.3.0 (stream-chat-android-offline)
- Handle edge cases where events are received out of order
- KTlint, travis and coverage reporting
- Interfaces for use cases and controllers for easier testing
- Channel data to isolate channel data vs rest of channel state
- Java version of code examples
- Handle edge cases for channels with more than 100 members
- Test coverage on mark read
- Bugfix queryChannelsController returning duplicate channels
- Support for hiding and showing channels
- Full offline pagination support (including the difference between GTE and GT filters)

# 1.2.2 - Wed 29 Apr 2020 (stream-chat-android-client)
🚨 Breaking changes:
- fields of models are moved to constructors: `io.getstream.chat.android.client.models`
- field of Device `push_provider` renamed to `pushProvider` and moved to constructor

✅ Other changes
- added local error codes with descriptions: `io.getstream.chat.android.client.errors.ChatErrorCode`
- fix uncaught java.lang.ExceptionInInitializerError while parsing custom object

# Apr 22nd, 2020 - 0.2.1 (stream-chat-android-offline)
- Better handling for missing cids

# Apr 22nd, 2020 - 0.2.0 (stream-chat-android-offline)
- Test suite > 100 tests
- Sample app (stream-chat-android) works
- Full offline sync for channels, messages and reactions
- Easy to use livedata objects for building your own UI

# Apr 22nd, 2020 - 0.1.0 (stream-chat-android-offline)
- First Release

## March 3rd, 2020 - 3.6.5 (stream-chat-android)
- Fix crash on sending Google gif

## March 3rd, 2020 - 3.6.4 (stream-chat-android)
- Update default endpoint: from `chat-us-east-1.stream-io-api.com` to `chat-us-east-staging.stream-io-api.com`
- update target api level to 29
- Fixed media playback error on api 29 devices
- Added score field to reaction model

## January 28th, 2020 - 3.6.3 (stream-chat-android)
- ViewModel & ViewHolder classes now use protected instead of private variables to allow customization via subclassing
- ChannelViewHolderFactory is now easier to customize
- Added ChannelViewHolder.messageInputText for 2 way data binding
- Documentation improvements
- Fix problem with wrong scroll position

## January 10th, 2020 - 3.6.2 (stream-chat-android)
- Enable multiline edit text
- Fix deprecated getColumnIndexOrThrow for 29 Api Level

## January 7th, 2020 - 3.6.1 (stream-chat-android)
- Add navigation components with handler to override default behaviour

## Breaking changes:
###
- `OpenCameraViewListener` is replaced with CameraDestination

## January 6th, 2020 - 3.6.0 (stream-chat-android)
- Add `MessageSendListener` interface for sending Message
- Update `README` about Customizing MessageInputView
- Client support for anonymous and guest users
- Client support initialization with Configurator
- Support auto capitalization for keyboard
- Add `NotificationManager` with customization opportunity
- Update `UpdateChannelRequest` for reserved fields
- renamed `MoreActionDialog` to `MessageMoreActionDialog`
- Add `StreamLoggerHandler` interface for custom logging client data
- Add logging customization ability
- fix markdown for mention if there is no space at prefix @
- fix Edit Attachment behavior
- add support for channel.hide with clear history + events
- Fix crash in AttachmentActivity and AttachmentDocumentActivity crash when app is killed in background
- Add utility method StreamChat.isConnected()

#### Breaking changes:

##### Channel hide request
- `Channel:hide` signature has changed: `HideChannelRequest` must be specified as first parameter
- `Client:hideChannel` signature has changed: `HideChannelRequest` must be specified as second parameter
- `ChannelListViewModel:hideChannel` signature has changed: `HideChannelRequest` must be specified as second parameter

##### How to upgrade
To keep the same behavior pass `new HideChannelRequest()` as request parameter to match with the new signature.

## December 9th, 2019 - 3.5.0 (stream-chat-android)
- Fix set typeFace without custom font
- Fix channel.watch (data payload was not sent)
- Fix API 23 compatiblity
- Add Attachment Border Color attrs
- Add Message Link Text Color attrs
- Add custom api endpoint config to sample app and SDK

## November 28th, 2019 - 3.4.1 (stream-chat-android)
- Fix Giphy buttons alignments
- Add Giphy error cases handling
- Update http related issues documentation


## November 28th, 2019 - 3.4.0 (stream-chat-android)
- Custom font fot the whole SDK
- Custom font per TextView
- Ignore sample app release unit tests, keep debug tests
- Added AttachmentBackgroundColorMine/Theirs
- Fix Edit/Delete thread parent message
- Replace fadein/fadeout animation of parent/current thread with default RecyclerView animation

## November 5th, 2019 - 3.3.0 (stream-chat-android)
- Fix Concurrent modification when removing member from channel
- Fix automention input issue
- Fix Sent message progress infinite
- Fix channel delete event handling in ChannelList view model
- Fix attachment duplicated issue when message edit
- Add File Upload 2.0
- Add editMessage function in Channel View Model
- Fix JSON encoding always omits null fields
- Sample app: add version header, release version signing
- Add Message Username and Date attrs


## November 5th, 2019 - 3.2.1 (stream-chat-android)
- Fixed transparency issues with user profile images on older devices
- Better channel header title for channels without a name
- Fixed read count difference between own and other users' messages
- Fixed Video length preview
- Catch error body parsing errors
- Do not show commands list UI when all commands are disabled
- Renamed `MessageInputClient` to `MessageInputController`
- Added Large file(20MB) check for uploading file
- Added streamUserNameShow and streamMessageDateShow in `MessageListViewStyle`
- Fixed channel header title position issue when Last Active is hidden


## October 25th, 2019 - 3.2.0 (stream-chat-android)
- Added event interceptors to `ChannelListViewModel`

## October 24th, 2019 - 3.1.0 (stream-chat-android)
- Add channel to list when the user is added
- Add `onUserDisconnected` event
- Make sure channel list view model is cleared when the user disconnects
- Fix bug with `setUser` when user data is not correctly URI encoded
- Add debug/info logging
- Add Attrs for DateSeparator

## Oct 23th, 2019 - 3.0.2 (stream-chat-android)
- Fix NPE with restore from background and null users

## Oct 22th, 2019 - 3.0.1 (stream-chat-android)
- Fix NPE with empty channel lists

## Oct 21th, 2019 - 3.0.0 (stream-chat-android)
- Added support for message search `client.searchMessages`
- Better support for query user options
- Update channel update signature
- Fix disconnection NPE
- Minor bugfixes
- Remove file/image support
- Expose members and watchers pagination options for query channel

#### Breaking changes
- `Channel.update` signature has changed

## Oct 16th, 2019 - 2.3.0 (stream-chat-android)
- Added support for `getReactions` endpoint
- Calls to `ChannelListViewModel#setChannelFilter` will reload the list of channels if necessary
- Added support for `channel.stopWatching()`
- Improved error message for uploading large files
- Remove error messages after you send a message (similar behaviour to Slack)
- Fixed slash command support on threads
- Improved newline handling
- Improved thread display
- Expose ban information for current user (`User#getBanned`)
- Bugfix on attachment size
- Added support for accepting and rejecting channel invites
- Expose current user LiveData with `StreamChat.getCurrentUser()`

## Oct 14th, 2019 - 2.2.1 (stream-chat-android)
- Renamed `FileSendResponse` to `UploadFileResponse`
- Renamed `SendFileCallback` to `UploadFileCallback`
- Removed `SendMessageRequest`
- Updated `sendMessage` and `updateMessage` from `Client`
- Added devToken function for setUser of Client
- Added a callback as an optional last argument for setUser functions
- Added ClientState which stores users, current user, unreadCount and the current user's mutes
- Added notification.mutes_updated event
- Add support for add/remove channel members
- Expose channel unread messages counts for any user in the channel

## Oct 9, 2019 - 2.2.0 (stream-chat-android)
- Limit message input height to 7 rows
- Fixed thread safety issues on Client.java
- Fixed serialization of custom fields for message/user/channel and attachment types
- Added support for distinct channels
- Added support to Channel hide/show
- Improved client error reporting (we now return a parsed error response when available)
- General improvements to Message Input View
- Added ReactionViewClickListener
- Added support for banning and unbanning users
- Added support for deleting a channel
- Add support for switching users via `client.disconnect` and `client.setUser`
- Add `reload` method to `ChannelListViewModel`
- Bugfix: hides attachment drawer after deny permission
- Add support for update channel endpoint
- Add PermissionRequestListener for Permission Request

## September 28, 2019 - 2.1.0 (stream-chat-android)
- Improved support for regenerating expired tokens

#### Breaking changes:
- `MessageInputView#progressCapturedMedia(int requestCode, int resultCode, Intent data)` renamed into `captureMedia(int requestCode, int resultCode, Intent data)`
- `binding.messageInput.permissionResult(requestCode, permissions, grantResults)` in `onRequestPermissionsResult(requestCode, permissions, grantResults) of `ChannelActivity`

## September 28, 2019 - 2.0.1 (stream-chat-android)
- Fix channel list ordering when a channel is added directly from Android
- Better Proguard support

## September 26, 2019 - 2.0.0 (stream-chat-android)
- Simplify random access to channels
- Channel query and watch methods now work the same as they do on all other SDKs

#### Breaking changes:
- `channel.query` does not watch the channel anymore, to retrieve channel state and watch use `channel.watch`
- `client.getChannelByCID` is now private, use one of the `client.channel` methods to get the same (no null checks needed)
