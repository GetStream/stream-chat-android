## Common changes for all artifacts
### 🐞 Fixed

### ⬆️ Improved
- Updated to Kotlin 1.5.20

### ✅ Added

### ⚠️ Changed

### ❌ Removed


## stream-chat-android
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added
- Added `ChatUi.Builder#withImageHeadersProvider` to allow adding custom headers to image requests

### ⚠️ Changed

### ❌ Removed


## stream-chat-android-client
### 🐞 Fixed

### ⬆️ Improved

### ✅ Added

### ⚠️ Changed
- Using the `useNewSerialization` option on the `ChatClient.Builder` to opt out from using the new serialization implementation is now an error. Please start using the new serialization implementation, or report any issues keeping you from doing so. The old implementation will be removed soon.

### ❌ Removed


## stream-chat-android-offline
### 🐞 Fixed
- By default we use backend request to define is new message event related to our query channels specs or not. Now filtering by BE only fields works for channels

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
- Added the `MessageInputView::setMaxMessageLengthHandler` method which allows to set a handler to display a custom error message when the message length is exceeded.  

### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.
- Made `SuggestionListController` and `SuggestionListUi` public. Note that both of these are _experimental_, which means that the API might change at any time in the future (even without a deprecation cycle).
- Made `AttachmentSelectionDialogFragment` _experimental_ which means that the API might change at any time in the future (even without a deprecation cycle).

### ❌ Removed
