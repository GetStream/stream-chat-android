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
- Added new attributes to `MessageInputView` allowing to customize the style of input field during command input:
    - `streamUiCommandInputBadgeTextSize`, `streamUiCommandInputBadgeTextColor`, `streamUiCommandInputBadgeFontAssets`, `streamUiCommandInputBadgeFont`, `streamUiCommandInputBadgeStyle` attributes to customize the text appearance of command name inside command badge
    - `streamUiCommandInputCancelIcon` attribute to customize the icon for cancel button
    - `streamUiCommandInputBadgeIcon` attribute to customize the icon inside command badge
    - `streamUiCommandInputBadgeBackgroundDrawable` attribute to customize the background shape of command badge
- Added possibility to customize `MessageListHeaderView` style via `streamUiMessageListHeaderStyle` theme attribute and via `TransformStyle.messageListHeaderStyleTransformer`.
- Added new attributes to `MessageInputView`:
    - `streamUiCommandIcon` attribute to customize the command icon displayed for each command item in the suggestion list popup
    - `streamUiLightningIcon` attribute to customize the lightning icon displayed in the top left corner of the suggestion list popup

### ⚠️ Changed
- 🚨 Breaking change: moved `commandsTitleTextStyle`, `commandsNameTextStyle`, `commandsDescriptionTextStyle`, `mentionsUsernameTextStyle`, `mentionsNameTextStyle`, `mentionsIcon`, `suggestionsBackground` fields from `MessageInputViewStyle` to `SuggestionListViewStyle`. Their values can be customized via `TransformStyle.suggestionListStyleTransformer`.
- Made `SuggestionListController` and `SuggestionListUi` public. Note that both of these are _experimental_, which means that the API might change at any time in the future (even without a deprecation cycle).
- Made `AttachmentSelectionDialogFragment` _experimental_ which means that the API might change at any time in the future (even without a deprecation cycle).

### ❌ Removed
